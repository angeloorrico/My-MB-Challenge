package com.aorrico.mymbchallenge.data.mapper

import com.aorrico.mymbchallenge.data.remote.dto.ExchangeAssetDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeInfoDto
import com.aorrico.mymbchallenge.domain.model.CryptoAsset
import com.aorrico.mymbchallenge.domain.model.Exchange
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import java.time.Instant
import java.time.format.DateTimeParseException

fun ExchangeInfoDto.toExchange(): Exchange = Exchange(
    id = id,
    name = name,
    slug = slug,
    logoUrl = logo?.takeIf { it.isNotBlank() },
    spotVolumeUsd = spotVolumeUsd,
    dateLaunched = dateLaunched.toInstantOrNull(),
)

fun ExchangeInfoDto.toExchangeDetail(): ExchangeDetail = ExchangeDetail(
    id = id,
    name = name,
    logoUrl = logo?.takeIf { it.isNotBlank() },
    description = description?.stripMarkdown()?.takeIf { it.isNotBlank() },
    websiteUrl = urls?.website?.firstOrNull { it.isNotBlank() },
    makerFee = makerFee,
    takerFee = takerFee,
    dateLaunched = dateLaunched.toInstantOrNull(),
)

/**
 * Maps proof-of-reserve wallet rows to the currencies the detail screen lists. The same currency
 * can appear once per wallet (e.g. USDT held across several chains) - the screen shows each
 * currency once, so entries are de-duplicated by [ExchangeAssetDto.currency]'s crypto_id,
 * preferring the first occurrence. Rows missing a name, symbol or price are dropped: they can't
 * be rendered meaningfully and the API does not guarantee those fields are always present.
 */
fun List<ExchangeAssetDto>.toCryptoAssets(): List<CryptoAsset> = this
    .mapNotNull { it.currency }
    .distinctBy { it.cryptoId ?: it.symbol }
    .mapNotNull { currency ->
        val name = currency.name?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val symbol = currency.symbol?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
        val price = currency.priceUsd ?: return@mapNotNull null
        CryptoAsset(symbol = symbol, name = name, priceUsd = price)
    }

// CoinMarketCap's `description` field is Markdown (headings, bold, links). The detail screen
// renders it as plain text rather than pulling in a Markdown renderer for one field, so the
// syntax markers are stripped instead of being shown to the user literally (e.g. "## About").
private val markdownHeadingRegex = Regex("(?m)^#{1,6}\\s*")
private val markdownBoldItalicRegex = Regex("\\*{1,3}([^*]+)\\*{1,3}")
private val markdownLinkRegex = Regex("\\[([^]]+)]\\([^)]+\\)")

private fun String.stripMarkdown(): String = this
    .replace(markdownLinkRegex, "$1")
    .replace(markdownBoldItalicRegex, "$1")
    .replace(markdownHeadingRegex, "")
    .replace(Regex("\n{3,}"), "\n\n")
    .trim()

private fun String?.toInstantOrNull(): Instant? {
    if (this.isNullOrBlank()) return null
    return try {
        Instant.parse(this)
    } catch (e: DateTimeParseException) {
        null
    }
}
