package com.aorrico.mymbchallenge.domain.repository

import androidx.paging.PagingData
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.domain.model.CryptoAsset
import com.aorrico.mymbchallenge.domain.model.Exchange
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import kotlinx.coroutines.flow.Flow

interface ExchangeRepository {

    /** Active exchanges ordered by 24h spot volume (highest first), paged from the network. */
    fun getExchanges(): Flow<PagingData<Exchange>>

    suspend fun getExchangeDetail(id: Long): AppResult<ExchangeDetail>

    /** Proof-of-reserve assets for [id]. An empty list is a valid, non-error result. */
    suspend fun getExchangeAssets(id: Long): AppResult<List<CryptoAsset>>
}
