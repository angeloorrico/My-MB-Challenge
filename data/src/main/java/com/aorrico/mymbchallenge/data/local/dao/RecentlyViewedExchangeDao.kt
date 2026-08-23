package com.aorrico.mymbchallenge.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.aorrico.mymbchallenge.data.local.entity.RecentlyViewedExchangeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyViewedExchangeDao {

    @Upsert
    suspend fun upsert(entity: RecentlyViewedExchangeEntity)

    @Query("SELECT * FROM recently_viewed_exchanges ORDER BY viewedAtEpochMillis DESC LIMIT :limit")
    fun observeRecentlyViewed(limit: Int): Flow<List<RecentlyViewedExchangeEntity>>

    @Query(
        "DELETE FROM recently_viewed_exchanges WHERE exchangeId NOT IN " +
            "(SELECT exchangeId FROM recently_viewed_exchanges ORDER BY viewedAtEpochMillis DESC LIMIT :keep)",
    )
    suspend fun pruneToLimit(keep: Int)
}
