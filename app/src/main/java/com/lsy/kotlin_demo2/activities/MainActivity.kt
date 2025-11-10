package com.lsy.kotlin_demo2.activities

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.databinding.ActivityMainBinding
import com.lsy.kotlin_demo2.fragments.GalleryFragment
import com.lsy.kotlin_demo2.fragments.IllustrationFragment
import com.lsy.kotlin_demo2.fragments.VectorFragment

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mAdapter: FragmentStateAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.main)
        ViewCompat.setOnApplyWindowInsetsListener(
            mBinding.main,
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })
        mAdapter = object: FragmentStateAdapter(this) {
            // 配置Fragment数量
            override fun getItemCount() = 3
            // 配置Fragment 序号和位置相对应
            override fun createFragment(position: Int) =
                when (position) {
                    0 -> GalleryFragment()
                    1 -> IllustrationFragment()
                    else -> VectorFragment()
                }
        }
        mBinding.viewPager2.adapter = mAdapter
        // 关键：设置缓存页数为 Fragment 总数
        mBinding.viewPager2.offscreenPageLimit = 3
        // 恢复上次选中的位置（返回时恢复）
        if (savedInstanceState != null){
            val lastPosition = savedInstanceState.getInt("VIEWPAGER_POSITION",0)
            mBinding.viewPager2.currentItem = lastPosition
        }
    }

    override fun onStart() {
        super.onStart()
        // 配置TabLayout与ViewPage2关联起来
        TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "照片"
                1 -> tab.text = "插画"
                else -> tab.text = "向量"
            }
        }.attach()
        //mBinding.tabLayout.setupWithViewPager2(mBinding.viewPager2)
    }
    // 保存 ViewPager 当前选中位置（跳转 Activity 时触发）
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("VIEWPAGER_POSITION",mBinding.viewPager2.currentItem)
    }
    override fun onResume() {
        super.onResume()
    }
}