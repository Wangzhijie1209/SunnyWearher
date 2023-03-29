package com.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit构建器
 * 使用object关键字让ServiceCreator成为了一个单例类,并在它内部定义了一个BASE_URL常量,用于指定Retrofit的根路径
 * 然后在内部使用Retrofit.Builder构建一个Retrofit对象,这些都是用private修饰符来修饰的,对于外部来说他们都是不可见的
 */
object ServiceCreator {
    private const val BASE_URL = "https://api.caiyunapp.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    //提供一个外部可见的create()方法,并接收一个Class类型的参数,当在外部调用这个方法时,实际上就是调用了Retrofit的create()方法
    //从而创建出相应Service接口的动态代理对象
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    //泛型实化:首先该函数必须是内联函数才行,也就是要用inline关键字来修饰该函数,其次,在声明泛型的地方必须加上reified关键字来表示该泛型要进行实化
    //定义了一个不带参数的create()方法,并使用inline关键字来修饰方法,使用reified关键字来修饰泛型,这是泛型实化的两大前提条件
    //接下来就可以使用T::class.java这种语法了,这里调用刚才定义的带有Class参数的create()方法即可  11.6.3
    inline fun <reified T> create(): T = create(T::class.java)
}