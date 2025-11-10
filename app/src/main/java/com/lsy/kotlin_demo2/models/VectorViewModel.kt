package com.lsy.kotlin_demo2.models

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.lsy.kotlin_demo2.domain.PhotoItem
import com.lsy.kotlin_demo2.domain.Pixabay
import com.lsy.kotlin_demo2.utils.VolleySingleton

class VectorViewModel(application: Application): AndroidViewModel(application) {
    private val keyWord: String = "vectors"
    private val _photoListLive = MutableLiveData<List<PhotoItem>>()
    val photoListLive: LiveData<List<PhotoItem>>
        get() = _photoListLive
    public fun fetchData(){
        val stringRequest = StringRequest(
            Request.Method.GET,
            getUrl(),
            Response.Listener {
                _photoListLive.value = Gson().fromJson(it, Pixabay::class.java).hits.toList()
            },
            Response.ErrorListener {
                Log.d("error", it.toString())
                _photoListLive.value = emptyList()
            }
        )
        VolleySingleton.Companion.getInstance(getApplication()).requestQueue.add(stringRequest)
    }
    private fun getUrl(): String{
        return "https://pixabay.com/api/?key=53056162-ba5b9207c72806275dab4c1e2&q=${keyWord}&per_page=100"
    }
    private val keyWords = arrayOf("cat", "dog", "flower", "car", "house","phone", "book", "tree", "sun", "moon")
}