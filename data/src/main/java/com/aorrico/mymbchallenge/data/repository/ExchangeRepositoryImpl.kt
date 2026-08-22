package com.aorrico.mymbchallenge.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.core.common.result.map
import com.aorrico.mymbchallenge.data.mapper.toCryptoAssets
import com.aorrico.mymbchallenge.data.mapper.toExchangeDetail
import com.aorrico.mymbchallenge.data.paging.ExchangePagingSource
import com.aorrico.mymbchallenge.data.remote.CoinMarketCapApi
import com.aorrico.mymbchallenge.data.remote.SafeApiCallExecutor
import com.aorrico.mymbchallenge.domain.model.CryptoAsset
import com.aorrico.mymbchallenge.domain.model.Exchange
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import com.aorrico.mymbchallenge.domain.repository.ExchangeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExchangeRepositoryImpl @Inject constructor(
    private val api: CoinMarketCapApi,
    private val safeApiCallExecutor: SafeApiCallExecutor,
) : ExchangeRepository {

    override fun getExchanges(): Flow<PagingData<Exchange>> = Pager(
        config = PagingConfig(
            pageSize = ExchangePagingSource.DEFAULT_PAGE_SIZE,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { ExchangePagingSource(api, safeApiCallExecutor) },
    ).flow

    override suspend fun getExchangeDetail(id: Long): AppResult<ExchangeDetail> =
        when (val result = safeApiCallExecutor.execute { api.getExchangeInfo(ids = id.toString()) }) {
            is AppResult.Error -> result
            is AppResult.Success -> {
                val info = result.data[id.toString()]
                if (info != null) {
                    AppResult.Success(info.toExchangeDetail())
                } else {
                    // Defensive: CMC returns error_code != 0 (caught above) whenever a
                    // requested id doesn't resolve, so this only guards against the response
                    // shape ever changing.
                    AppResult.Error(AppError.Parsing("Exchange $id was missing from the response"))
                }
            }
        }

    override suspend fun getExchangeAssets(id: Long): AppResult<List<CryptoAsset>> =
        safeApiCallExecutor.execute { api.getExchangeAssets(id = id) }
            .map { assets -> assets.toCryptoAssets() }
}
