package com.aorrico.mymbchallenge.feature.exchangelist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.aorrico.mymbchallenge.core.common.error.toAppError
import com.aorrico.mymbchallenge.core.ui.R as CoreUiR
import com.aorrico.mymbchallenge.core.ui.components.FullScreenEmpty
import com.aorrico.mymbchallenge.core.ui.components.FullScreenError
import com.aorrico.mymbchallenge.core.ui.components.FullScreenLoading
import com.aorrico.mymbchallenge.core.ui.error.toDisplayMessage
import com.aorrico.mymbchallenge.domain.model.Exchange
import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.aorrico.mymbchallenge.feature.exchangelist.components.ExchangeListItem
import com.aorrico.mymbchallenge.feature.exchangelist.components.RecentlyViewedRow

/**
 * @param listState hoist this from a caller that needs the scroll position to survive beyond
 *   this composable's own lifetime (e.g. across a list/detail pane rearrangement on rotation) -
 *   left as [rememberLazyListState] by default for standalone use.
 * @param selectedExchangeId highlights the corresponding row; used in a two-pane layout to show
 *   which exchange the detail pane is currently displaying. Null when nothing needs highlighting
 *   (e.g. single-pane layout, where the row is just navigated away from).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeListRoute(
    onExchangeClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    selectedExchangeId: Long? = null,
    viewModel: ExchangeListViewModel = hiltViewModel(),
) {
    val exchanges = viewModel.exchanges.collectAsLazyPagingItems()
    val recentlyViewed by viewModel.recentlyViewed.collectAsState()

    // A newly-viewed exchange prepends a row to the LazyColumn, but LazyColumn keeps whatever was
    // already on screen pinned in place - so the new row ends up scrolled above the fold instead
    // of visible. Nudge back to the top when that happens, but only if the user hadn't scrolled
    // away already (don't yank their position around mid-browse).
    LaunchedEffect(recentlyViewed.firstOrNull()?.exchangeId) {
        if (recentlyViewed.isNotEmpty() && listState.firstVisibleItemIndex <= 1) {
            listState.scrollToItem(0)
        }
    }

    // Paging's retry() lives on LazyPagingItems, which only exists here in the UI layer - the
    // ViewModel just exposes raw connectivity state. Only reacting to the false->true edge (not
    // every recomposition where isConnected happens to be true) avoids retrying a load that
    // failed for an unrelated reason every time this composable recomposes.
    val isConnected by viewModel.isConnected.collectAsState()
    var wasDisconnected by remember { mutableStateOf(false) }
    LaunchedEffect(isConnected) {
        val justReconnected = isConnected && wasDisconnected
        wasDisconnected = !isConnected
        if (justReconnected &&
            (exchanges.loadState.refresh is LoadState.Error || exchanges.loadState.append is LoadState.Error)
        ) {
            exchanges.retry()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.exchange_list_title)) })
        },
    ) { padding ->
        ExchangeListContent(
            exchanges = exchanges,
            onExchangeClick = { onExchangeClick(it.id) },
            recentlyViewed = recentlyViewed,
            onRecentlyViewedClick = onExchangeClick,
            listState = listState,
            selectedExchangeId = selectedExchangeId,
            modifier = Modifier.padding(padding),
        )
    }
}

@Composable
internal fun ExchangeListContent(
    exchanges: LazyPagingItems<Exchange>,
    onExchangeClick: (Exchange) -> Unit,
    modifier: Modifier = Modifier,
    recentlyViewed: List<RecentlyViewedExchange> = emptyList(),
    onRecentlyViewedClick: (Long) -> Unit = {},
    listState: LazyListState = rememberLazyListState(),
    selectedExchangeId: Long? = null,
) {
    val refreshState = exchanges.loadState.refresh

    when {
        refreshState is LoadState.Loading && exchanges.itemCount == 0 -> {
            FullScreenLoading(modifier = modifier.fillMaxSize())
        }

        refreshState is LoadState.Error && exchanges.itemCount == 0 -> {
            FullScreenError(
                message = refreshState.error.toAppError().toDisplayMessage(),
                onRetry = { exchanges.retry() },
                modifier = modifier.fillMaxSize(),
            )
        }

        refreshState is LoadState.NotLoading && exchanges.itemCount == 0 -> {
            FullScreenEmpty(
                message = stringResource(R.string.exchange_list_empty),
                modifier = modifier.fillMaxSize(),
            )
        }

        else -> {
            LazyColumn(modifier = modifier.fillMaxSize(), state = listState) {
                if (recentlyViewed.isNotEmpty()) {
                    item(key = "recently_viewed") {
                        RecentlyViewedRow(
                            exchanges = recentlyViewed,
                            onExchangeClick = onRecentlyViewedClick,
                        )
                        HorizontalDivider()
                    }
                }

                items(
                    count = exchanges.itemCount,
                    key = exchanges.itemKey { it.id },
                ) { index ->
                    val exchange = exchanges[index]
                    if (exchange != null) {
                        ExchangeListItem(
                            exchange = exchange,
                            onClick = onExchangeClick,
                            selected = exchange.id == selectedExchangeId,
                        )
                        HorizontalDivider()
                    }
                }

                item {
                    AppendStateFooter(loadState = exchanges.loadState.append, onRetry = { exchanges.retry() })
                }
            }
        }
    }
}

@Composable
private fun AppendStateFooter(loadState: LoadState, onRetry: () -> Unit) {
    when (loadState) {
        is LoadState.Loading -> Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center),
            )
        }

        is LoadState.Error -> Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = loadState.error.toAppError().toDisplayMessage(),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = onRetry) {
                Text(stringResource(CoreUiR.string.action_retry))
            }
        }

        is LoadState.NotLoading -> Unit
    }
}
