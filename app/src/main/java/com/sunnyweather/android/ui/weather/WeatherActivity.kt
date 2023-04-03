package com.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.media.metrics.Event
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherActivity : AppCompatActivity() {

    private lateinit var WeatherBinding: ActivityWeatherBinding
    val viewModel by lazy { ViewModelProvider(this).get(WeatherViewModel::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WeatherBinding = ActivityWeatherBinding.inflate(layoutInflater)

        //EventBus注册事件
        EventBus.getDefault().register(this)

        //让背景图和状态栏融合到一起
        val decorView = window.decorView
        decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        window.statusBarColor = Color.TRANSPARENT
        setContentView(WeatherBinding.root)

        //从intent中取出经纬度坐标和地区名称 并赋值到WeatherViewModel的相应变量中
        if (viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }

        if (viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }
        if (viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
            WeatherBinding.swipeRefresh.isRefreshing = false
        })

        //设置下拉刷新进度条的颜色
        WeatherBinding.swipeRefresh.setColorSchemeResources(com.google.android.material.R.color.design_default_color_primary)
        refreshWeather()
        //设置下拉刷新监听器,当触发监听器时 就在监听器中回调refreshWeather()方法来刷新天气信息
        WeatherBinding.swipeRefresh.setOnRefreshListener { refreshWeather() }

        //在切换城市的按钮的点击事件中调用drawerLayout的openDrawer()方法来打开滑动菜单
        WeatherBinding.nowInclude.navBtn.setOnClickListener {
            WeatherBinding.drawerLayout.openDrawer(GravityCompat.START)
        }
        //监听DrawerLayout的状态,当滑动菜单被隐藏的时候,同时也要隐藏输入法,
        WeatherBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener{
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}

            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken,InputMethodManager.HIDE_NOT_ALWAYS)
            }

            override fun onDrawerOpened(drawerView: View) {}
        })


    }

    //刷新天气信息
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)
        WeatherBinding.swipeRefresh.isRefreshing = true//让下拉进度条显示出来
    }

    //  WeatherBinding.forecastInclude
    //  WeatherBinding.lifeIndexInclude
    //  WeatherBinding.nowInclude
    private fun showWeatherInfo(weather: Weather) {
        WeatherBinding.nowInclude.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily
        //填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()}℃"
        WeatherBinding.nowInclude.currentTemp.text = currentTempText
        WeatherBinding.nowInclude.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        WeatherBinding.nowInclude.currentAQI.text = currentPM25Text
        WeatherBinding.nowInclude.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        //填充forecast.xml布局中的数据
        WeatherBinding.forecastInclude.forecastLayout.removeAllViews()

        //处理每天的天气信息 动态加载forecast_item.xml布局并设置数据 然后添加到父布局
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(
                R.layout.forecast_item,
                WeatherBinding.forecastInclude.forecastLayout,
                false
            )
            val dataInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dataInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()}℃"
            temperatureInfo.text = tempText
            WeatherBinding.forecastInclude.forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        //生活指数方面虽然服务器会返回很多天的数据,但是界面上只需要当天的数据就可以了
        //因此这里我们对所有的生活指数都取了下标为零的那个元素的数据
        val lifeIndex = daily.lifeIndex
        WeatherBinding.lifeIndexInclude.coldRiskText.text = lifeIndex.coldRisk[0].desc
        WeatherBinding.lifeIndexInclude.dressingText.text = lifeIndex.dressing[0].desc
        WeatherBinding.lifeIndexInclude.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        WeatherBinding.lifeIndexInclude.carWashingText.text = lifeIndex.carWashing[0].desc
        WeatherBinding.weatherLayout.visibility = View.VISIBLE
    }

    //                holder.drawerLayout.closeDrawers()
//                activity.viewModel.locationLng = place.location.lng
//                activity.viewModel.locationLat = place.location.lat
//                activity.viewModel.placeName = place.name

    //                activity.refreshWeather()
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(messageEvent:com.sunnyweather.android.ui.place.Place){
        WeatherBinding.drawerLayout.closeDrawers()
        viewModel.locationLat = messageEvent.lat
        viewModel.locationLng = messageEvent.lng
        viewModel.placeName = messageEvent.name
        refreshWeather()
    }



    override fun onDestroy() {
        super.onDestroy()
        //解开注解
        EventBus.getDefault().unregister(this)
    }
}