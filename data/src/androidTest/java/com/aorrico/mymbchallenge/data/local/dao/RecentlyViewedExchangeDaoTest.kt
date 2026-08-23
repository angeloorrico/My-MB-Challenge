package com.aorrico.mymbchallenge.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.aorrico.mymbchallenge.data.local.AppDatabase
import com.aorrico.mymbchallenge.data.local.entity.RecentlyViewedExchangeEntity
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RecentlyViewedExchangeDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: RecentlyViewedExchangeDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.recentlyViewedExchangeDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_thenObserve_returnsMostRecentFirst() = runTest {
        dao.upsert(RecentlyViewedExchangeEntity(24, "Kraken", null, viewedAtEpochMillis = 1_000))
        dao.upsert(RecentlyViewedExchangeEntity(270, "Binance", "https://logo", viewedAtEpochMillis = 2_000))

        val result = dao.observeRecentlyViewed(limit = 5).first()

        assertThat(result.map { it.exchangeId }).containsExactly(270L, 24L).inOrder()
    }

    @Test
    fun upsert_withExistingId_replacesTheRowInstead_ofDuplicatingIt() = runTest {
        dao.upsert(RecentlyViewedExchangeEntity(24, "Kraken", null, viewedAtEpochMillis = 1_000))
        dao.upsert(RecentlyViewedExchangeEntity(24, "Kraken", null, viewedAtEpochMillis = 5_000))

        val result = dao.observeRecentlyViewed(limit = 5).first()

        assertThat(result).hasSize(1)
        assertThat(result.single().viewedAtEpochMillis).isEqualTo(5_000)
    }

    @Test
    fun pruneToLimit_removesEntriesOlderThanTheKeepCount() = runTest {
        repeat(7) { index ->
            dao.upsert(RecentlyViewedExchangeEntity(index.toLong(), "Exchange $index", null, viewedAtEpochMillis = index.toLong()))
        }

        dao.pruneToLimit(keep = 5)

        val result = dao.observeRecentlyViewed(limit = 100).first()
        assertThat(result.map { it.exchangeId }).containsExactly(6L, 5L, 4L, 3L, 2L).inOrder()
    }
}
