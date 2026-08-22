package com.aorrico.mymbchallenge.feature.exchangedetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.domain.model.CryptoAsset
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import org.junit.Rule
import org.junit.Test

/**
 * Exercises the pure, ViewModel-free content composable directly. Info and assets are modeled
 * as independent states in production, so this specifically proves the UI renders correctly when
 * only one of the two has failed - not just the fully-happy and fully-failed paths.
 */
class ExchangeDetailContentTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val detail = ExchangeDetail(
        id = 24,
        name = "Kraken",
        logoUrl = null,
        description = "A cryptocurrency exchange",
        websiteUrl = "https://kraken.com",
        makerFee = 0.02,
        takerFee = 0.05,
        dateLaunched = null,
    )

    @Test
    fun rendersHeaderAndAssetsWhenBothSucceed() {
        composeRule.setContent {
            ExchangeDetailContent(
                detail = detail,
                assetsState = AssetsState.Success(listOf(CryptoAsset(symbol = "BTC", name = "Bitcoin", priceUsd = 65000.0))),
                onRetryAssets = {},
            )
        }

        composeRule.onNodeWithText("Kraken").assertExists()
        composeRule.onNodeWithText("Bitcoin (BTC)").assertExists()
    }

    @Test
    fun showsEmptyAssetsMessageWhenTheExchangeHasNoProofOfReserveData() {
        composeRule.setContent {
            ExchangeDetailContent(
                detail = detail,
                assetsState = AssetsState.Success(emptyList()),
                onRetryAssets = {},
            )
        }

        composeRule.onNodeWithText("Nenhuma criptomoeda associada foi divulgada por esta exchange.").assertExists()
    }

    @Test
    fun showsRetryForAssetsSectionWhileHeaderStaysVisible() {
        composeRule.setContent {
            ExchangeDetailContent(
                detail = detail,
                assetsState = AssetsState.Error(AppError.NoConnectivity()),
                onRetryAssets = {},
            )
        }

        composeRule.onNodeWithText("Kraken").assertExists()
        composeRule.onNodeWithText("Tentar novamente").assertExists()
    }
}
