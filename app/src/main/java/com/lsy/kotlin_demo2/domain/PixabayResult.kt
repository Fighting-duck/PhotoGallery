package com.lsy.kotlin_demo2.domain

class PixabayResult<T>(
    val totalHits: Int,
    val hits: Array<T>,
    val total: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as PixabayResult<T>
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