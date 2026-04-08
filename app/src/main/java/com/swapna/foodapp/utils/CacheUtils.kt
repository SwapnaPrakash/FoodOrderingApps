package com.swapna.foodapp.utils

object CacheUtils {

    // Returns true if the cache is older than CACHE_DURATION_MIN
    fun isStale(cachedAt: Long?): Boolean {
        if (cachedAt == null) return true
        val ageMillis = System.currentTimeMillis() - cachedAt
        val ageMinutes = ageMillis / 1000 / 60
        return ageMinutes >= AppConstants.CACHE_DURATION_MIN
    }

    // Convenience: check freshness
    fun isFresh(cachedAt: Long?): Boolean = !isStale(cachedAt)
}