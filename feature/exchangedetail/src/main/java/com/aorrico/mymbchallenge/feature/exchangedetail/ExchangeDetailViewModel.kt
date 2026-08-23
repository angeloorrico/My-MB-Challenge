package com.aorrico.mymbchallenge.feature.exchangedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.aorrico.mymbchallenge.domain.usecase.GetExchangeAssetsUseCase
import com.aorrico.mymbchallenge.domain.usecase.GetExchangeDetailUseCase
import com.aorrico.mymbchallenge.domain.usecase.RecordExchangeViewedUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant

/**
 * [exchangeId] is an assisted parameter instead of a nav-graph
 * [androidx.lifecycle.SavedStateHandle] argument, because there's no per-destination
 * NavBackStackEntry to read it from here - both panes of the list-detail layout are just
 * composables on the same screen. A caller switches exchanges by requesting a differently-keyed
 * instance instead (see [ExchangeDetailRoute]).
 */
@HiltViewModel(assistedFactory = ExchangeDetailViewModel.Factory::class)
class ExchangeDetailViewModel @AssistedInject constructor(
    @Assisted private val exchangeId: Long,
    private val getExchangeDetailUseCase: GetExchangeDetailUseCase,
    private val getExchangeAssetsUseCase: GetExchangeAssetsUseCase,
    private val recordExchangeViewedUseCase: RecordExchangeViewedUseCase,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(exchangeId: Long): ExchangeDetailViewModel
    }

    private val _uiState = MutableStateFlow(ExchangeDetailUiState())
    val uiState: StateFlow<ExchangeDetailUiState> = _uiState.asStateFlow()

    init {
        loadInfo()
        loadAssets()

        // isConnected is a StateFlow, so it only ever emits on actual value changes - drop(1)
        // skips the initial state, leaving only true genuine reconnect transitions (false->true).
        viewModelScope.launch {
            connectivityObserver.isConnected
                .drop(1)
                .filter { connected -> connected }
                .collect {
                    if (_uiState.value.info.isNoConnectivityError) loadInfo()
                    if (_uiState.value.assets.isNoConnectivityError) loadAssets()
                }
        }
    }

    fun retryInfo() = loadInfo()

    fun retryAssets() = loadAssets()

    private fun loadInfo() {
        _uiState.update { it.copy(info = InfoState.Loading) }
        viewModelScope.launch {
            val newInfoState = when (val result = getExchangeDetailUseCase(exchangeId)) {
                is AppResult.Success -> {
                    // Recorded on a successful load, not on tap - a view that never actually
                    // rendered anything (offline, bad id) shouldn't show up as "recently viewed".
                    recordExchangeViewedUseCase(result.data.toRecentlyViewed())
                    InfoState.Success(result.data)
                }
                is AppResult.Error -> InfoState.Error(result.error)
            }
            _uiState.update { it.copy(info = newInfoState) }
        }
    }

    private fun ExchangeDetail.toRecentlyViewed() = RecentlyViewedExchange(
        exchangeId = id,
        name = name,
        logoUrl = logoUrl,
        viewedAt = Instant.now(),
    )

    private fun loadAssets() {
        _uiState.update { it.copy(assets = AssetsState.Loading) }
        viewModelScope.launch {
            val newAssetsState = when (val result = getExchangeAssetsUseCase(exchangeId)) {
                is AppResult.Success -> AssetsState.Success(result.data)
                is AppResult.Error -> AssetsState.Error(result.error)
            }
            _uiState.update { it.copy(assets = newAssetsState) }
        }
    }

    private val InfoState.isNoConnectivityError: Boolean
        get() = this is InfoState.Error && error is AppError.NoConnectivity

    private val AssetsState.isNoConnectivityError: Boolean
        get() = this is AssetsState.Error && error is AppError.NoConnectivity
}
