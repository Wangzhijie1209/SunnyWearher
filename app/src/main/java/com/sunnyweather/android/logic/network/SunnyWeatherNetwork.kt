package com.sunnyweather.android.logic.network

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.await
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * 单例类 网络数据源访问入口,对所有网络请求的API进行封装
 */
object SunnyWeatherNetwork {

    private val weatherService = ServiceCreator.create(WeatherService::class.java)
    suspend fun getDailyWeather(lng: String, lat: String) =
        weatherService.getDailyWeather(lng, lat).await()
    suspend fun getRealtimeWeather(lng: String, lat: String) =
        weatherService.getRealtimeWeather(lng, lat).await()

    //首先使用ServiceCreator创建了一个PlaceService接口的动态代理对象
    private val placeService = ServiceCreator.create(PlaceService::class.java)
    //然后定义了一个searchPlaces()函数,并在这里调用刚刚在PlaceService接口中定义的searchPlaces()方法,发起搜索城市数据请求
    //声明为挂起函数
    suspend fun searchPlaces(query: String) = placeService.searchPlaces(query).await()


    //await()函数仍然是一个挂起函数,然后我们给他声明了一个泛型T,并将await()函数定义成了Call<T>的扩展函数,这样
    //所有返回值是Call类型的Retrofit网络请求接口就可以直接调用await()函数了
    //当外部调用SunnyWeatherNetwork的searchPlaces()函数时,Retrofit就会立即发起网络请求,同时当前线程也会被阻塞住,直到服务器响应我们的请求之后,await()函数会
    //将解析出阿里的数据模型对象取出并返回,同时恢复当前协程的执行,searchPlaces()函数在得到await()函数的返回值后会将该数据在返回到上一层
    private suspend fun <T> Call<T>.await(): T {
        return suspendCoroutine { continuation ->
            enqueue(object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    if (body != null) continuation.resume(body)
                    else continuation.resumeWithException(RuntimeException("response body is null"))
                }

                override fun onFailure(call: Call<T>, t: Throwable) {
                    continuation.resumeWithException(t)
                }
            })
        }
    }

}