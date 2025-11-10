package com.lsy.kotlin_demo2.adapters

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.activities.PhotoActivity
import com.lsy.kotlin_demo2.domain.PhotoItem
import com.lsy.kotlin_demo2.models.DataStatus
import io.supercharge.shimmerlayout.ShimmerLayout

interface OnFooterItemClickListener {
    fun onItemClick()
}

class GalleryAdapter : ListAdapter<PhotoItem, RecyclerView.ViewHolder>(DiffCallback) {
    companion object {
        private const val NORMAL_VIEW_TYPE = 0  // 正常item
        private const val FOOTER_VIEW_TYPE = 1  // 加载更多 item
    }

    var footViewStatus: DataStatus = DataStatus.LOAD_MORE
    var footerItemClickListener: OnFooterItemClickListener? = null
    fun setFooterListener(listener: OnFooterItemClickListener) {
        footerItemClickListener = listener
    }

    object DiffCallback : DiffUtil.ItemCallback<PhotoItem>() {
        // 判断是否是同一个item
        override fun areItemsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem.photoId == newItem.photoId
        }

        // 判断内容是否相同
        override fun areContentsTheSame(oldItem: PhotoItem, newItem: PhotoItem): Boolean {
            return oldItem == newItem
        }
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val shimmerLayout: ShimmerLayout = itemView.findViewById(R.id.shimmerLayoutCell)
        val photoUser: TextView = itemView.findViewById(R.id.tv_User)
        val photoLikes: TextView = itemView.findViewById(R.id.tv_Likes)
        val photoFavorites: TextView = itemView.findViewById(R.id.tv_favorites)

        init {
            shimmerLayout.apply {
                setShimmerColor(0x55FFFFFF)
                setShimmerAngle(0)
            }
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tv_load_more: TextView = itemView.findViewById(R.id.textView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val holder: RecyclerView.ViewHolder
        if (viewType == NORMAL_VIEW_TYPE) {  // 正常item
            holder = MyViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.gallrey_cell, parent, false)
            )
            holder.itemView.setOnClickListener {
                val bundle = Bundle().apply {
                    putParcelableArrayList("PHOTO_LIST", ArrayList(currentList))
                    putInt("PHOTO_INDEX", holder.adapterPosition) // 当前位置
                }
                val intent: Intent = Intent(parent.context, PhotoActivity::class.java).apply {
                    putExtras(bundle)
                }
                parent.context.startActivity(intent)
            }
        } else {   // footer
            holder = FooterViewHolder(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.gallery_footer,
                    parent,
                    false
                ).also {
                    // 占据全屏空间
                    (it.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan = true
                }
            )
            holder.itemView.setOnClickListener {
                holder.progressBar.visibility = View.VISIBLE
                holder.tv_load_more.text = "正在加载..."
                footerItemClickListener?.onItemClick()
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // // 直接返回，不绑定 Footer 数据
        if (getItemViewType(position) == FOOTER_VIEW_TYPE) {
            val holder = holder as FooterViewHolder // 转换为底部 Item 的 ViewHolder
            with(holder) {
                when (footViewStatus) {
                    DataStatus.LOAD_MORE -> { // 正在加载
                        progressBar.visibility = View.VISIBLE
                        tv_load_more.text = "正在加载..."
                        itemView.isClickable = true
                    }

                    DataStatus.NO_MORE -> {  // 加载完毕
                        progressBar.visibility = View.GONE
                        tv_load_more.text = "全部加载完毕"
                        itemView.isClickable = false
                    }

                    DataStatus.NETWORK_ERROR -> { // 网络错误
                        progressBar.visibility = View.GONE
                        tv_load_more.text = "网络故障，点击重试"
                        itemView.isClickable = true
                    }
                }
            }
            return
        }

        val holder = holder as MyViewHolder // 转换为正常 Item 的 ViewHolder
        val item = getItem(position) ?: return  // 空安全判断
        // 绑定数据
        with(holder) {
            shimmerLayout.startShimmerAnimation() // 启动 shimmer
            photoUser.text = item.photoUser
            photoLikes.text = item.photoLikes.toString()
            photoFavorites.text = item.photoFavorites.toString()
            // 处理高度 避免高度变化
            imageView.layoutParams.height = getItem(position).photoHeight
        }
        // Glide 加载图片
        Glide.with(holder.itemView)
            .load(getItem(position).previewURL)
            .placeholder(R.drawable.ic_photo_gray_24dp)
            .listener(object : RequestListener<Drawable> {

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable?>,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable,
                    model: Any,
                    target: Target<Drawable?>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false.also {
                        holder.shimmerLayout.stopShimmerAnimation()
                    }
                }
            })
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + 1 // // 正常数据 + Footer
    }

    override fun getItemViewType(position: Int): Int {
        // 最后一个item是footer
        return if (position >= super.getItemCount()) FOOTER_VIEW_TYPE else NORMAL_VIEW_TYPE
    }
}