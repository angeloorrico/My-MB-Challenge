package com.aorrico.mymbchallenge.data.repository

import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.data.remote.CoinMarketCapApi
import com.aorrico.mymbchallenge.data.remote.FakeConnectivityObserver
import com.aorrico.mymbchallenge.data.remote.SafeApiCallExecutor
import com.aorrico.mymbchallenge.data.remote.dto.CmcEnvelope
import com.aorrico.mymbchallenge.data.remote.dto.CmcStatusDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeAssetCurrencyDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeAssetDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeInfoDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeUrlsDto
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ExchangeRepositoryImplTest {

    private val api: CoinMarketCapApi = mockk()
    private lateinit var repository: ExchangeRepositoryImpl

    private val okStatus = CmcStatusDto(errorCode = 0, errorMessage = null)

    @Before
    fun setUp() {
        repository = ExchangeRepositoryImpl(
            api,
            SafeApiCallExecutor(Moshi.Builder().build(), FakeConnectivityObserver(connected = true)),
        )
    }

    @Test
    fun `getExchangeDetail returns the mapped detail on success`() = runTest {
        coEvery { api.getExchangeInfo(ids = "24") } returns CmcEnvelope(
            status = okStatus,
            data = mapOf(
                "24" to ExchangeInfoDto(
                    id = 24,
                    name = "Kraken",
                    slug = "kraken",
                    description = "desc",
                    logo = "logo.png",
                    urls = ExchangeUrlsDto(website = listOf("https://kraken.com")),
                    dateLaunched = "2011-07-28T00:00:00.000Z",
                    makerFee = 0.02,
                    takerFee = 0.05,
                    spotVolumeUsd = 100.0,
                ),
            ),
        )

        val result = repository.getExchangeDetail(24) as AppResult.Success

        assertThat(result.data.id).isEqualTo(24)
        assertThat(result.data.name).isEqualTo("Kraken")
    }

    @Test
    fun `getExchangeDetail returns a Parsing error when the response is missing the requested id`() = runTest {
        coEvery { api.getExchangeInfo(ids = "24") } returns CmcEnvelope(status = okStatus, data = emptyMap())

        val result = repository.getExchangeDetail(24) as AppResult.Error

        assertThat(result.error).isInstanceOf(AppError.Parsing::class.java)
    }

    @Test
    fun `getExchangeDetail propagates API errors`() = runTest {
        coEvery { api.getExchangeInfo(ids = "999") } returns CmcEnvelope(
            status = CmcStatusDto(errorCode = 400, errorMessage = "Invalid value"),
            data = null,
        )

        val result = repository.getExchangeDetail(999) as AppResult.Error

        assertThat(result.error).isEqualTo(AppError.Api(400, "Invalid value"))
    }

    @Test
    fun `getExchangeAssets returns an empty list when the exchange has none, not an error`() = runTest {
        coEvery { api.getExchangeAssets(id = 24) } returns CmcEnvelope(status = okStatus, data = emptyList())

        val result = repository.getExchangeAssets(24) as AppResult.Success

        assertThat(result.data).isEmpty()
    }

    @Test
    fun `getExchangeAssets maps currencies from proof-of-reserve wallets`() = runTest {
        coEvery { api.getExchangeAssets(id = 270) } returns CmcEnvelope(
            status = okStatus,
            data = listOf(
                ExchangeAssetDto(
                    walletAddress = "0x1",
                    balance = 10.0,
                    currency = ExchangeAssetCurrencyDto(cryptoId = 1, symbol = "BTC", name = "Bitcoin", priceUsd = 65000.0),
                ),
            ),
        )

        val result = repository.getExchangeAssets(270) as AppResult.Success

        assertThat(result.data.single().symbol).isEqualTo("BTC")
    }
}
