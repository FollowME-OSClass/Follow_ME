package com.oss.followMe

class ApiObject()
{
    object WeatherObject
    {
        // 기온
        var t1h: String? = null
        // 강수량
        var rn1: String? = null
        // 하늘 상태
        var sky: String? = null
        // 습도
        var reh: String? = null
        // 풍향
        var vec: String? = null
        // 풍속
        var wsd: String? = null
    }
    object AirObject
    {
        // 미세 먼지 농도
        var pm10: String? = null
        // 미세 먼지 등급
        var pm10Grade: String? = null
        // 초 미세 먼지 농도
        var pm25: String? = null
        // 초 미세 먼지 등급
        var pm25Grade: String? = null
    }
    object Contents
    {
        var firImg:     String = ""
        var secImg:     String = ""

        var info:       String = ""
        var intro:      String = ""
        var address:    String = ""
        var locate:     String = ""
        var name:       String = ""
        var nx:         String = ""
        var ny:         String = ""
    }
    object Theme
    {
        var intro:      String? = null
        var themeName:  String? = null
        var warning:    String? = null
    }
}