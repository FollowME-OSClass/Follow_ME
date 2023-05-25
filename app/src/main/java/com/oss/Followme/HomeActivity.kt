package com.oss.followMe

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.oss.followMe.databinding.ActivityHomeBinding
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : AppCompatActivity(), View.OnClickListener
{
    private lateinit var _binding: ActivityHomeBinding
    private val binding get() = _binding
    private var weather: String? = null
    private var temperature: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.testAPI.setOnClickListener(this)
    }

    @Throws(IOException::class, JSONException::class)
    private fun weatherAPI(baseDate: String, checkTime: String, nx: String, ny: String): String
    {
        val baseTime: String = timeChange(checkTime) // 조회하고 싶은 시간

        val apiUrl = getString(R.string.weather_apikey)

        val urlBuilder = StringBuilder(apiUrl)

        // 경도
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"))
        // 위도
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"))
        // 조회하고 싶은 날짜
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(baseDate, "UTF-8"))
        // 조회하고 싶은 시간 02:00 ~ 3시간 단위
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"))


        // GET 방식으로 전송해서 파라미터 받아오기
        val url = URL(urlBuilder.toString())
        //어떻게 넘어가는지 확인하고 싶으면 아래 출력분 주석 해제

        //println(url);

        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-type", "application/json")

        Log.d("conn_Check", "Conn is: $conn")

        val rd: BufferedReader =
            if (conn.responseCode in 200..300) BufferedReader(InputStreamReader(conn.inputStream))
            else BufferedReader(InputStreamReader(conn.errorStream))

        val line = rd.readLine()

        rd.close()
        conn.disconnect()

        val result = line.toString()

        // response 키를 가지고 데이터를 파싱
        val response = JSONObject(result).getString("response")
        // response 로 부터 body 찾기
        val body = JSONObject(response).getString("body")
        // body 로 부터 items 찾기
        val items = JSONObject(body).getString("items")
        // items로 부터 itemlist 를 받기
        val itemList = JSONObject(items).getJSONArray("item")

        for (i in 0 until itemList.length())
        {
            val fcstValue = itemList.getJSONObject(i).getString("fcstValue")
            val category = itemList.getJSONObject(i).getString("category")
            if (category == "SKY")
            {
                weather = "현재 날씨는 "
                when (fcstValue)
                {
                    "1" -> { weather += "맑은 상태로" }
                    "2" -> { weather += "비가 오는 상태로 " }
                    "3" -> { weather += "구름이 많은 상태로 " }
                    "4" -> { weather += "흐린 상태로 " }
                }
            }
            if (category == "T1H") { temperature = " 기온은 $fcstValue℃" }
        }
        return weather + temperature
    }

    private fun timeChange(time: String): String
    {
        var setTime = time

        setTime = when (setTime)
        {
            "0200", "0300", "0400" -> "0230"
            "0500", "0600", "0700" -> "0530"
            "0800", "0900", "1000" -> "0830"
            "1100", "1200", "1300" -> "1130"
            "1400", "1500", "1600" -> "1430"
            "1700", "1800", "1900" -> "1730"
            "2000", "2100", "2200" -> "2030"
            else -> "2330"
        }
        return setTime
    }

    override fun onClick(v: View?)
    {
        if(v != null)
        {
            when(v.id)
            {
                R.id.testAPI ->
                {
                    val weatherThread = Thread(NetworkThread())
                    weatherThread.start()
                    weatherThread.join()

                    createView("weatherText")
                }
            }
        }
    }

    private fun createView(viewName: String)
    {
        when(viewName)
        {
            "weatherText" ->
            {
                val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val weatherTextView = TextView(applicationContext)

                weatherTextView.text = weather
                weatherTextView.textSize = 11f
                weatherTextView.setTypeface(null, Typeface.BOLD)
                weatherTextView.id = 0
                weatherTextView.layoutParams = param
                weatherTextView.setBackgroundColor(Color.argb(231, 62, 115, 188))
                weatherTextView.setTextColor(Color.argb(255, 255, 255, 255))
                binding.homeLinear.addView(weatherTextView)
            }
        }
    }

    inner class NetworkThread : Thread()
    {

        override fun run()
        {
            try
            {
                // date와 time에 값을 넣어야함, 오늘 날짜 기준으로 넣기!
                // ex) date = "20210722", time = "0500"
                val date = Date(System.currentTimeMillis() - (System.currentTimeMillis() / 1000 / 60 / 60 * 3))

                val simpleDateFormatDay = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(date)
                val simpleDateFormatTime = SimpleDateFormat("HH00", Locale.KOREA).format(date)
                Log.i("Date + Time: ",simpleDateFormatDay + simpleDateFormatTime)

                weather = weatherAPI(simpleDateFormatDay, simpleDateFormatTime, "53", "38")
            }
            catch (e: IOException) { Log.i("THREE_ERROR1", e.message!!) }
            catch (e: JSONException) { Log.i("THREE_ERROR2", e.message!!) }
            Log.i("현재 날씨", weather.toString())
        }
    }
}