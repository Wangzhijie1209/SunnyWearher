package com.sunnyweather.android.ui.place

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.R
import com.sunnyweather.android.databinding.FragmentPlaceBinding
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {

    //使用lazy函数这种懒加载技术来获取PlaceViewModel的实例,允许我们在整个类中随时使用viewModel这个变量,而完全不用关心它何时初始化 是否为空等前提条件
    val viewModel by lazy { ViewModelProvider(this).get(PlaceViewModel::class.java) }

    private lateinit var PlaceBinding: FragmentPlaceBinding

    private lateinit var adapter: PlaceAdapter

    //加载Fragment布局
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        PlaceBinding = FragmentPlaceBinding.inflate(inflater, container, false)

        return PlaceBinding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(viewModel.isPlaceSaved()){
            val place = viewModel.getSavePlace()
            val intent = Intent(context,WeatherActivity::class.java).apply {
                putExtra("location_lng",place.location.lng)
                putExtra("location_lat",place.location.lat)
                putExtra("place_name",place.name)
            }
            startActivity(intent)
            activity?.finish()
            return
        }
        //设置RecyclerView的LayoutManager和适配器
        val layoutManager = LinearLayoutManager(activity)
        PlaceBinding.recyclerView.layoutManager = layoutManager
        //使用PlaceViewModel中的placeList集合作为数据源
        adapter = PlaceAdapter(this, viewModel.placeList)
        PlaceBinding.recyclerView.adapter = adapter

        //监听搜索框的内容变化情况 每当搜索框中的内容发生了改变,我们就获取新的内容,
        PlaceBinding.searchPlaceEdit.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()) {
                viewModel.searchPlaces(content)//然后传递给PlaceViewModel的searchPlaces()方法 就可以发起搜索城市数据的网络请求了
            } else {//当搜索框的内容为空时,就将RecyclerView隐藏起来,将背景图显示出来
                PlaceBinding.recyclerView.visibility = View.GONE
                PlaceBinding.bgImageView.visibility = View.VISIBLE
                viewModel.placeList.clear()
                adapter.notifyDataSetChanged()
            }
        }

        //我们对PlaceViewModel中的placeLiveData对象进行观察,当有任何数据变化时,就会回调到传入的Observer接口实现中
        //然后对回调的数据进行判断
        viewModel.placeLiveData.observe(this, Observer { result ->
            val places = result.getOrNull()
            if (places != null) {//如果数据不为空 就将这些数据添加到PlaceViewModel的placeList集合中
                PlaceBinding.recyclerView.visibility = View.VISIBLE
                PlaceBinding.bgImageView.visibility = View.GONE
                viewModel.placeList.clear()
                viewModel.placeList.addAll(places)
                adapter.notifyDataSetChanged() //通知PlaceAdapter刷新界面
            }else{//如果数据为空,则说明发生了异常,弹出一个Toast提示,并将具体的异常原因打印出来
                Toast.makeText(activity,"未能查询到任何地点",Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }


}