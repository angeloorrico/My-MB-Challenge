package com.aorrico.mymbchallenge.data.repository

import com.aorrico.mymbchallenge.data.local.dao.RecentlyViewedExchangeDao
import com.aorrico.mymbchallenge.data.local.entity.RecentlyViewedExchangeEntity
import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.google.common.truth.Truth.assertThat
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.Instant

class RecentlyViewedRepositoryImplTest {

    private val dao: RecentlyViewedExchangeDao = mockk(relaxed = true)
    private val repository = RecentlyViewedRepositoryImpl(dao)

    @Test
    fun `observeRecentlyViewed maps entities to domain models, most recent first`() = runTest {
        every { dao.observeRecentlyViewed(limit = 5) } returns flowOf(
            listOf(
                RecentlyViewedExchangeEntity(24, "Kraken", null, viewedAtEpochMillis = 2_000),
                RecentlyViewedExchangeEntity(270, "Binance", "https://logo", viewedAtEpochMillis = 1_000),
            ),
        )

        val result = repository.observeRecentlyViewed()

        result.collect { list ->
            assertThat(list).containsExactly(
                RecentlyViewedExchange(24, "Kraken", null, Instant.ofEpochMilli(2_000)),
                RecentlyViewedExchange(270, "Binance", "https://logo", Instant.ofEpochMilli(1_000)),
            ).inOrder()
        }
    }

    @Test
    fun `recordView upserts the exchange and then prunes older entries beyond the retained limit`() = runTest {
        val exchange = RecentlyViewedExchange(24, "Kraken", null, Instant.ofEpochMilli(2_000))

        repository.recordView(exchange)

        coVerifyOrder {
            dao.upsert(RecentlyViewedExchangeEntity(24, "Kraken", null, viewedAtEpochMillis = 2_000))
            dao.pruneToLimit(keep = 5)
        }
    }
}
