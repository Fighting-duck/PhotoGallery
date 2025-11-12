package com.lsy.kotlin_demo2.models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import androidx.paging.toLiveData
import com.lsy.kotlin_demo2.database.NetworkStatus
import com.lsy.kotlin_demo2.database.PixabayDataSourceFactory
import com.lsy.kotlin_demo2.domain.PhotoItem


class GalleryViewModel(
    application: Application
) : AndroidViewModel(application) {
    private val factory = PixabayDataSourceFactory<PhotoItem>(application, PhotoItem::class.java)
    val pagedListLiveData = factory.toLiveData(1)

    // 借助Factory获取DataSource中的networkStatus变量
    val networkStatusLiveData: LiveData<NetworkStatus> = factory.pixabayDataSource.switchMap {
        it.networkStatus
    }

    //val pagedListLiveData  = LivePagedListBuilder(PixabayDataSourceFactory(application), 1).build()
    fun resetQuery() {
        // 重置查询
        pagedListLiveData.value?.dataSource?.invalidate()
    }

    // 重试
    fun retry() {
        factory.pixabayDataSource.value?.retry?.invoke()
    }
}