package com.lsy.kotlin_demo2.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.activities.PhotoActivity
import com.lsy.kotlin_demo2.adapters.GalleryAdapter
import com.lsy.kotlin_demo2.databinding.FragmentIllustrationBinding
import com.lsy.kotlin_demo2.databinding.FragmentVectorBinding
import com.lsy.kotlin_demo2.domain.PhotoItem
import com.lsy.kotlin_demo2.models.IllustrationViewModel
import com.lsy.kotlin_demo2.models.VectorViewModel

class VectorFragment : Fragment() {
    private lateinit var mBinding: FragmentVectorBinding
    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var mViewModel: VectorViewModel
    private lateinit var mLayoutManager: StaggeredGridLayoutManager
    private var scrollPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentVectorBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        galleryAdapter = GalleryAdapter()
        mBinding.recycleView.apply {
            adapter = galleryAdapter
            mLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            layoutManager = mLayoutManager
        }
        mViewModel = ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory(requireActivity().application)
        ).get(VectorViewModel::class.java)
        mViewModel.photoListLive.observe(viewLifecycleOwner, {
            galleryAdapter.submitList(it)
            mBinding.swipeLayoutGallery.isRefreshing = false
        })

        mViewModel.photoListLive.value ?: mViewModel.fetchData()

        mBinding.swipeLayoutGallery.setOnRefreshListener {
            mViewModel.fetchData()
        }
    }
    // 恢复状态
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
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
        // 保存列表当前滚动位置
        scrollPosition = mLayoutManager.findFirstVisibleItemPositions(null)[0]
        outState.putInt("SCROLL_POSITION", scrollPosition)
    }
}