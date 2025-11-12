package com.lsy.kotlin_demo2.activities

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.databinding.ActivityMainBinding
import com.lsy.kotlin_demo2.models.MainViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding
    private lateinit var mNavController: NavController
    private lateinit var mViewModel: MainViewModel
    // 创建一个fragment to motionLayout的Map
    private val destinationMap by lazy {
        mapOf(
            R.id.galleryFragment to mBinding.home.motionLayoutHome,
            R.id.exploreFragment to mBinding.explore.motionLayoutExplore,
            R.id.mineFragment to mBinding.mine.motionLayoutMine
        )
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.main)
        mViewModel = viewModels<MainViewModel>().value // 获取ViewModel实例(ktx获取)

        // 获取NavController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragment) as NavHostFragment
        mNavController = navHostFragment.navController
        // 设置ActionBar
        setupActionBarWithNavController(
            mNavController,
            AppBarConfiguration(destinationMap.keys)
        )
        // 监听点击
        destinationMap.forEach { (destinationId, motionLayout) ->
            motionLayout.setOnClickListener {
                mNavController.navigate(destinationId)
            }
        }
        // 目的地监听
        mNavController.addOnDestinationChangedListener { controller, destination, arguments ->
            controller.popBackStack() // 将返回栈弹空
            // 恢复初始状态
            destinationMap.values.forEach { motionLayout ->
                motionLayout.progress = 0f
            }
            // 标签同步替换
            destinationMap[destination.id]?.transitionToEnd()
        }
    }
    override fun onResume() {
        super.onResume()
    }
    override fun onStart() {
        super.onStart()
    }

/*    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && ::mBinding.isInitialized) {
            *//*val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            mBinding.linearLayout.visibility = if (isLandscape) View.GONE else View.VISIBLE*//*
            hideSystemUI()
            val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
            mBinding.linearLayout.visibility = if (isLandscape) View.GONE else View.VISIBLE
            // mViewModel.emmitVideoResolution()  // 重新调整视频分辨率
        }
    }*/

    private fun hideSystemUI() {
        window.decorView.setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Hide the nav bar and status bar
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_FULLSCREEN
        )
    }
}