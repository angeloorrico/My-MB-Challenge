package com.aorrico.mymbchallenge.feature.exchangedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.core.ui.R as CoreUiR
import com.aorrico.mymbchallenge.core.ui.components.FullScreenError
import com.aorrico.mymbchallenge.core.ui.components.FullScreenLoading
import com.aorrico.mymbchallenge.core.ui.error.toDisplayMessage
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import com.aorrico.mymbchallenge.feature.exchangedetail.components.CryptoAssetRow
import com.aorrico.mymbchallenge.feature.exchangedetail.components.ExchangeDetailHeader

/**
 * @param exchangeId which exchange to show. Each id gets its own retained ViewModel instance
 *   (keyed below), so switching back to an exchange already viewed this session - or rotating
 *   the device - doesn't re-trigger the network calls.
 * @param showBackButton hide the back affordance when this pane sits next to the list pane
 *   (two-pane layout), where there's nothing to navigate back to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExchangeDetailRoute(
    exchangeId: Long,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true,
    viewModel: ExchangeDetailViewModel = hiltViewModel<ExchangeDetailViewModel, ExchangeDetailViewModel.Factory>(
        key = "exchange-detail-$exchangeId",
    ) { factory -> factory.create(exchangeId) },
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.exchange_detail_title)) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.action_back),
                            )
                        }
                    }
                },
            )
        },
    ) { padding ->
        when (val info = uiState.info) {
            is InfoState.Loading -> FullScreenLoading(modifier = Modifier.padding(padding).fillMaxSize())

            is InfoState.Error -> FullScreenError(
                message = info.error.toDisplayMessage(),
                onRetry = viewModel::retryInfo,
                modifier = Modifier.padding(padding).fillMaxSize(),
            )

            is InfoState.Success -> ExchangeDetailContent(
                detail = info.detail,
                assetsState = uiState.assets,
                onRetryAssets = viewModel::retryAssets,
                modifier = Modifier.padding(padding).fillMaxSize(),
            )
        }
    }
}

@Composable
internal fun ExchangeDetailContent(
    detail: ExchangeDetail,
    assetsState: AssetsState,
    onRetryAssets: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(modifier = modifier) {
        item { ExchangeDetailHeader(detail = detail) }

        when (assetsState) {
            is AssetsState.Loading -> item { AssetsLoadingRow() }

            is AssetsState.Error -> item {
                AssetsErrorRow(error = assetsState.error, onRetry = onRetryAssets)
            }

            is AssetsState.Success -> if (assetsState.assets.isEmpty()) {
                item { AssetsEmptyRow() }
            } else {
                items(assetsState.assets, key = { it.symbol }) { asset ->
                    CryptoAssetRow(asset = asset)
                }
            }
        }
    }
}

@Composable
private fun AssetsLoadingRow() {
    Box(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(24.dp).align(Alignment.Center))
    }
}

@Composable
private fun AssetsEmptyRow() {
    Text(
        text = stringResource(R.string.detail_assets_empty),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
    )
}

@Composable
private fun AssetsErrorRow(error: AppError, onRetry: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = error.toDisplayMessage(),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(end = 8.dp),
        )
        TextButton(onClick = onRetry) {
            Text(stringResource(CoreUiR.string.action_retry))
        }
    }
}
