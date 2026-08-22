package com.aorrico.mymbchallenge.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.aorrico.mymbchallenge.core.common.error.asException
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.data.mapper.toExchange
import com.aorrico.mymbchallenge.data.remote.CoinMarketCapApi
import com.aorrico.mymbchallenge.data.remote.SafeApiCallExecutor
import com.aorrico.mymbchallenge.domain.model.Exchange

/**
 * Feeds the exchange list one page at a time. Each page costs two API credits: a cheap
 * `/exchange/map` call (sorted by volume) to pick which ids belong on this page, in order, and
 * one batched `/exchange/info` call to fetch the full record for all of them at once - the free
 * plan doesn't allow `/exchange/listings/latest`, which would otherwise do this in one call.
 *
 * The list is append-only (no jump-to-position/prepend support needed for a simple feed), so
 * [getRefreshKey] always restarts from the first page rather than trying to recompute an anchor.
 */
class ExchangePagingSource(
    private val api: CoinMarketCapApi,
    private val safeApiCallExecutor: SafeApiCallExecutor,
) : PagingSource<Int, Exchange>() {

    override fun getRefreshKey(state: PagingState<Int, Exchange>): Int? = null

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Exchange> {
        val start = params.key ?: FIRST_PAGE_START
        val limit = params.loadSize.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)

        val mapResult = safeApiCallExecutor.execute {
            api.getExchangeMap(start = start, limit = limit)
        }
        val mapItems = when (mapResult) {
            is AppResult.Error -> return LoadResult.Error(mapResult.error.asException())
            is AppResult.Success -> mapResult.data
        }

        if (mapItems.isEmpty()) {
            return LoadResult.Page(data = emptyList(), prevKey = null, nextKey = null)
        }

        val ids = mapItems.joinToString(",") { it.id.toString() }
        val infoResult = safeApiCallExecutor.execute { api.getExchangeInfo(ids = ids) }
        val infoById = when (infoResult) {
            is AppResult.Error -> return LoadResult.Error(infoResult.error.asException())
            is AppResult.Success -> infoResult.data
        }

        // /exchange/info returns an unordered map keyed by id; re-apply the volume-sorted
        // order that /exchange/map gave us. An id can legitimately be absent from the info
        // response (e.g. delisted between the two calls) - such rows are simply skipped.
        val exchanges = mapItems.mapNotNull { item -> infoById[item.id.toString()]?.toExchange() }

        return LoadResult.Page(
            data = exchanges,
            prevKey = null,
            nextKey = if (mapItems.size < limit) null else start + limit,
        )
    }

    companion object {
        const val FIRST_PAGE_START = 1
        const val DEFAULT_PAGE_SIZE = 20
        private const val MIN_PAGE_SIZE = 1
        private const val MAX_PAGE_SIZE = 100
    }
}
