package com.aorrico.mymbchallenge.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recently_viewed_exchanges")
data class RecentlyViewedExchangeEntity(
    @PrimaryKey val exchangeId: Long,
    val name: String,
    val logoUrl: String?,
    /** Epoch millis rather than [java.time.Instant] - avoids a Room TypeConverter for one field. */
    val viewedAtEpochMillis: Long,
)
