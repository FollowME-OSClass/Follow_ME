package com.oss.followMe

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.oss.followMe.databinding.ActivityPopupBinding
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

class PopupActivity(context: Context, themeName: String?, themeCode: String?) : Dialog(context)
{

    private val _popupBinding: ActivityPopupBinding = ActivityPopupBinding.inflate(layoutInflater)
    private val popupBinding get() = _popupBinding
    private var contents = ApiObject.Contents
    private val weather = ApiObject.WeatherObject
    private val air = ApiObject.AirObject
    private val name = themeName
    private val code = themeCode

    private val dialog: Dialog = this
    private val dialogParams = dialog.window?.attributes

    init
    {
        setContentView(popupBinding.root)
        Log.d("themeCheck", "$code, $name")
    }

    fun popupDialog()
    {
        val database = FirebaseDatabase.getInstance().reference.child("Travel").child(code.toString()).child(name.toString())

        database.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(snapshot: DataSnapshot)
            {
                Glide.with(popupBinding.root).load(snapshot.child("img").value).into(popupBinding.themeImg)

                contents.intro = snapshot.child("intro").value.toString()
                contents.info = snapshot.child("info").value.toString()
                contents.address = snapshot.child("address").value.toString()
                contents.name = snapshot.child("name").value.toString()
                contents.locate = snapshot.child("locate").value.toString()
                contents.nx = snapshot.child("nx").value.toString()
                contents.ny = snapshot.child("ny").value.toString()

                val enviData = Thread(EnviThread(contents.nx, contents.ny, contents.locate))

                enviData.start()
                enviData.join()

                inputPopupView()

                popupBinding.closePopup.setOnClickListener{ dialog.dismiss() }
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                dialogParams?.width = 990
                dialogParams?.height = 1440

                dialog.window?.attributes
                setCancelable(false)
                dialog.show()
            }

            override fun onCancelled(error: DatabaseError)
            {
                Log.e("dataError", "Firebase DataError")
            }
        })
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { dialog.dismiss() }

    private fun inputPopupView()
    {
        val introText = " " + contents.intro + "\n"
        val addressText = "주소: " + contents.address

        val vecWsd = "${weather.vec} (${weather.wsd})"

        val pm10 = "${air.pm10} (${air.pm10Grade})"
        val pm25 = "${air.pm25} (${air.pm25Grade})"

        popupBinding.infoTitle.text = contents.info
        popupBinding.infoName.text = contents.name
        popupBinding.infoIntro.text = introText
        popupBinding.addressText.text = addressText

        popupBinding.weatherSky.text = weather.sky
        popupBinding.weatherTmp.text = weather.t1h
        popupBinding.weatherReh.text = weather.reh
        popupBinding.weatherRn1.text = weather.rn1
        popupBinding.weatherVecWsd.text = vecWsd

        popupBinding.airPm10.text = pm10
        popupBinding.airPm25.text = pm25
    }

    @Throws(IOException::class, JSONException::class)
    private fun weatherData(nx: String, ny: String, date: String, time: String)
    {
        val baseTime = timeChange(time) // 조회하고 싶은 시간
        val weatherUrl = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtFcst?serviceKey=b3Bwbi39PobvWYcuGGwcD%2F9Kpo%2BODkKI9ud8dCf29X8pqNRfQ8sPfm3PUKJplSSsfcUxCQmUF3iIa1mIVuknCQ%3D%3D&dataType=JSON&numOfRows=60"
        val urlBuilder = StringBuilder(weatherUrl)

        // 경도
        urlBuilder.append("&" + URLEncoder.encode("nx", "UTF-8") + "=" + URLEncoder.encode(nx, "UTF-8"))
        // 위도
        urlBuilder.append("&" + URLEncoder.encode("ny", "UTF-8") + "=" + URLEncoder.encode(ny, "UTF-8"))
        // 조회하고 싶은 날짜
        urlBuilder.append("&" + URLEncoder.encode("base_date", "UTF-8") + "=" + URLEncoder.encode(date, "UTF-8"))
        // 조회하고 싶은 시간 02:00 ~ 3시간 단위
        urlBuilder.append("&" + URLEncoder.encode("base_time", "UTF-8") + "=" + URLEncoder.encode(baseTime, "UTF-8"))

        val urlWeather = URL(urlBuilder.toString())
        val weatherConnect: HttpURLConnection = urlWeather.openConnection() as HttpURLConnection

        weatherConnect.requestMethod = "GET"
        weatherConnect.setRequestProperty("Content-type", "application/json")

        val br: BufferedReader =
            if (weatherConnect.responseCode in 200..300) BufferedReader(InputStreamReader(weatherConnect.inputStream))
            else BufferedReader(InputStreamReader(weatherConnect.errorStream))

        Log.d("brCheck", br.toString())

        val line = br.readLine()

        br.close()
        weatherConnect.disconnect()

        val result = line.toString()

        val response = JSONObject(result).getString("response")
        val body = JSONObject(response).getString("body")
        val items = JSONObject(body).getString("items")
        val itemList = JSONObject(items).getJSONArray("item")

        for(i in 0 until itemList.length())
        {
            val fcstValue = itemList.getJSONObject(i).getString("fcstValue")
            val category  = itemList.getJSONObject(i).getString("category")

            if (category == "SKY")
            {
                weather.sky = "날씨: "
                when (fcstValue)
                {
                    "1" -> { weather.sky += "맑은 상태" }
                    "2" -> { weather.sky += "비가 오는 상태" }
                    "3" -> { weather.sky += "구름이 많은 상태" }
                    "4" -> { weather.sky += "흐린 상태" }
                }
            }
            if (category == "T1H") weather.t1h = "기온: $fcstValue℃"
            if (category == "RN1")
            {
                weather.rn1 = "강수량: "
                if (fcstValue == "강수없음") weather.rn1 += fcstValue
                else weather.rn1 += "${fcstValue}mm"
            }
            if (category == "REH") weather.reh = "습도: $fcstValue%"

            if (category == "VEC")
            {
                weather.vec = "풍향: "
                if (fcstValue.toInt() in 0..22) weather.vec += "북풍"
                else if (fcstValue.toInt() in 23..67) weather.vec += "북동풍"
                else if (fcstValue.toInt() in 68..112) weather.vec += "동풍"
                else if (fcstValue.toInt() in 113..157) weather.vec += "동남풍"
                else if (fcstValue.toInt() in 158..202) weather.vec += "남풍"
                else if (fcstValue.toInt() in 203..247) weather.vec += "남서풍"
                else if (fcstValue.toInt() in 248..292) weather.vec += "서풍"
                else if (fcstValue.toInt() in 293..337) weather.vec += "북서풍"
                else if (fcstValue.toInt() in 338..360) weather.vec += "북풍"
            }
            if (category == "WSD") weather.wsd = "풍속: ${fcstValue}m/s"
        }
    }

    @Throws(IOException::class, JSONException::class)
    private fun airData(station: String)
    {
        val airUrl = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/getMsrstnAcctoRltmMesureDnsty?serviceKey=b3Bwbi39PobvWYcuGGwcD%2F9Kpo%2BODkKI9ud8dCf29X8pqNRfQ8sPfm3PUKJplSSsfcUxCQmUF3iIa1mIVuknCQ%3D%3D&returnType=json&numOfRows=30"
        val urlBuilder = StringBuilder(airUrl)

        urlBuilder.append("&" + URLEncoder.encode("ver", "UTF-8") + "=" + URLEncoder.encode("1.3", "UTF-8"))
        urlBuilder.append("&" + URLEncoder.encode("dataTerm", "UTF-8") + "=" + URLEncoder.encode("daily", "UTF-8"))
        urlBuilder.append("&" + URLEncoder.encode("stationName", "UTF-8") + "=" + URLEncoder.encode(station, "UTF-8"))

        val urlAir = URL(urlBuilder.toString())
        val airConnect: HttpURLConnection = urlAir.openConnection() as HttpURLConnection
        airConnect.requestMethod = "GET"
        airConnect.setRequestProperty("Content-type", "application/json")

        val br: BufferedReader =
            if (airConnect.responseCode in 200..300) BufferedReader(InputStreamReader(airConnect.inputStream))
            else BufferedReader(InputStreamReader(airConnect.errorStream))

        val line = br.readLine()

        br.close()
        airConnect.disconnect()

        val result = line.toString()

        val response = JSONObject(result).getString("response")
        val body = JSONObject(response).getString("body")
        val items = JSONObject(body).getJSONArray("items")

        air.pm10 = "미세 먼지: "
        air.pm10 += items.getJSONObject(0).getString("pm10Value") + "\uFEFF㎍/㎥"
        when(items.getJSONObject(0).getString("pm10Grade"))
        {
            "1"     -> {air.pm10Grade = "좋음"}
            "2"     -> {air.pm10Grade = "보통"}
            "3"     -> {air.pm10Grade = "나쁨"}
            "4"     -> {air.pm10Grade = "매우 나쁨"}
            "null"  -> {air.pm10Grade = "알 수 없음"}
        }

        air.pm25 = "초 미세 먼지: "
        air.pm25 += items.getJSONObject(0).getString("pm25Value") + "\uFEFF㎍/㎥"
        when(items.getJSONObject(0).getString("pm25Grade"))
        {
            "1"     -> {air.pm25Grade = "좋음"}
            "2"     -> {air.pm25Grade = "보통"}
            "3"     -> {air.pm25Grade = "나쁨"}
            "4"     -> {air.pm25Grade = "매우 나쁨"}
            "null"  -> {air.pm25Grade = "알 수 없음"}
        }
    }

    private fun timeChange(time: String): String
    {
        var setTime = time

        setTime = when (setTime)
        {
            "0300", "0400", "0500" -> "0230"
            "0600", "0700", "0800" -> "0530"
            "0900", "1000", "1100" -> "0830"
            "1200", "1300", "1400" -> "1130"
            "1500", "1600", "1700" -> "1430"
            "1800", "1900", "2000" -> "1730"
            "2100", "2200", "2300" -> "2030"
            else -> "2330"
        }
        return setTime
    }

    inner class EnviThread(nx: String, ny: String, station: String): Thread()
    {
        private val _nx = nx
        private val _ny = ny
        private val _station = station

        override fun run()
        {
            try
            {
                val date = Date(System.currentTimeMillis() - (System.currentTimeMillis() / 1000 / 60 / 60 * 3))
                val dateFormatDay = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(date)
                val dateFormatTime = SimpleDateFormat("HH00", Locale.KOREA).format(date)

                Log.i("Date + Time: ",dateFormatDay + dateFormatTime)

                weatherData(_nx, _ny, dateFormatDay, dateFormatTime)

                airData(_station)
            }
            catch (e: IOException) { Log.e("IOThreadError", "EnviThread was not working!: ${e.localizedMessage}") }
            catch (e: JSONException) { Log.e("JSONThreadError", "EnviThread was not working!: ${e.localizedMessage}") }
        }
    }
}