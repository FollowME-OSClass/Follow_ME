package com.oss.followMe

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.SearchView
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.oss.followMe.databinding.ActivitySearchBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class SearchActivity : ComponentActivity(), View.OnClickListener
{
    private lateinit var _searchBinding: ActivitySearchBinding
    private val searchBinding get() = _searchBinding

    private var themeDataSet = mutableListOf<ThemeData>()
    private val pageNum = 1

    private val searchAdapter = SearchAdapter()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        _searchBinding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(searchBinding.root)

        searchInit()

        searchBinding.searchTravel.isClickable = false
        searchBinding.themeTravel.setOnClickListener(this)
        searchBinding.myInfoTravel.setOnClickListener(this)

        searchBinding.searchThemeBtn.setOnClickListener(this)
        searchBinding.searchRestaurantBtn.setOnClickListener(this)
        searchBinding.searchHotelBtn.setOnClickListener(this)

    }

    private fun searchInit()
    {
        searchBinding.searchBar.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            @SuppressLint("NotifyDataSetChanged")
            override fun onQueryTextSubmit(p0: String?): Boolean
            {
                val searchBarThread = SearchBarThread(p0)

                searchBarThread.start()
                searchBarThread.join()

                val linearLayout = LinearLayoutManager(applicationContext)
                searchBinding.searchRecycle.layoutManager = linearLayout

                searchBinding.searchRecycle.removeAllViewsInLayout()

                searchAdapter.data = themeDataSet
                searchAdapter.notifyDataSetChanged()

                searchBinding.searchRecycle.adapter = searchAdapter

                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {
                return true
            }
        })
    }

    override fun onClick(v: View?)
    {
        if(v != null)
        {
            when(v)
            {
                searchBinding.themeTravel ->
                {
                    val moveHomeActivity = Intent(this, HomeActivity::class.java)
                    startActivity(moveHomeActivity)
                }

                searchBinding.myInfoTravel ->
                {

                }

                // 관광지 검색
                searchBinding.searchThemeBtn ->
                {
                    val searchThread = SearchThread("c1")

                    searchAdapting(searchThread)

                    initClickable()
                    searchBinding.searchThemeBtn.isClickable = false
                }
                // 식당 검색
                searchBinding.searchRestaurantBtn ->
                {
                    val searchThread = SearchThread("c4")

                    searchAdapting(searchThread)

                    initClickable()
                    searchBinding.searchRestaurantBtn.isClickable = false
                }

                searchBinding.searchHotelBtn ->
                {
                    val searchThread = SearchThread("c3")

                    searchAdapting(searchThread)

                    initClickable()
                    searchBinding.searchHotelBtn.isClickable = false
                }
            }
        }
    }

    private fun searchBarText(text: String?)
    {
        val themeUrl = "https://api.visitjeju.net/vsjApi/contents/searchList?apiKey=nueb4lmqst5qc9de&locale=kr"
        val urlBuilder = StringBuilder(themeUrl)

        // 페이지 번호
        urlBuilder.append("&" + URLEncoder.encode("page", "UTF-8") + "=" + URLEncoder.encode(pageNum.toString(), "UTF-8"))
        // 관광지 제목
        urlBuilder.append("&" + URLEncoder.encode("title", "UTF-8") + "=" + URLEncoder.encode(text, "UTF-8"))

        val urlTheme = URL(urlBuilder.toString())
        val themeConnect: HttpURLConnection = urlTheme.openConnection() as HttpURLConnection

        themeConnect.requestMethod = "GET"
        themeConnect.setRequestProperty("Content-type", "application/json")

        val br: BufferedReader =
            if (themeConnect.responseCode in 200..300) BufferedReader(InputStreamReader(themeConnect.inputStream))
            else BufferedReader(InputStreamReader(themeConnect.errorStream))

        val line = br.readLine()

        br.close()
        themeConnect.disconnect()

        val result = line.toString()

        val items = JSONObject(result).getJSONArray("items")

        for (i in 0 until items.length())
        {
            val cg = items.getJSONObject(i).getJSONObject("contentscd").getString("value")
            if(cg != "c6")
            {
                themeDataSet.add(
                    ThemeData(
                        // img
                        items.getJSONObject(i).getJSONObject("repPhoto").getJSONObject("photoid").getString("imgpath"),
                        // title
                        items.getJSONObject(i).getString("title"),
                        // intro
                        items.getJSONObject(i).getString("introduction"),
                        // address
                        "주소: " + items.getJSONObject(i).getString("address"),
                        // alltag
                        items.getJSONObject(i).getString("alltag"),
                        // region
                        items.getJSONObject(i).getJSONObject("region2cd").getString("label"),
                        // phone
                        items.getJSONObject(i).getString("phoneno")
                    )
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchAdapting(thread: SearchThread)
    {
        thread.start()
        thread.join()

        val linearLayout = LinearLayoutManager(this)
        searchBinding.searchRecycle.layoutManager = linearLayout

        searchBinding.searchRecycle.removeAllViewsInLayout()

        searchAdapter.data = themeDataSet
        searchAdapter.notifyDataSetChanged()

        searchBinding.searchRecycle.adapter = searchAdapter
    }

    private fun initClickable()
    {
        searchBinding.searchThemeBtn.isClickable = true
        searchBinding.searchRestaurantBtn.isClickable = true
        searchBinding.searchHotelBtn.isClickable = true
    }

    private fun searchTheme(category: String)
    {

        val themeUrl = "https://api.visitjeju.net/vsjApi/contents/searchList?apiKey=nueb4lmqst5qc9de&locale=kr"
        val urlBuilder = StringBuilder(themeUrl)

        // 페이지 번호
        urlBuilder.append("&" + URLEncoder.encode("page", "UTF-8") + "=" + URLEncoder.encode(pageNum.toString(), "UTF-8"))

        urlBuilder.append("&" + URLEncoder.encode("category", "UTF-8") + "=" + URLEncoder.encode(category, "UTF-8"))

        val urlTheme = URL(urlBuilder.toString())
        val themeConnect: HttpURLConnection = urlTheme.openConnection() as HttpURLConnection

        themeConnect.requestMethod = "GET"
        themeConnect.setRequestProperty("Content-type", "application/json")

        Log.d("ConnectCheck", themeConnect.toString())

        val br: BufferedReader =
            if (themeConnect.responseCode in 200..300) BufferedReader(InputStreamReader(themeConnect.inputStream))
            else BufferedReader(InputStreamReader(themeConnect.errorStream))

        val line = br.readLine()

        br.close()
        themeConnect.disconnect()

        val result = line.toString()

        val items = JSONObject(result).getJSONArray("items")

        for (i in 0 until items.length())
        {
            themeDataSet.add(
                ThemeData(
                    // img
                    items.getJSONObject(i).getJSONObject("repPhoto").getJSONObject("photoid").getString("imgpath"),
                    // title
                    items.getJSONObject(i).getString("title"),
                    // intro
                    items.getJSONObject(i).getString("introduction"),
                    // address
                    items.getJSONObject(i).getString("address"),
                    // alltag
                    items.getJSONObject(i).getString("alltag"),
                    // region
                    items.getJSONObject(i).getJSONObject("region2cd").getString("label"),
                    // phone
                    items.getJSONObject(i).getString("phoneno")
                )
            )
        }
    }

    inner class SearchThread(category: String): Thread()
    {
        private val cg = category

        override fun run()
        {
            try
            {
                themeDataSet = mutableListOf()
                searchTheme(cg)
            }
            catch (e: IOException) { Log.e("IOThreadError", "SearchThread was not working!: ${e.localizedMessage}") }
            catch (e: JSONException) { Log.e("JSONThreadError", "SearchThread was not working!: ${e.localizedMessage}") }
        }
    }

    inner class SearchBarThread(text: String?): Thread()
    {
        private val title = text

        override fun run()
        {
            try
            {
                themeDataSet = mutableListOf()
                searchBarText(title)
            }
            catch (e: IOException) { Log.e("IOThreadError", "SearchThread was not working!: ${e.localizedMessage}") }
            catch (e: JSONException) { Log.e("JSONThreadError", "SearchThread was not working!: ${e.localizedMessage}") }
        }
    }
}