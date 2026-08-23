package com.aorrico.mymbchallenge.domain.repository

import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import kotlinx.coroutines.flow.Flow

interface RecentlyViewedRepository {

    /** Most recently viewed exchanges first, capped to a small shortcut-row length. */
    fun observeRecentlyViewed(): Flow<List<RecentlyViewedExchange>>

    /** Upserts [exchange] as just-viewed and prunes older entries beyond the retained limit. */
    suspend fun recordView(exchange: RecentlyViewedExchange)
}
