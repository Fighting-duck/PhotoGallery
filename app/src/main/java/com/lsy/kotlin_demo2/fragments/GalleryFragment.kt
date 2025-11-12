package com.lsy.kotlin_demo2.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lsy.kotlin_demo2.adapters.GalleryAdapter
import com.lsy.kotlin_demo2.database.NetworkStatus
import com.lsy.kotlin_demo2.databinding.FragmentGalleryBinding
import com.lsy.kotlin_demo2.interfaces.OnFooterItemClickListener
import com.lsy.kotlin_demo2.models.GalleryViewModel

class GalleryFragment : Fragment() {
    private lateinit var mBinding: FragmentGalleryBinding
    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var mViewModel: GalleryViewModel
    private lateinit var mLayoutManager: StaggeredGridLayoutManager
    private lateinit var mContext: Context
    private var scrollPosition: Int = 0 // 记录滑动位置

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("GalleryFragment", "onCreate")
        super.onCreate(savedInstanceState)
        mViewModel = viewModels<GalleryViewModel>().value // 获取ViewModel实例(ktx获取)
        mContext = requireContext()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("GalleryFragment", "onCreateView")
        mBinding = FragmentGalleryBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("GalleryFragment", "onViewCreated")
        galleryAdapter = GalleryAdapter(mViewModel::retry)
        mBinding.recycleView.apply {
            adapter = galleryAdapter
            // 不整齐交错的网格
            mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            layoutManager = mLayoutManager
        }

        // 观察数据
        mViewModel.pagedListLiveData.observe(viewLifecycleOwner, Observer {
            galleryAdapter.submitList(it)
        })
        mViewModel.networkStatusLiveData.observe(viewLifecycleOwner, Observer {
            Log.d("networkStatus", it.toString())
            galleryAdapter.updateNetworkStatus(it)
            mBinding.swipeLayoutGallery.isRefreshing = it == NetworkStatus.LOADING
        })

        // 下拉刷新
        mBinding.swipeLayoutGallery.setOnRefreshListener {
            mViewModel.resetQuery()
        }
        // recycleView底部点击
        galleryAdapter.setFooterListener(object : OnFooterItemClickListener {
            override fun onItemClick() {
                mViewModel.retry()
            }
        })
        // 给recycleView做一个滚动监听
        mBinding.recycleView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            // 发生滑动就会被调用
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy < 0) { // 向上滑动
                    return
                }
                val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                val intArray = IntArray(2)
                // 当最后一个item完全可见
                layoutManager.findLastVisibleItemPositions(intArray)
                if (intArray[0] == galleryAdapter.itemCount - 1) { // 最后一个item完全可见

                }
            }

            override fun onScrollStateChanged(
                recyclerView: RecyclerView,
                newState: Int
            ) {
                // 三种状态 0（手指离开） 1（缓慢滑动） 2（快速滑动）
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
    }

    // 恢复状态
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d("GalleryFragment", "onViewStateRestored")
        if (savedInstanceState != null) {
            scrollPosition = savedInstanceState.getInt("SCROLL_POSITION", 0)
            mBinding.recycleView.post {
                mLayoutManager.scrollToPosition(scrollPosition)
            }
        }
    }

    // 保存状态
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d("GalleryFragment", "onSaveInstanceState")
        // 保存列表当前滚动位置
        scrollPosition = mLayoutManager.findFirstVisibleItemPositions(null)[0]
        outState.putInt("SCROLL_POSITION", scrollPosition)
    }
}