package com.aorrico.mymbchallenge.data.mapper

import com.aorrico.mymbchallenge.data.remote.dto.ExchangeAssetCurrencyDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeAssetDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeInfoDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeUrlsDto
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.Instant

class ExchangeMappersTest {

    private fun exchangeInfoDto(
        logo: String? = "https://example.com/logo.png",
        dateLaunched: String? = "2011-07-28T00:00:00.000Z",
        website: List<String>? = listOf("https://kraken.com"),
        description: String? = "A description",
    ) = ExchangeInfoDto(
        id = 24,
        name = "Kraken",
        slug = "kraken",
        description = description,
        logo = logo,
        urls = ExchangeUrlsDto(website = website),
        dateLaunched = dateLaunched,
        makerFee = 0.02,
        takerFee = 0.05,
        spotVolumeUsd = 2_208_013_958.83,
    )

    @Test
    fun `toExchange maps all fields`() {
        val exchange = exchangeInfoDto().toExchange()

        assertThat(exchange.id).isEqualTo(24)
        assertThat(exchange.name).isEqualTo("Kraken")
        assertThat(exchange.slug).isEqualTo("kraken")
        assertThat(exchange.logoUrl).isEqualTo("https://example.com/logo.png")
        assertThat(exchange.spotVolumeUsd).isEqualTo(2_208_013_958.83)
        assertThat(exchange.dateLaunched).isEqualTo(Instant.parse("2011-07-28T00:00:00.000Z"))
    }

    @Test
    fun `toExchange treats blank logo as null`() {
        val exchange = exchangeInfoDto(logo = "").toExchange()

        assertThat(exchange.logoUrl).isNull()
    }

    @Test
    fun `toExchange treats null date_launched as null instant`() {
        val exchange = exchangeInfoDto(dateLaunched = null).toExchange()

        assertThat(exchange.dateLaunched).isNull()
    }

    @Test
    fun `toExchange treats unparseable date_launched as null instead of throwing`() {
        val exchange = exchangeInfoDto(dateLaunched = "not-a-date").toExchange()

        assertThat(exchange.dateLaunched).isNull()
    }

    @Test
    fun `toExchangeDetail picks the first non-blank website url`() {
        val detail = exchangeInfoDto(website = listOf("", "https://kraken.com", "https://other.com")).toExchangeDetail()

        assertThat(detail.websiteUrl).isEqualTo("https://kraken.com")
    }

    @Test
    fun `toExchangeDetail maps null website list to null url`() {
        val detail = exchangeInfoDto(website = null).toExchangeDetail()

        assertThat(detail.websiteUrl).isNull()
    }

    @Test
    fun `toExchangeDetail treats blank description as null`() {
        val detail = exchangeInfoDto(description = "").toExchangeDetail()

        assertThat(detail.description).isNull()
    }

    @Test
    fun `toExchangeDetail strips Markdown syntax from the description`() {
        val raw = "## What Is Binance?\n\nBinance is the **largest** exchange. See [docs](https://x.com)."
        val detail = exchangeInfoDto(description = raw).toExchangeDetail()

        assertThat(detail.description).isEqualTo(
            "What Is Binance?\n\nBinance is the largest exchange. See docs.",
        )
    }

    @Test
    fun `toCryptoAssets drops rows missing name, symbol or price`() {
        val assets = listOf(
            ExchangeAssetDto(
                walletAddress = "0x1",
                balance = 1.0,
                currency = ExchangeAssetCurrencyDto(cryptoId = 1, symbol = "BTC", name = "Bitcoin", priceUsd = 65000.0),
            ),
            ExchangeAssetDto(
                walletAddress = "0x2",
                balance = 2.0,
                currency = ExchangeAssetCurrencyDto(cryptoId = 2, symbol = null, name = "Missing Symbol", priceUsd = 1.0),
            ),
            ExchangeAssetDto(walletAddress = "0x3", balance = 3.0, currency = null),
        ).toCryptoAssets()

        assertThat(assets).hasSize(1)
        assertThat(assets.single().symbol).isEqualTo("BTC")
    }

    @Test
    fun `toCryptoAssets de-duplicates the same currency held across multiple wallets`() {
        val usdt = ExchangeAssetCurrencyDto(cryptoId = 825, symbol = "USDT", name = "Tether", priceUsd = 1.0)
        val assets = listOf(
            ExchangeAssetDto(walletAddress = "0x1", balance = 100.0, currency = usdt),
            ExchangeAssetDto(walletAddress = "0x2", balance = 200.0, currency = usdt),
        ).toCryptoAssets()

        assertThat(assets).hasSize(1)
    }

    @Test
    fun `toCryptoAssets on an empty proof-of-reserve list returns an empty list, not an error`() {
        val assets = emptyList<ExchangeAssetDto>().toCryptoAssets()

        assertThat(assets).isEmpty()
    }
}
