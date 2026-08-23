package com.aorrico.mymbchallenge.data.repository

import com.aorrico.mymbchallenge.data.local.dao.RecentlyViewedExchangeDao
import com.aorrico.mymbchallenge.data.mapper.toDomain
import com.aorrico.mymbchallenge.data.mapper.toEntity
import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.aorrico.mymbchallenge.domain.repository.RecentlyViewedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecentlyViewedRepositoryImpl @Inject constructor(
    private val dao: RecentlyViewedExchangeDao,
) : RecentlyViewedRepository {

    override fun observeRecentlyViewed(): Flow<List<RecentlyViewedExchange>> =
        dao.observeRecentlyViewed(limit = MAX_RECENTLY_VIEWED).map { entities -> entities.map { it.toDomain() } }

    override suspend fun recordView(exchange: RecentlyViewedExchange) {
        dao.upsert(exchange.toEntity())
        dao.pruneToLimit(keep = MAX_RECENTLY_VIEWED)
    }

    private companion object {
        const val MAX_RECENTLY_VIEWED = 5
    }
}
