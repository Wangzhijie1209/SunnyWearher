package com.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

/**
 * status 代表请求的状态 ok表示成功
 * places 是一个JSON数组,会包含几个与我们查询的关键字关系度比较高的地区信息
 * 其中name表示该地区的名字 location表示该地区的经纬度
 * formatted_address表示该地区的地址
 */
data class PlaceResponse(val status: String, val places: List<Place>)
data class Place(
    val name: String,
    val location: Location,
    @SerializedName("formatted_address") val address: String //这里使用了@SerializedName注解的方式,来让JSON和Kotlin字段之间建立映射关系
)
data class Location(val lng:String,val lat:String)
