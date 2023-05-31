package com.oss.followMe

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.database.*
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

class HomeActivity : ComponentActivity(), View.OnClickListener
{
    private lateinit var _binding: ActivityHomeBinding
    private val binding get() = _binding
    private var weather = ApiObject.WeatherObject
    private var air = ApiObject.AirObject
    private var theme = ApiObject.Theme
    private var contents = ApiObject.Contents
    private var contextCheck = false

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.testThemeTravel.setOnClickListener(this)
    }

    @Throws(IOException::class, JSONException::class)
    private fun weatherAPI(baseDate: String, checkTime: String, nx: String, ny: String)
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
        // items 로 부터 itemList 를 받기
        val itemList = JSONObject(items).getJSONArray("item")

        for (i in 0 until itemList.length())
        {
            val fcstValue = itemList.getJSONObject(i).getString("fcstValue")
            val category = itemList.getJSONObject(i).getString("category")

            if (category == "SKY")
            {
                weather.sky = "현재 날씨는 "
                when (fcstValue)
                {
                    "1" -> { weather.sky += "맑은 상태입니다." }
                    "2" -> { weather.sky += "비가 오는 상태입니다." }
                    "3" -> { weather.sky += "구름이 많은 상태입니다." }
                    "4" -> { weather.sky += "흐린 상태입니다." }
                }
            }
            if (category == "T1H") weather.t1h = "현재 기온은 $fcstValue℃입니다."
            if (category == "RN1")
            {
                weather.rn1 = "현재 강수량은 "
                if (fcstValue == "강수없음") weather.rn1 += "없습니다."
                else weather.rn1 += "${fcstValue}입니다."
            }
            if (category == "REH") weather.reh = "현재 습도는 $fcstValue%입니다."
            if (category == "PTY")
            {
                weather.pty = "현재 강수 형태는 "
                when (fcstValue)
                {
                    "0" -> { weather.pty += "없습니다." }
                    "1" -> { weather.pty += "비가 내리고 있습니다." }
                    "2" -> { weather.pty += "비나 눈이 내리고 있습니다." }
                    "3" -> { weather.pty += "눈이 내리고 있습니다." }
                    "5" -> { weather.pty += "빗방울이 내리고 있습니다." }
                    "6" -> { weather.pty += "빗방울 및 눈 날림이 있습니다." }
                    "7" -> { weather.pty += "눈 날림이 있습니다." }
                }
            }
            if (category == "VEC")
            {
                weather.vec = "현재 풍향은 "
                if (fcstValue.toInt() in 0..22) weather.vec += "북풍입니다."
                else if (fcstValue.toInt() in 23..67) weather.vec += "북동풍입니다."
                else if (fcstValue.toInt() in 68..112) weather.vec += "동풍입니다."
                else if (fcstValue.toInt() in 113..157) weather.vec += "동남풍입니다."
                else if (fcstValue.toInt() in 158..202) weather.vec += "남풍입니다."
                else if (fcstValue.toInt() in 203..247) weather.vec += "남서풍입니다."
                else if (fcstValue.toInt() in 248..292) weather.vec += "서풍입니다."
                else if (fcstValue.toInt() in 293..337) weather.vec += "북서풍입니다."
                else if (fcstValue.toInt() in 338..360) weather.vec += "북풍입니다."
            }
            if (category == "WSD") weather.wsd = "현재 풍속은 ${fcstValue}m/s입니다."
        }
    }

    private fun airAPI(stationName: String?)
    {
        val apiUrl = getString(R.string.air_apikey)

        val urlBuilder = StringBuilder(apiUrl)

        // 버전 명 (1.3 버전에 PM_10, PM_2.5 수치 포함)
        urlBuilder.append("&" + URLEncoder.encode("ver", "UTF-8") + "=" + URLEncoder.encode("1.3", "UTF-8"))
        // 데이터 기간
        urlBuilder.append("&" + URLEncoder.encode("dataTerm", "UTF-8") + "=" + URLEncoder.encode("daily", "UTF-8"))
        // 측정소 명
        urlBuilder.append("&" + URLEncoder.encode("stationName", "UTF-8") + "=" + URLEncoder.encode(stationName, "UTF-8"))

        // GET 방식으로 전송해서 파라미터 받아오기
        val url = URL(urlBuilder.toString())

        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.setRequestProperty("Content-type", "application/json")

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
        val items = JSONObject(body).getJSONArray("items")

        // 미세 먼지 농도
        air.pm10 = "미세 먼지 농도는 "
        air.pm10 += items.getJSONObject(0).getString("pm10Value") + "\uFEFF㎍/㎥입니다."
        // 미세 먼지 등급
        air.pm10Grade = "미세 먼지 등급은 "
        when(items.getJSONObject(0).getString("pm10Grade"))
        {
            "1" -> {air.pm10Grade += "좋음 "}
            "2" -> {air.pm10Grade += "보통 "}
            "3" -> {air.pm10Grade += "나쁨 "}
            "4" -> {air.pm10Grade += "매우 나쁨 "}
            "null" -> {air.pm10Grade += " 알 수 없음 "}
        }
        air.pm10Grade += "상태입니다."
        // 초 미세 먼지 농도
        air.pm25 = "초 미세 먼지 농도는 "
        air.pm25 += items.getJSONObject(0).getString("pm25Value") + "\uFEFF㎍/㎥입니다."
        // 초 미세 먼지 등급
        air.pm25Grade = "초 미세 먼지 등급은 "
        when(items.getJSONObject(0).getString("pm25Grade"))
        {
            "1" -> {air.pm25Grade += "좋음 "}
            "2" -> {air.pm25Grade += "보통 "}
            "3" -> {air.pm25Grade += "나쁨 "}
            "4" -> {air.pm25Grade += "매우 나쁨 "}
            "null" -> {air.pm25Grade += "알 수 없음 "}
        }
        air.pm25Grade += "상태입니다."
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
                R.id.testThemeTravel ->
                {
                    // 주소
                    val database = FirebaseDatabase.getInstance().reference.child("Travel").child("CNTS_000000000022466")

                    database.addValueEventListener(object : ValueEventListener
                    {
                        override fun onDataChange(snapshot: DataSnapshot)
                        {

                            theme.intro = snapshot.child("Intro").value.toString()
                            theme.themeName = snapshot.child("ThemeName").value.toString()
                            theme.warning = snapshot.child("Warning").value.toString()

                            createView("theme")

                            for(data in snapshot.children)
                            {
                                if(data.value!!.javaClass.name != String::class.java.name)
                                {
                                    contents.address =  data.child("address").value.toString()
                                    contents.imgSource?.img1 = data.child("img").child("img1").value.toString()
                                    contents.imgSource?.img2 = data.child("img").child("img2").value.toString()
                                    contents.name = data.child("name").value.toString()
                                    contents.locate = data.child("locate").value.toString()
                                    contents.nx = data.child("nx").value.toString()
                                    contents.ny = data.child("ny").value.toString()

                                    createView("contents")

                                    val themeThread = Thread(NetworkThread(contents.nx, contents.ny, contents.locate))
                                    themeThread.start()
                                    themeThread.join()

                                    createView("weather")
                                    createView("air")
                                }
                            }
                        }
                        override fun onCancelled(error: DatabaseError) { Log.e("Firebase Error", "Firebase data read error ${error.toException()}") }
                    })
                }
            }
        }
    }

    private fun createView(viewName: String){
        when (viewName)
        {
            "weather" ->
            {
                Log.i("WeatherViewCreate", "날씨 뷰 생성")

                val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val weatherTextView = TextView(applicationContext)
                val text = weather.sky + "\n" +
                           weather.t1h + "\n" +
                           weather.reh + "\n" +
                           weather.rn1 + "\n" +
                           weather.pty + "\n" +
                           weather.vec + "\n" +
                           weather.wsd + "\n"

                if (contextCheck) { weatherTextView.gravity = Gravity.START }
                else { weatherTextView.gravity = Gravity.END }

                weatherTextView.text = text
                weatherTextView.textSize = 11f
                weatherTextView.setTypeface(null, Typeface.BOLD)
                weatherTextView.id = 0
                weatherTextView.layoutParams = param
                weatherTextView.setBackgroundColor(Color.argb(231, 62, 115, 188))
                weatherTextView.setTextColor(Color.argb(255, 255, 255, 255))
                binding.homeLinear.addView(weatherTextView)
            }

            "air" ->
            {
                Log.i("airTextView", "미세 먼지 뷰 생성")

                val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val airTextView = TextView(applicationContext)
                val text = air.pm10      + "\n" +
                           air.pm10Grade + "\n" +
                           air.pm25      + "\n" +
                           air.pm25Grade + "\n"

                if (contextCheck) { airTextView.gravity = Gravity.START }
                else { airTextView.gravity = Gravity.END }

                airTextView.text = text
                airTextView.textSize = 11f
                airTextView.setTypeface(null, Typeface.BOLD)
                airTextView.id = 0
                airTextView.layoutParams = param
                airTextView.setBackgroundColor(Color.argb(231, 62, 115, 188))
                airTextView.setTextColor(Color.argb(255, 255, 255, 255))
                binding.homeLinear.addView(airTextView)
            }

            "theme" ->
            {
                Log.i("themeTitleView", "테마 뷰 생성")

                val param= LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val intro = TextView(applicationContext)
                val text = theme.intro      + "\n\n" +
                           theme.themeName  + "\n\n" +
                           theme.warning    + "\n"

                intro.gravity = Gravity.CENTER

                intro.text = text
                intro.textSize = 11f
                intro.setTypeface(null, Typeface.BOLD)
                intro.id = 0
                intro.layoutParams = param
                intro.setBackgroundColor(Color.argb(231, 62, 115, 188))
                intro.setTextColor(Color.argb(255, 255, 255, 255))
                binding.homeLinear.addView(intro)
            }

            "contents" ->
            {
                val param = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                val contentsTextView = TextView(applicationContext)
                val text = "이름: " + contents.name    + "\n" +
                           "주소: " + contents.address + "\n"

                if (contextCheck)
                {
                    contentsTextView.gravity = Gravity.END
                    contextCheck = false
                }
                else
                {
                    contentsTextView.gravity = Gravity.START
                    contextCheck = true
                }

                contentsTextView.text = text
                contentsTextView.textSize = 11f
                contentsTextView.setTypeface(null, Typeface.BOLD)
                contentsTextView.id = 0
                contentsTextView.layoutParams = param
                contentsTextView.setBackgroundColor(Color.argb(231, 62, 115, 188))
                contentsTextView.setTextColor(Color.argb(255, 255, 255, 255))
                binding.homeLinear.addView(contentsTextView)
            }
        }
    }

    inner class NetworkThread(nx: String, ny: String, station: String) : Thread()
    {
        private val _nx = nx
        private val _ny = ny
        private val _station = station

        override fun run()
        {
            try
            {
                val date = Date(System.currentTimeMillis() - (System.currentTimeMillis() / 1000 / 60 / 60 * 3))

                val simpleDateFormatDay = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(date)
                val simpleDateFormatTime = SimpleDateFormat("HH00", Locale.KOREA).format(date)
                Log.i("Date + Time: ",simpleDateFormatDay + simpleDateFormatTime)

                weatherAPI(simpleDateFormatDay, simpleDateFormatTime, _nx, _ny)
                airAPI(_station)
            }
            catch (e: IOException) { Log.i("THREE_ERROR1", e.message!!) }
            catch (e: JSONException) { Log.i("THREE_ERROR2", e.message!!) }
        }
    }
}