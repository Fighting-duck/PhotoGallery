package com.lsy.kotlin_demo2.adapters

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.activities.PhotoActivity
import com.lsy.kotlin_demo2.database.NetworkStatus
import com.lsy.kotlin_demo2.domain.PhotoItem
import com.lsy.kotlin_demo2.interfaces.OnFooterItemClickListener
import io.supercharge.shimmerlayout.ShimmerLayout

class GalleryAdapter(
    private val retry: () -> Unit = {}
) : PagedListAdapter<PhotoItem, RecyclerView.ViewHolder>(DiffCallback) {
    private var networkStatus: NetworkStatus = NetworkStatus.LOADING
    var footerItemClickListener: OnFooterItemClickListener? = null
    fun setFooterListener(listener: OnFooterItemClickListener) {
        footerItemClickListener = listener
    }

    private var hasFooter = false

    init {
        Log.d("GalleryAdapter", "init")
        retry()
    }

    constructor() : this(retry = {}) {

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

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun newInstance(parent: ViewGroup): PhotoViewHolder {
                return PhotoViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.gallrey_cell,
                        parent,
                        false
                    )
                )
            }
        }

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

        fun bindWithPhotoItem(item: PhotoItem) {
            with(this) {
                shimmerLayout.startShimmerAnimation() // 启动 shimmer
                photoUser.text = item.photoUser
                photoLikes.text = item.photoLikes.toString()
                photoFavorites.text = item.photoViews.toString()
                // 处理高度 避免高度变化
                imageView.layoutParams.height = item.photoHeight
            }
            // Glide 加载图片
            Glide.with(itemView)
                .load(item.previewURL)
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
                            shimmerLayout.stopShimmerAnimation()
                        }
                    }
                })
                .into(imageView)
        }
    }

    class FooterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        companion object {
            fun newInstance(parent: ViewGroup): FooterViewHolder {
                return FooterViewHolder(
                    LayoutInflater.from(parent.context).inflate(
                        R.layout.gallery_footer,
                        parent,
                        false
                    ).also {
                        // 占据全屏空间
                        (it.layoutParams as StaggeredGridLayoutManager.LayoutParams).isFullSpan =
                            true
                    }
                )
            }
        }

        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val tv_load_more: TextView = itemView.findViewById(R.id.textView)

        fun bindWithNetworkStatus(networkStatus: NetworkStatus) {
            with(this) {
                when (networkStatus) {
                    NetworkStatus.INITIAL_LOADING -> {// 初始加载
                    }

                    NetworkStatus.LOADED -> {// 一个页面加载完毕

                    }

                    NetworkStatus.LOADING -> { // 正在加载
                        progressBar.visibility = View.VISIBLE
                        tv_load_more.text = "正在加载..."
                        itemView.isClickable = true
                    }

                    NetworkStatus.COMPLETED -> {  // 加载完毕
                        progressBar.visibility = View.GONE
                        tv_load_more.text = "全部加载完毕"
                        itemView.isClickable = false
                    }

                    NetworkStatus.FAILED -> { // 网络错误
                        progressBar.visibility = View.GONE
                        tv_load_more.text = "网络故障，点击重试"
                        itemView.isClickable = true
                    }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // 根据 viewType 返回对应的ViewHolder
        return when (viewType) {
            R.layout.gallrey_cell -> PhotoViewHolder.newInstance(parent).also { holder ->
                holder.itemView.setOnClickListener {
                    val bundle = Bundle().apply {
                        putParcelableArrayList("PHOTO_LIST", ArrayList(currentList!!))
                        putInt("PHOTO_INDEX", holder.adapterPosition) // 当前位置
                    }
                    val intent: Intent = Intent(parent.context, PhotoActivity::class.java).apply {
                        putExtras(bundle)
                    }
                    parent.context.startActivity(intent)
                }
            }

            else -> FooterViewHolder.newInstance(parent).also { holder ->
                holder.itemView.setOnClickListener {
                    holder.progressBar.visibility = View.VISIBLE
                    holder.tv_load_more.text = "正在加载..."
                    footerItemClickListener?.onItemClick()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            R.layout.gallrey_cell -> {
                val item = getItem(position) ?: return
                (holder as PhotoViewHolder).bindWithPhotoItem(item)
            }

            else -> {
                // 转换为底部 Item 的 ViewHolder
                (holder as FooterViewHolder).bindWithNetworkStatus(networkStatus)
            }
        }
    }

    override fun getItemCount(): Int {
        return super.getItemCount() + if (hasFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        // 最后一个item是footer
        return if (hasFooter && position == itemCount - 1)
            R.layout.gallery_footer
        else
            R.layout.gallrey_cell
    }

    /**
     * 更新网络状态
     * @param networkStatus 网络状态
     * @return
     */
    fun updateNetworkStatus(networkStatus: NetworkStatus) {
        this.networkStatus = networkStatus
        if (networkStatus == NetworkStatus.INITIAL_LOADING) {
            hideFooter()
        } else {
            showFooter()
        }
    }
    /**
     * 隐藏底部
     * @return
     */
    private fun hideFooter() {
        if (hasFooter) {
            notifyItemRemoved(itemCount - 1)
        }
        hasFooter = false
    }

    /**
     * 显示底部
     * @return
     */
    private fun showFooter() {
        if (hasFooter) {
            notifyItemChanged(itemCount - 1)
        } else {
            hasFooter = true
            notifyItemInserted(itemCount - 1)
        }
    }
}