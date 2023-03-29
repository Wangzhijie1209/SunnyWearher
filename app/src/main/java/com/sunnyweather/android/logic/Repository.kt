package com.sunnyweather.android.logic

import androidx.lifecycle.liveData
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers

/**
 * 单例类 作为仓库层的统一封装入口
 * 仓库层的主要工作就是判断调用方请求的数据应该是从本地数据源中获取还是从网络数据源中获取,并将获得的数据
 * 返回给调用方
 * 仓库层有点像是一个数据获取与缓存的中间层,在本地没有缓存的情况下就去网络层获取,如果本地有缓存 就直接将缓存数据返回
 */
object Repository {
    fun searchPlaces(query:String) = liveData(Dispatchers.IO){
        val result = try {
            val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
          if(placeResponse.status =="ok"){
              val places = placeResponse.places
              Result.success(places)
          }else{
              Result.failure(RuntimeException("response status is ${placeResponse.status}"))
          }
        }catch (e:java.lang.Exception){
            Result.failure<List<Place>>(e)
        }
        emit(result)
    }
}