package com.aorrico.mymbchallenge.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.aorrico.mymbchallenge.R
import com.aorrico.mymbchallenge.core.ui.components.FullScreenEmpty
import com.aorrico.mymbchallenge.core.ui.components.OfflineBanner
import com.aorrico.mymbchallenge.feature.exchangedetail.ExchangeDetailRoute
import com.aorrico.mymbchallenge.feature.exchangedetail.R as ExchangeDetailR
import com.aorrico.mymbchallenge.feature.exchangelist.ExchangeListRoute
import com.aorrico.mymbchallenge.feature.exchangelist.ExchangeListViewModel
import kotlinx.coroutines.launch

/**
 * Root screen: a canonical list-detail layout that collapses to a single pane (list, then detail
 * pushed on top) on a compact window and expands to list-left/detail-right side by side once the
 * window is wide enough - which in practice covers rotating a phone to landscape, without hard
 * -coding orientation (a tablet in portrait is often wide enough for two panes too, and a small
 * phone's landscape width might not be - width is the correct signal, not orientation).
 *
 * Both the exchange list's ViewModel and its scroll position are hoisted here, above where the
 * pane arrangement is decided, specifically so that rotating between one and two panes reuses the
 * same instances instead of recreating them - which is what makes "don't re-fetch on rotation"
 * and "keep the scroll position" hold even while the pane layout itself is changing.
 *
 * The scaffold directive uses [calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth] rather
 * than the plain default (which only splits into two panes at the Expanded width class, 840dp+):
 * plenty of phones land in the Medium class (600-840dp) once rotated to landscape - this Pixel
 * profile's landscape width is ~731dp, for instance - and the goal here is "landscape on an
 * ordinary phone shows two panes," not "only tablet-sized windows do."
 */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExchangeListDetailScreen(modifier: Modifier = Modifier) {
    val navigator = rememberListDetailPaneScaffoldNavigator<Long>(
        scaffoldDirective = calculatePaneScaffoldDirectiveWithTwoPanesOnMediumWidth(currentWindowAdaptiveInfo()),
    )
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = navigator.canNavigateBack()) {
        coroutineScope.launch { navigator.navigateBack() }
    }

    val listViewModel: ExchangeListViewModel = hiltViewModel()
    val listState: LazyListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    val connectivityViewModel: ConnectivityViewModel = hiltViewModel()
    val isConnected by connectivityViewModel.isConnected.collectAsState()

    Column(modifier = modifier) {
        OfflineBanner(visible = !isConnected)

        ListDetailPaneScaffold(
            modifier = Modifier.weight(1f),
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    ExchangeListRoute(
                        onExchangeClick = { exchangeId ->
                            coroutineScope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, exchangeId)
                            }
                        },
                        viewModel = listViewModel,
                        listState = listState,
                        selectedExchangeId = navigator.currentDestination
                            ?.takeIf { it.pane == ListDetailPaneScaffoldRole.Detail }
                            ?.contentKey,
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val selectedExchangeId = navigator.currentDestination
                        ?.takeIf { it.pane == ListDetailPaneScaffoldRole.Detail }
                        ?.contentKey

                    if (selectedExchangeId != null) {
                        ExchangeDetailRoute(
                            exchangeId = selectedExchangeId,
                            onBackClick = { coroutineScope.launch { navigator.navigateBack() } },
                            // Both panes are visible at once in expanded layouts, so a "back"
                            // from the detail pane back to the list only makes sense in compact
                            // layouts.
                            showBackButton = navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Hidden,
                        )
                    } else {
                        EmptyDetailPane()
                    }
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmptyDetailPane(modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(ExchangeDetailR.string.exchange_detail_title)) }) },
    ) { padding ->
        FullScreenEmpty(
            message = stringResource(R.string.select_exchange_placeholder),
            modifier = Modifier.padding(padding).fillMaxSize(),
        )
    }
}
