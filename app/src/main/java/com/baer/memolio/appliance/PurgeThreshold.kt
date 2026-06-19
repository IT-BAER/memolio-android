package com.baer.memolio.appliance

/**
 * Pure trash-retention cutoff. A photo with deletedAt < cutoff(now) is past its
 * retention window and may be purged. Default retention is 30 days (spec section 6).
 */
object PurgeThreshold {

    const val DEFAULT_RETENTION_DAYS = 30

    fun cutoff(now: Long, retentionDays: Int = DEFAULT_RETENTION_DAYS): Long =
        now - retentionDays.toLong() * 24L * 60L * 60L * 1000L
}
