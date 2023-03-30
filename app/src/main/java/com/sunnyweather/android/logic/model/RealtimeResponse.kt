package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

/**
 * 查看具体的天气信息
 * https://api.caiyunapp.com/v2.5/{token}/116.4073963,39.9041999/realtime.json
 */
data class RealtimeResponse(val status: String, val result: Result) {
    data class Result(val realtime: Realtime)
    data class Realtime(val skycon: String, val temperature: Float,
                        @SerializedName("air_quality") val airQuality: AirQuality)
    data class AirQuality(val aqi: AQI)
    data class AQI(val chn: Float)
}