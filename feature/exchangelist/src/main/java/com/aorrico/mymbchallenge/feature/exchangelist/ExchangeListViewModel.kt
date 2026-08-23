package com.aorrico.mymbchallenge.feature.exchangelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import com.aorrico.mymbchallenge.domain.model.Exchange
import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.aorrico.mymbchallenge.domain.usecase.GetExchangesUseCase
import com.aorrico.mymbchallenge.domain.usecase.ObserveRecentlyViewedExchangesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ExchangeListViewModel @Inject constructor(
    getExchangesUseCase: GetExchangesUseCase,
    observeRecentlyViewedExchangesUseCase: ObserveRecentlyViewedExchangesUseCase,
    connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    /**
     * cachedIn(viewModelScope) makes the paged stream survive configuration changes (rotation)
     * without re-fetching: the PagingData snapshot is kept alive as long as the ViewModel is.
     */
    val exchanges: Flow<PagingData<Exchange>> = getExchangesUseCase().cachedIn(viewModelScope)

    /** Exposed so the UI can auto-retry a failed load the moment connectivity returns. */
    val isConnected: StateFlow<Boolean> = connectivityObserver.isConnected

    val recentlyViewed: StateFlow<List<RecentlyViewedExchange>> = observeRecentlyViewedExchangesUseCase()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
