package com.sunnyweather.android.logic.network

import com.sunnyweather.android.SunnyWeatherApplication
import com.sunnyweather.android.logic.model.PlaceResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceService {
    //查询全球绝大多数城市的数据信息v2/place?query=北京&token={token}&lang=zh_CN
    //搜索城市数据API中只有query这个参数是需要动态指定的,这里使用@Query注解的方式来进行实现
    @GET("v2/place?token=${SunnyWeatherApplication.TOKEN}&lang=zh_CN")
    //searchPlaces方法的返回值被声明为了 Call<PlaceResponse> 这样Retrofit就会将服务器返回的JSON数据自动解析成PlaceResponse对象了
    fun searchPlaces(@Query("query") query:String):Call<PlaceResponse>
}