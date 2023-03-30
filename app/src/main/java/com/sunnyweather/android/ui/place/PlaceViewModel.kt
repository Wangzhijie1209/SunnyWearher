package com.sunnyweather.android.ui.place

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.sunnyweather.android.logic.Repository
import com.sunnyweather.android.logic.model.Place

//ViewModel相当于逻辑层和UI层之间的一个桥梁
class PlaceViewModel : ViewModel() {
    //定义了一个searchPlaces()方法,但是这里并没有直接调用仓库层的searchPlaces()方法,而是将传入的搜索参数赋值给了
    //一个searchLiveData对象 并使用Transformations的switchMap()方法来观察这个对象,否则仓库层返回的LiveData对象将无法观察
    //现在每当searchPlaces()函数被调用时,switchMap()方法所对应的转换函数就会执行,然后在转换函数中,我们只需要调用仓库层中定义的
    //searchPlaces()方法就可以发起网络请求,同时将仓库层返回的LiveData对象转换成一个可供Activity观察的LiveData对象
    //另外还在PlaceViewModel中定义了一个placeList集合,用于对界面上显示的城市数据进行缓存,因为原则上与界面相关的数据都应该放在ViewMode中
    //这样可以保证他们在手机屏幕发生旋转的时候不会丢失,稍后会在编写UI层代码的时候用到这个集合
    private val searchLiveData = MutableLiveData<String>()

    val placeList = ArrayList<Place>()

    //
    val placeLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchPlaces(query)
    }


    fun searchPlaces(query: String) {
        searchLiveData.value = query
    }

    fun savePlace(place: Place) = Repository.savePlace(place)

    fun getSavePlace() = Repository.getSavePlace()
    fun isPlaceSaved() = Repository.isPlaceSaved()
}