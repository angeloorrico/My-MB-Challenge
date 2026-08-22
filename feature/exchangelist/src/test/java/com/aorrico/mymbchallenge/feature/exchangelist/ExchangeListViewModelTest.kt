package com.aorrico.mymbchallenge.feature.exchangelist

import androidx.paging.PagingData
import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import com.aorrico.mymbchallenge.domain.usecase.GetExchangesUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

/**
 * cachedIn(viewModelScope) keeps a sharing coroutine alive on Dispatchers.Main for the whole
 * ViewModel lifetime, so collecting it via asSnapshot() inside runTest just fights
 * kotlinx-coroutines-test's "no uncompleted coroutines" check no matter what dispatcher you pick.
 * The actual paging behavior (ordering, page boundaries, error propagation) is already covered
 * end-to-end by ExchangePagingSourceTest and ExchangeRepositoryImplTest - this test just needs to
 * prove the ViewModel is wired to the use case it was given.
 */
class ExchangeListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun connectivityObserver(connected: Boolean = true): ConnectivityObserver {
        val observer: ConnectivityObserver = mockk()
        every { observer.isConnected } returns MutableStateFlow(connected)
        return observer
    }

    @Test
    fun `constructor reads the exchanges flow from the injected use case exactly once`() {
        val useCase: GetExchangesUseCase = mockk()
        every { useCase() } returns flowOf(PagingData.empty())

        ExchangeListViewModel(useCase, connectivityObserver())

        verify(exactly = 1) { useCase() }
    }

    @Test
    fun `isConnected exposes the connectivity observer's state`() {
        val useCase: GetExchangesUseCase = mockk()
        every { useCase() } returns flowOf(PagingData.empty())

        val viewModel = ExchangeListViewModel(useCase, connectivityObserver(connected = false))

        assertThat(viewModel.isConnected.value).isFalse()
    }
}
