package com.aorrico.mymbchallenge.data.paging

import androidx.paging.PagingSource
import com.aorrico.mymbchallenge.data.remote.CoinMarketCapApi
import com.aorrico.mymbchallenge.data.remote.FakeConnectivityObserver
import com.aorrico.mymbchallenge.data.remote.SafeApiCallExecutor
import com.aorrico.mymbchallenge.data.remote.dto.CmcEnvelope
import com.aorrico.mymbchallenge.data.remote.dto.CmcStatusDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeInfoDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeMapItemDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeUrlsDto
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ExchangePagingSourceTest {

    private val api: CoinMarketCapApi = mockk()
    private lateinit var pagingSource: ExchangePagingSource

    private val okStatus = CmcStatusDto(errorCode = 0, errorMessage = null)

    @Before
    fun setUp() {
        pagingSource = ExchangePagingSource(
            api,
            SafeApiCallExecutor(Moshi.Builder().build(), FakeConnectivityObserver(connected = true)),
        )
    }

    private fun infoDto(id: Long, name: String) = ExchangeInfoDto(
        id = id,
        name = name,
        slug = name.lowercase(),
        description = null,
        logo = null,
        urls = ExchangeUrlsDto(website = null),
        dateLaunched = null,
        makerFee = null,
        takerFee = null,
        spotVolumeUsd = 100.0,
    )

    @Test
    fun `load returns a page ordered as exchange map returned it, enriched with info data`() = runTest {
        // loadSize matches the number of items returned, so the page is "full" and pagination
        // continues - a short page (fewer items than requested) is covered by the next test.
        coEvery { api.getExchangeMap(start = 1, limit = 2) } returns CmcEnvelope(
            status = okStatus,
            data = listOf(
                ExchangeMapItemDto(id = 270, name = "Binance", slug = "binance", isActive = 1),
                ExchangeMapItemDto(id = 24, name = "Kraken", slug = "kraken", isActive = 1),
            ),
        )
        coEvery { api.getExchangeInfo(ids = "270,24") } returns CmcEnvelope(
            status = okStatus,
            // Deliberately returned out of order to prove the map's order wins.
            data = mapOf("24" to infoDto(24, "Kraken"), "270" to infoDto(270, "Binance")),
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 2, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertThat(result.data.map { it.id }).containsExactly(270L, 24L).inOrder()
        assertThat(result.nextKey).isEqualTo(3)
        assertThat(result.prevKey).isNull()
    }

    @Test
    fun `load returns null nextKey when the map page is not full (last page)`() = runTest {
        coEvery { api.getExchangeMap(start = 1, limit = 20) } returns CmcEnvelope(
            status = okStatus,
            data = listOf(ExchangeMapItemDto(id = 270, name = "Binance", slug = "binance", isActive = 1)),
        )
        coEvery { api.getExchangeInfo(ids = "270") } returns CmcEnvelope(
            status = okStatus,
            data = mapOf("270" to infoDto(270, "Binance")),
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertThat(result.nextKey).isNull()
    }

    @Test
    fun `load returns an empty page with no next key when the map page is empty`() = runTest {
        coEvery { api.getExchangeMap(start = any(), limit = any()) } returns CmcEnvelope(
            status = okStatus,
            data = emptyList(),
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Append(key = 41, loadSize = 20, placeholdersEnabled = false),
        ) as PagingSource.LoadResult.Page

        assertThat(result.data).isEmpty()
        assertThat(result.nextKey).isNull()
    }

    @Test
    fun `load surfaces a map endpoint failure as LoadResult Error`() = runTest {
        coEvery { api.getExchangeMap(start = 1, limit = 20) } returns CmcEnvelope(
            status = CmcStatusDto(errorCode = 1008, errorMessage = "Rate limited"),
            data = null,
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
    }

    @Test
    fun `load surfaces an info endpoint failure as LoadResult Error even when map succeeded`() = runTest {
        coEvery { api.getExchangeMap(start = 1, limit = 20) } returns CmcEnvelope(
            status = okStatus,
            data = listOf(ExchangeMapItemDto(id = 270, name = "Binance", slug = "binance", isActive = 1)),
        )
        coEvery { api.getExchangeInfo(ids = "270") } returns CmcEnvelope(
            status = CmcStatusDto(errorCode = 400, errorMessage = "Invalid value"),
            data = null,
        )

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false),
        )

        assertThat(result).isInstanceOf(PagingSource.LoadResult.Error::class.java)
    }

    @Test
    fun `getRefreshKey always restarts from the first page since prepend is not supported`() {
        assertThat(pagingSource.getRefreshKey(mockk())).isNull()
    }
}
