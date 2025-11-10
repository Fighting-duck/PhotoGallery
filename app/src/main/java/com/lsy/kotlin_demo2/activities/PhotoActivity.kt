package com.lsy.kotlin_demo2.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.get
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.adapters.PagerPhotoListAdapter
import com.lsy.kotlin_demo2.databinding.ActivityPhotoBinding
import com.lsy.kotlin_demo2.domain.PhotoItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream

const val REQUEST_WRITE_EXTERNAL_STORAGE = 1
class PhotoActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityPhotoBinding
    private var position: Int = 0
    private lateinit var photoList: ArrayList<PhotoItem>
    private lateinit var context: Context
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
        position = intent.getIntExtra("PHOTO_INDEX",0)
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
        mBinding.viewPager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setPhotoTag(position)
            }
        })
        mBinding.btnSave.setOnClickListener {
            // 申请权限
            if(Build.VERSION.SDK_INT<29 && ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_WRITE_EXTERNAL_STORAGE)
            }else{
                savePhoto()
            }
        }
    }

    fun setPhotoTag(position: Int) {
        mBinding.photoTag.text = getString(R.string.photo_tag,position+1,photoList.size)
    }

    fun savePhoto() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                // 1. 获取图片（需在主线程）
                var bitmap: Bitmap? = null
                withContext(Dispatchers.Main) {
                    val recyclerView = mBinding.viewPager2.get(0) as? RecyclerView
                    val holder =
                        recyclerView?.findViewHolderForLayoutPosition(mBinding.viewPager2.currentItem)
                                as? PagerPhotoListAdapter.MyViewHolder
                    bitmap = holder?.photoView?.drawable?.toBitmap()
                }

                if (bitmap == null) {
                    // 2. 显示 Toast：切换到主线程 + 用正确 Context
                    MainScope().launch {
                        Toast.makeText(context, "无法获取图片", Toast.LENGTH_SHORT).show()
                    }
                    return@withContext
                }

                // 3. 生成保存路径
                val saveUri = context.contentResolver.insert(
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

                // 4. 保存图片
                context.contentResolver.openOutputStream(saveUri)?.use { outputStream ->
                    val success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    MainScope().launch {
                        if (success) {
                            Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "保存失败：写入失败", Toast.LENGTH_SHORT).show()
                        }
                    }
                } ?: run {
                    MainScope().launch {
                        Toast.makeText(context, "保存失败：无法打开输出流", Toast.LENGTH_SHORT).show()
                    }
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
        when(requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    savePhoto()
                }
                else {
                    // 权限被拒绝
                    Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
