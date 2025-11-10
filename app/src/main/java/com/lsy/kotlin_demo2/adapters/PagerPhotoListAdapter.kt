package com.lsy.kotlin_demo2.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.chrisbanes.photoview.PhotoView
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.domain.PhotoItem

// PagerPhotoListAdapter.kt 文件
class PagerPhotoListAdapter : ListAdapter<PhotoItem, PagerPhotoListAdapter.MyViewHolder>(DiffCallback) {

    // 嵌套 + private：仅当前 Adapter 内部可见，与其他文件的 MyViewHolder 不冲突
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val photoView: PhotoView = itemView.findViewById(R.id.pagerPhoto)
    }

    object DiffCallback : DiffUtil.ItemCallback<PhotoItem>() {
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.pager_photo_view, parent, false)
        return MyViewHolder(view) // 直接使用内部类
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(holder.itemView)
            .load(getItem(position).previewURL) // 注意：这里漏了具体字段（如 previewURL）
            .placeholder(R.drawable.ic_photo_gray_24dp)
            .into(holder.photoView)
    }
}