package com.lsy.kotlin_demo2.domain

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

class Pixabay (
    val totalHits: Int,
    val hits:Array<PhotoItem>,
    val total: Int
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Pixabay
        if (totalHits != other.totalHits) return false
        if (!hits.contentEquals(other.hits)) return false
        if (total != other.total) return false
        return true
    }

    override fun hashCode(): Int {
        val result = totalHits
        return result * 31 + hits.contentHashCode() + total
    }
}

@Parcelize data class PhotoItem(
    @SerializedName("webformatURL") val previewURL: String,
    @SerializedName("id")val photoId: Int,
    @SerializedName("largeImageURL")val fullUrl: String,
    @SerializedName("webformatHeight")val photoHeight: Int,
    @SerializedName("user")val photoUser: String,
    @SerializedName("likes")val photoLikes: Int,
    @SerializedName("favorites")val photoFavorites: Int,

): Parcelable