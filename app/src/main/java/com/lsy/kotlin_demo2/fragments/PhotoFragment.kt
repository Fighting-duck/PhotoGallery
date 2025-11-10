package com.lsy.kotlin_demo2.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lsy.kotlin_demo2.domain.PhotoItem
import com.lsy.kotlin_demo2.R
import com.lsy.kotlin_demo2.databinding.FragmentPhotoBinding

class PhotoFragment : Fragment() {
    private lateinit var mBinding: FragmentPhotoBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mBinding = FragmentPhotoBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.shimmerLayoutPhoto.apply {
            setShimmerColor(0x55FFFFFF)
            setShimmerAngle(0)
            stopShimmerAnimation()
        }
        Glide.with(requireActivity())
            .load(arguments?.getParcelable<PhotoItem>("PHOTO")?.fullUrl)
            .placeholder(R.drawable.ic_photo_gray_24dp)
            .listener(object: RequestListener<Drawable> {
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
                        mBinding.shimmerLayoutPhoto.stopShimmerAnimation()
                    }
                }
            })
            .into(mBinding.photoView)

    }
}