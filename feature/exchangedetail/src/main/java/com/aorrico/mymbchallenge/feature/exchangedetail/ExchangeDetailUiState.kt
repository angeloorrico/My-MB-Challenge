package com.aorrico.mymbchallenge.feature.exchangedetail

import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.domain.model.CryptoAsset
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail

/**
 * The core exchange record and its proof-of-reserve assets are loaded independently: a failure
 * fetching one must not block the other from rendering (e.g. the exchange's description/fees
 * still show up even if the assets call times out).
 */
data class ExchangeDetailUiState(
    val info: InfoState = InfoState.Loading,
    val assets: AssetsState = AssetsState.Loading,
)

sealed interface InfoState {
    data object Loading : InfoState
    data class Success(val detail: ExchangeDetail) : InfoState
    data class Error(val error: AppError) : InfoState
}

sealed interface AssetsState {
    data object Loading : AssetsState
    data class Success(val assets: List<CryptoAsset>) : AssetsState
    data class Error(val error: AppError) : AssetsState
}
