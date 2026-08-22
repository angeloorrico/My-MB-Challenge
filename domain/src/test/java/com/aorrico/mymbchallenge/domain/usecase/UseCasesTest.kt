package com.aorrico.mymbchallenge.domain.usecase

import androidx.paging.PagingData
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.domain.model.CryptoAsset
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import com.aorrico.mymbchallenge.domain.repository.ExchangeRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test

/** Each use case is a thin delegation to the repository - these confirm the wiring, nothing more. */
class UseCasesTest {

    private val repository: ExchangeRepository = mockk()

    @Test
    fun `GetExchangesUseCase delegates to the repository`() {
        val flow = flowOf(PagingData.empty<com.aorrico.mymbchallenge.domain.model.Exchange>())
        every { repository.getExchanges() } returns flow

        val result = GetExchangesUseCase(repository)()

        assertThat(result).isSameInstanceAs(flow)
    }

    @Test
    fun `GetExchangeDetailUseCase delegates to the repository with the given id`() = runTest {
        val detail = mockk<ExchangeDetail>()
        coEvery { repository.getExchangeDetail(24) } returns AppResult.Success(detail)

        val result = GetExchangeDetailUseCase(repository)(24)

        assertThat(result).isEqualTo(AppResult.Success(detail))
    }

    @Test
    fun `GetExchangeAssetsUseCase delegates to the repository with the given id`() = runTest {
        val assets = listOf(mockk<CryptoAsset>())
        coEvery { repository.getExchangeAssets(270) } returns AppResult.Success(assets)

        val result = GetExchangeAssetsUseCase(repository)(270)

        assertThat(result).isEqualTo(AppResult.Success(assets))
    }
}
