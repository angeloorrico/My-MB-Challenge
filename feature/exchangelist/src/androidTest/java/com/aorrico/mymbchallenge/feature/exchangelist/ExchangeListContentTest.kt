package com.aorrico.mymbchallenge.feature.exchangelist

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.aorrico.mymbchallenge.domain.model.Exchange
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import java.time.Instant

/**
 * Exercises the pure, ViewModel-free content composable directly - no Hilt/network wiring needed
 * to verify the list renders items and reflects the three loading/error/empty states correctly.
 */
class ExchangeListContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun exchange(id: Long, name: String) = Exchange(
        id = id,
        name = name,
        slug = name.lowercase(),
        logoUrl = null,
        spotVolumeUsd = 1_000_000_000.0,
        dateLaunched = Instant.parse("2017-01-01T00:00:00Z"),
    )

    @Test
    fun rendersExchangeNamesAndInvokesClickCallback() {
        var clickedId: Long? = null

        composeRule.setContent {
            val items = flowOf(PagingData.from(listOf(exchange(270, "Binance"), exchange(24, "Kraken"))))
                .collectAsLazyPagingItems()
            ExchangeListContent(exchanges = items, onExchangeClick = { clickedId = it.id })
        }

        composeRule.onNodeWithText("Binance").assertExists()
        composeRule.onNodeWithText("Kraken").assertExists()

        composeRule.onNodeWithText("Binance").performClick()
        assertEquals(270L, clickedId)
    }

    @Test
    fun showsEmptyStateWhenThereAreNoExchanges() {
        composeRule.setContent {
            val items = flowOf(
                PagingData.from<Exchange>(
                    emptyList(),
                    sourceLoadStates = LoadStates(
                        refresh = LoadState.NotLoading(endOfPaginationReached = true),
                        prepend = LoadState.NotLoading(endOfPaginationReached = true),
                        append = LoadState.NotLoading(endOfPaginationReached = true),
                    ),
                ),
            ).collectAsLazyPagingItems()
            ExchangeListContent(exchanges = items, onExchangeClick = {})
        }

        composeRule.onNodeWithText("Nenhuma exchange encontrada.").assertExists()
    }

    @Test
    fun showsRetryButtonWhenTheInitialLoadFails() {
        composeRule.setContent {
            val items = flowOf(
                PagingData.from<Exchange>(
                    emptyList(),
                    sourceLoadStates = LoadStates(
                        refresh = LoadState.Error(IOException("boom")),
                        prepend = LoadState.NotLoading(endOfPaginationReached = false),
                        append = LoadState.NotLoading(endOfPaginationReached = false),
                    ),
                ),
            ).collectAsLazyPagingItems()
            ExchangeListContent(exchanges = items, onExchangeClick = {})
        }

        composeRule.onNodeWithText("Tentar novamente").assertExists()
    }
}
