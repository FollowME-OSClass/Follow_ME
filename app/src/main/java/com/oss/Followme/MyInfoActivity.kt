package com.oss.followMe

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.oss.followMe.databinding.ActivityMyInfoBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MyInfoActivity : ComponentActivity(), View.OnClickListener
{

    private lateinit var _myInfoBinding: ActivityMyInfoBinding
    private val myInfoBinding get() = _myInfoBinding

    private var themeDataSet = mutableListOf<ThemeData>()
    private val searchAdapter = SearchAdapter()
    private val userInfo = ApiObject.UserInfo

    private var pressTime: Long = 0
    private val finishedTime: Long = 1000
    private val infoContext = this

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _myInfoBinding = ActivityMyInfoBinding.inflate(layoutInflater)
        setContentView(myInfoBinding.root)

        myInfoBinding.searchTravel.setOnClickListener(this)
        myInfoBinding.themeTravel.setOnClickListener(this)
        myInfoBinding.myInfoTravel.isClickable = false

        infoInit()

        starTheme()
    }

    override fun onClick(v: View?)
    {
        when(v)
        {
            myInfoBinding.searchTravel ->
            {
                val moveSearchActivity = Intent(this, SearchActivity::class.java)
                startActivity(moveSearchActivity)
            }

            myInfoBinding.themeTravel ->
            {
                val moveHomeActivity = Intent(this, HomeActivity::class.java)
                startActivity(moveHomeActivity)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed()
    {
        val checkTime = System.currentTimeMillis()
        val intervalTime = checkTime - pressTime

        if(intervalTime in 0..finishedTime)
        {
            moveTaskToBack(true)
            finish()
            android.os.Process.killProcess(android.os.Process.myPid())
        }
        else
        {
            pressTime = checkTime
            Toast.makeText(applicationContext, "한번 더 누르면 앱이 종료됩니다", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun infoInit()
    {
        Glide.with(myInfoBinding.root).load(userInfo.profileImg).into(myInfoBinding.profileImg)
        myInfoBinding.profileImg.clipToOutline = true
        myInfoBinding.profileNickname.text = "이름: " + userInfo.nickname
        myInfoBinding.profileNickname.setTextColor(Color.argb(255, 0, 0, 0))
        myInfoBinding.profileEmail.text = "이메일: " + userInfo.email
        myInfoBinding.profileEmail.setTextColor(Color.argb(255, 0, 0, 0))
    }

    private fun starTheme()
    {
        val userInfo = ApiObject.UserInfo
        val database = FirebaseDatabase.getInstance().reference.child("Users").child("KakaoLogin").child("kakao"+userInfo.id).child("Favorite")

        database.addValueEventListener(object : ValueEventListener
        {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot)
            {
                for(task in snapshot.children)
                {
                    val getKey = task.key
                    val starThread = StarThread(getKey)

                    starThread.start()
                    starThread.join()

                    val linearLayout = LinearLayoutManager(infoContext)
                    myInfoBinding.infoFavorite.layoutManager = linearLayout

                    myInfoBinding.infoFavorite.removeAllViewsInLayout()

                    searchAdapter.data = themeDataSet
                    searchAdapter.notifyDataSetChanged()

                    myInfoBinding.infoFavorite.adapter = searchAdapter
                }
            }
            override fun onCancelled(error: DatabaseError) { Log.e("Firebase Error", "Firebase data read error ${error.toException()}") }
        })
    }

    private fun favoriteTheme(key: String?)
    {
        val themeUrl = "https://api.visitjeju.net/vsjApi/contents/searchList?apiKey=nueb4lmqst5qc9de&locale=kr"
        val urlBuilder = StringBuilder(themeUrl)

        urlBuilder.append("&" + URLEncoder.encode("cid", "UTF-8") + "=" + URLEncoder.encode(key, "UTF-8"))

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

        themeDataSet.add(
            ThemeData(
                // cId
                items.getJSONObject(0).getString("contentsid"),
                // img
                items.getJSONObject(0).getJSONObject("repPhoto").getJSONObject("photoid").getString("imgpath"),
                // title
                items.getJSONObject(0).getString("title"),
                // intro
                items.getJSONObject(0).getString("introduction"),
                // address
                items.getJSONObject(0).getString("address"),
                // alltag
                items.getJSONObject(0).getString("alltag"),
                // region
                items.getJSONObject(0).getJSONObject("region2cd").getString("label"),
                // phone
                items.getJSONObject(0).getString("phoneno")
            )
        )
    }


    inner class StarThread(_key: String?):Thread()
    {
        private val key = _key
        override fun run()
        {
            try
            {
                favoriteTheme(key)
            }
            catch (e: IOException) { Log.e("IOThreadError", "EnviThread was not working!: ${e.localizedMessage}") }
            catch (e: JSONException) { Log.e("JSONThreadError", "EnviThread was not working!: ${e.localizedMessage}") }
        }
    }
}