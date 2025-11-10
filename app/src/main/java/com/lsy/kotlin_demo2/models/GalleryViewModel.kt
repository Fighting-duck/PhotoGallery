package com.lsy.kotlin_demo2.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.lsy.kotlin_demo2.domain.PhotoItem
import com.lsy.kotlin_demo2.domain.Pixabay
import com.lsy.kotlin_demo2.utils.VolleySingleton
import kotlin.math.ceil

enum class DataStatus {
    LOAD_MORE,  // 加载更多
    NO_MORE, // 没有更多数据
    NETWORK_ERROR // 网络错误
}
/**
 * 获取图片列表的ViewModel
 * @param savedStateHandle 保存状态 进程被杀死恢复状态
 * @param application
 * @constructor
 */
class GalleryViewModel(
    application: Application
): AndroidViewModel(application) {
    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
    val photoListLive: LiveData<List<PhotoItem>>
        get() = _photoListLive
    private val _dataStatusLive: MutableLiveData<DataStatus> = MutableLiveData()
    val dataStatusLive: LiveData<DataStatus>
        get() = _dataStatusLive

    private val keyWord: String = "photos"
    private var currentPage: Int = 1 // 当前页数
    private var totalPage: Int = 1 // 总页数
    private var isNewQuery: Boolean = true
    private var isLoading: Boolean = false
    private var perPage: Int = 50
    public var needToScrollToTop: Boolean = false

    init {
        resetQuery()  // 初始化时默认发起一次请求
    }
    // 重新发起一次请求
    fun resetQuery(){
        currentPage = 1
        totalPage = 1
        isNewQuery = true
        needToScrollToTop = true
        fetchData()
    }
    // 继续加载数据
    fun fetchData(){
        if (isLoading) return
        isLoading = true
        // 如果当前页数大于总页数，则不再请求
        if (currentPage > totalPage)  {
            _dataStatusLive.value = DataStatus.NO_MORE
            return
        }
        val stringRequest = StringRequest(
            Request.Method.GET,
            getUrl(),
            Response.Listener {
                with(Gson().fromJson(it, Pixabay::class.java)) {
                    totalPage = ceil(totalHits.toDouble() / perPage).toInt()  // 想上取整
                    if (isNewQuery) {  // 新查询
                        _photoListLive.value = this.hits.toList()
                    }
                    else {
                        // 追加数据
                        _photoListLive.value = arrayListOf(_photoListLive.value!!,hits.toList()).flatten()
                    }
                }
                _dataStatusLive.value = DataStatus.LOAD_MORE
                isLoading = false
                isNewQuery = false
                currentPage++
            },
            Response.ErrorListener {
                Log.d("error", it.toString())
                _dataStatusLive.value = DataStatus.NETWORK_ERROR
                isLoading = false
            }
        )
        VolleySingleton.Companion.getInstance(getApplication()).requestQueue.add(stringRequest)
    }
    private fun getUrl(): String{
        //return "https://pixabay.com/api/?key=53056162-ba5b9207c72806275dab4c1e2&q=${keyWords.random()}&per_page=100"
        return "https://pixabay.com/api/?key=53056162-ba5b9207c72806275dab4c1e2&q=${keyWord}&per_page=${perPage}&page=${currentPage}}"
    }
    private val keyWords = arrayOf("cat", "dog", "flower", "car", "house","phone", "book", "tree", "sun", "moon")
}