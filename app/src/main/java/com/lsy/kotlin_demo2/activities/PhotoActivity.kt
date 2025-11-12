package com.lsy.kotlin_demo2.activities

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.adapters.PagerPhotoListAdapter
import com.lsy.kotlin_demo2.databinding.ActivityPhotoBinding
import com.lsy.kotlin_demo2.domain.PhotoItem
import com.lsy.kotlin_demo2.utils.ImageDownloader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val REQUEST_WRITE_EXTERNAL_STORAGE = 1

class PhotoActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityPhotoBinding
    private var position: Int = 0  // 当前图片索引
    private lateinit var photoList: ArrayList<PhotoItem> // 图片列表
    private lateinit var context: Context // 上下文
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        mBinding = ActivityPhotoBinding.inflate(layoutInflater)
        context = this
        setContentView(mBinding.root)
        ViewCompat.setOnApplyWindowInsetsListener(mBinding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        position = intent.getIntExtra("PHOTO_INDEX", 0)
        photoList = intent.getParcelableArrayListExtra<PhotoItem>("PHOTO_LIST") ?: arrayListOf()
    }

    override fun onStart() {
        super.onStart()
        PagerPhotoListAdapter().apply {
            mBinding.viewPager2.adapter = this
            submitList(photoList)
        }
        setPhotoTag(position)
        mBinding.viewPager2.setCurrentItem(position, false)
        // 水平滚动
        //mBinding.viewPager2.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        // 垂直滚动
        mBinding.viewPager2.orientation = ViewPager2.ORIENTATION_VERTICAL
        // 监听滑动
        mBinding.viewPager2.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setPhotoTag(position)
            }
        })
        mBinding.btnSave.setOnClickListener {
            // 申请权限
            if (Build.VERSION.SDK_INT < 29 && ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_EXTERNAL_STORAGE
                )
            } else {
                savePhoto()
            }
        }
    }

    fun setPhotoTag(position: Int) {
        mBinding.photoTag.text = getString(R.string.photo_tag, position + 1, photoList.size)
    }

    fun savePhoto() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                //1. 获取图片
                val photoItem = photoList[position]
                // 2. 生成保存路径
                val saveUri: Uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    ContentValues().apply {
                        put(
                            MediaStore.Images.Media.DISPLAY_NAME,
                            "photo_${System.currentTimeMillis()}.png"
                        )
                        put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    }
                ) ?: run {
                    MainScope().launch {
                        Toast.makeText(context, "保存失败：无法创建路径", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }
                // 3. 下载图片
                ImageDownloader(context).download(photoItem.fullUrl, saveUri) { result ->
                    result.fold(
                        onSuccess = { uri ->
                            Toast.makeText(context, "保存成功: $uri", Toast.LENGTH_SHORT).show()
                        },
                        onFailure = { e ->
                            Toast.makeText(context, "失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        when (requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savePhoto()
                } else {
                    // 权限被拒绝
                    Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
