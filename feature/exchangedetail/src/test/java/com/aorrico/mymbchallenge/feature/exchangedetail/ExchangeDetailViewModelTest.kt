package com.aorrico.mymbchallenge.feature.exchangedetail

import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.domain.model.CryptoAsset
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.aorrico.mymbchallenge.domain.usecase.GetExchangeAssetsUseCase
import com.aorrico.mymbchallenge.domain.usecase.GetExchangeDetailUseCase
import com.aorrico.mymbchallenge.domain.usecase.RecordExchangeViewedUseCase
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ExchangeDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getExchangeDetailUseCase: GetExchangeDetailUseCase = mockk()
    private val getExchangeAssetsUseCase: GetExchangeAssetsUseCase = mockk()
    private val recordExchangeViewedUseCase: RecordExchangeViewedUseCase = mockk(relaxed = true)

    private val detail = ExchangeDetail(
        id = 24,
        name = "Kraken",
        logoUrl = null,
        description = "desc",
        websiteUrl = "https://kraken.com",
        makerFee = 0.02,
        takerFee = 0.05,
        dateLaunched = null,
    )

    private fun viewModel(
        id: Long = 24,
        connectivity: MutableStateFlow<Boolean> = MutableStateFlow(true),
    ): ExchangeDetailViewModel {
        val connectivityObserver: ConnectivityObserver = mockk()
        every { connectivityObserver.isConnected } returns connectivity
        return ExchangeDetailViewModel(
            id,
            getExchangeDetailUseCase,
            getExchangeAssetsUseCase,
            recordExchangeViewedUseCase,
            connectivityObserver,
        )
    }

    @Test
    fun `both info and assets load independently and succeed`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(
            listOf(CryptoAsset(symbol = "BTC", name = "Bitcoin", priceUsd = 65000.0)),
        )

        val state = viewModel().uiState.value

        assertThat(state.info).isEqualTo(InfoState.Success(detail))
        assertThat((state.assets as AssetsState.Success).assets).hasSize(1)
    }

    @Test
    fun `an exchange with no proof-of-reserve assets is a Success with an empty list, not an error`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(emptyList())

        val state = viewModel().uiState.value

        assertThat(state.assets).isEqualTo(AssetsState.Success(emptyList()))
    }

    @Test
    fun `assets failing does not affect the already-loaded info state`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Error(AppError.NoConnectivity())

        val state = viewModel().uiState.value

        assertThat(state.info).isEqualTo(InfoState.Success(detail))
        assertThat(state.assets).isInstanceOf(AssetsState.Error::class.java)
    }

    @Test
    fun `info failing does not affect the already-loaded assets state`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Error(AppError.NoConnectivity())
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(emptyList())

        val state = viewModel().uiState.value

        assertThat(state.info).isInstanceOf(InfoState.Error::class.java)
        assertThat(state.assets).isEqualTo(AssetsState.Success(emptyList()))
    }

    @Test
    fun `records a view only when info loads successfully`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(emptyList())

        viewModel()

        coVerify(exactly = 1) {
            recordExchangeViewedUseCase(
                match<RecentlyViewedExchange> { it.exchangeId == 24L && it.name == "Kraken" && it.logoUrl == null },
            )
        }
    }

    @Test
    fun `does not record a view when info fails to load`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Error(AppError.NoConnectivity())
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(emptyList())

        viewModel()

        coVerify(exactly = 0) { recordExchangeViewedUseCase(any()) }
    }

    @Test
    fun `retryInfo re-fetches only the info section`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Error(AppError.NoConnectivity())
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(emptyList())

        val vm = viewModel()
        assertThat(vm.uiState.value.info).isInstanceOf(InfoState.Error::class.java)

        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        vm.retryInfo()

        assertThat(vm.uiState.value.info).isEqualTo(InfoState.Success(detail))
    }

    @Test
    fun `retryAssets re-fetches only the assets section`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Error(AppError.NoConnectivity())

        val vm = viewModel()
        assertThat(vm.uiState.value.assets).isInstanceOf(AssetsState.Error::class.java)

        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(
            listOf(CryptoAsset(symbol = "BTC", name = "Bitcoin", priceUsd = 65000.0)),
        )
        vm.retryAssets()

        assertThat((vm.uiState.value.assets as AssetsState.Success).assets).hasSize(1)
    }

    @Test
    fun `auto-retries a NoConnectivity info failure when connectivity returns`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Error(AppError.NoConnectivity())
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(emptyList())

        val connectivity = MutableStateFlow(true)
        val vm = viewModel(connectivity = connectivity)
        assertThat(vm.uiState.value.info).isInstanceOf(InfoState.Error::class.java)

        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        connectivity.value = false
        connectivity.value = true

        assertThat(vm.uiState.value.info).isEqualTo(InfoState.Success(detail))
    }

    @Test
    fun `auto-retries a NoConnectivity assets failure when connectivity returns`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Error(AppError.NoConnectivity())

        val connectivity = MutableStateFlow(true)
        val vm = viewModel(connectivity = connectivity)
        assertThat(vm.uiState.value.assets).isInstanceOf(AssetsState.Error::class.java)

        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(
            listOf(CryptoAsset(symbol = "BTC", name = "Bitcoin", priceUsd = 65000.0)),
        )
        connectivity.value = false
        connectivity.value = true

        assertThat((vm.uiState.value.assets as AssetsState.Success).assets).hasSize(1)
    }

    @Test
    fun `does not retry a failure that was not caused by connectivity`() = runTest {
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Error(AppError.Api(1006, "Plan restricted"))
        coEvery { getExchangeAssetsUseCase(24) } returns AppResult.Success(emptyList())

        val connectivity = MutableStateFlow(true)
        val vm = viewModel(connectivity = connectivity)

        // If a retry were wrongly triggered, this would flip the state to Success and the
        // assertion below would fail.
        coEvery { getExchangeDetailUseCase(24) } returns AppResult.Success(detail)
        connectivity.value = false
        connectivity.value = true

        assertThat(vm.uiState.value.info).isEqualTo(InfoState.Error(AppError.Api(1006, "Plan restricted")))
    }
}
