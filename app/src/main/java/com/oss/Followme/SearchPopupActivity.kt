package com.oss.followMe

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import com.bumptech.glide.Glide
import com.oss.followMe.databinding.ActivitySearchPopupBinding
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

class SearchPopupActivity(context: Context, _data: ThemeData) : Dialog(context)
{

    private val _searchPopupBinding:ActivitySearchPopupBinding = ActivitySearchPopupBinding.inflate(layoutInflater)
    private val searchPopupBinding get() = _searchPopupBinding

    private val dialog: Dialog = this
    private val dialogParams = dialog.window?.attributes
    private val data: ThemeData = _data

    private val weather = ApiObject.WeatherObject
    private val air = ApiObject.AirObject
    private val enviData = ApiObject.Contents

    private var starCheck = false

    init
    {
        setContentView(searchPopupBinding.root)

        searchPopupBinding.starIcon.setOnClickListener {
            starCheck = if(!starCheck)
            {
                searchPopupBinding.starIcon.setImageResource(R.drawable.able_star)
                true
            }
            else
            {
                searchPopupBinding.starIcon.setImageResource(R.drawable.disable_star)
                false
            }
        }
    }

    fun popupDialog() {
        dataInit()

        val enviThread = Thread(EnviThread(enviData))

        enviThread.start()
        enviThread.join()

        inputWeatherView()

        searchPopupBinding.closePopup.setOnClickListener{ dialog.dismiss() }
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialogParams?.width = 990
        dialogParams?.height = 1440

        dialog.window?.attributes
        dialog.setCancelable(false)

        dialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() { dialog.dismiss() }

    @SuppressLint("SetTextI18n")
    private fun dataInit()
    {
        var tagText = "태그: "
        var phoneNumber = "연락처: "

        tagText += if(data.tag != "null") { "#" + data.tag.replace(","," #") }
        else { "X" }
        phoneNumber += if(data.phoneNumber == "--" || data.phoneNumber == "null") { "X" }
        else { data.phoneNumber.replace("-", ".") }

        Glide.with(searchPopupBinding.root).load(data.img).into(searchPopupBinding.themeImg)

        searchPopupBinding.infoName.text = data.title
        searchPopupBinding.infoIntro.text = data.intro + "\n"
        searchPopupBinding.addressText.text = "주소: " + data.address
        searchPopupBinding.tagText.text = tagText
        searchPopupBinding.phoneNumber.text = phoneNumber

        when(data.region)
        {
            "제주시내" ->
            {
                enviData.locate = "이도동"
                enviData.nx = "52"
                enviData.ny = "38"
            }

            "애월" ->
            {
                enviData.locate = "애월읍"
                enviData.nx = "49"
                enviData.ny = "37"
            }

            "한림" ->
            {
                enviData.locate = "한림읍"
                enviData.nx = "48"
                enviData.ny = "36"
            }

            "한경" ->
            {
                enviData.locate = "한림읍"
                enviData.nx = "46"
                enviData.ny = "35"
            }

            "조천" ->
            {
                enviData.locate = "조천읍"
                enviData.nx = "55"
                enviData.ny = "39"
            }

            "구좌" ->
            {
                enviData.locate = "조천읍"
                enviData.nx = "59"
                enviData.ny = "38"
            }

            "서귀포시내"->
            {
                enviData.locate = "동흥동"
                enviData.nx = "53"
                enviData.ny = "33"
            }

            "성산" ->
            {
                enviData.locate = "성산읍"
                enviData.nx = "60"
                enviData.ny = "37"
            }

            "남원" ->
            {
                enviData.locate = "남원읍"
                enviData.nx = "56"
                enviData.ny = "33"
            }

            "표선" ->
            {
                enviData.locate = "남원읍"
                enviData.nx = "58"
                enviData.ny = "34"
            }

            "중문" ->
            {
                enviData.locate = "강정동"
                enviData.nx = "51"
                enviData.ny = "32"
            }

            "대정" ->
            {
                enviData.locate = "대정읍"
                enviData.nx = "48"
                enviData.ny = "32"
            }

            "안덕" ->
            {
                enviData.locate = "대정읍"
                enviData.nx = "49"
                enviData.ny = "32"
            }

            "추자도" ->
            {
                enviData.locate = "애월읍"
                enviData.nx = "48"
                enviData.ny = "48"
            }

            "우도" ->
            {
                enviData.locate = "성산읍"
                enviData.nx = "60"
                enviData.ny = "38"
            }
        }
    }

    private fun inputWeatherView()
    {
        val vecWsd = "${weather.vec} (${weather.wsd})"
        val pm10 = "${air.pm10} (${air.pm10Grade})"
        val pm25 = "${air.pm25} (${air.pm25Grade})"

        searchPopupBinding.weatherSky.text = weather.sky
        searchPopupBinding.weatherTmp.text = weather.t1h
        searchPopupBinding.weatherReh.text = weather.reh
        searchPopupBinding.weatherRn1.text = weather.rn1
        searchPopupBinding.weatherVecWsd.text = vecWsd

        searchPopupBinding.airPm10.text = pm10
        searchPopupBinding.airPm25.text = pm25
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

    inner class EnviThread(_data: ApiObject.Contents): Thread()
    {
        private val _nx = _data.nx
        private val _ny = _data.ny
        private val _station = _data.locate

        override fun run()
        {
            try
            {
                val date = Date(System.currentTimeMillis() - (System.currentTimeMillis() / 1000 / 60 / 60 * 3))
                val dateFormatDay = SimpleDateFormat("yyyyMMdd", Locale.KOREA).format(date)
                val dateFormatTime = SimpleDateFormat("HH00", Locale.KOREA).format(date)

                weatherData(_nx, _ny, dateFormatDay, dateFormatTime)

                airData(_station)
            }
            catch (e: IOException) { Log.e("IOThreadError", "EnviThread was not working!: ${e.localizedMessage}") }
            catch (e: JSONException) { Log.e("JSONThreadError", "EnviThread was not working!: ${e.localizedMessage}") }
        }
    }
}