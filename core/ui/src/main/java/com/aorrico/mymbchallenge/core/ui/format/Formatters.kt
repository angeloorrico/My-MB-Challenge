package com.aorrico.mymbchallenge.core.ui.format

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

private val usSymbols = DecimalFormatSymbols(Locale.US)

/**
 * Compact USD notation for large figures like spot_volume_usd (e.g. "$2.21B"). Falls back to
 * plain grouped notation below one million, where compact suffixes would be misleading.
 */
fun formatCompactUsd(value: Double?): String {
    if (value == null) return "—"
    val abs = kotlin.math.abs(value)
    val (divisor, suffix) = when {
        abs >= 1_000_000_000_000.0 -> 1_000_000_000_000.0 to "T"
        abs >= 1_000_000_000.0 -> 1_000_000_000.0 to "B"
        abs >= 1_000_000.0 -> 1_000_000.0 to "M"
        else -> 1.0 to ""
    }
    val pattern = if (suffix.isEmpty()) "#,##0" else "#,##0.00"
    val formatted = DecimalFormat(pattern, usSymbols).format(value / divisor)
    return "$$formatted$suffix"
}

/**
 * Crypto prices span many orders of magnitude (a $0.00000001 memecoin next to a $100k BTC), so
 * the number of decimal places shown adapts to the magnitude instead of a fixed 2dp that would
 * round small prices to "$0.00".
 */
fun formatCryptoPriceUsd(value: Double?): String {
    if (value == null) return "—"
    val abs = kotlin.math.abs(value)
    val decimals = when {
        abs == 0.0 -> 2
        abs >= 1.0 -> 2
        abs >= 0.01 -> 4
        abs >= 0.0001 -> 6
        else -> 8
    }
    val pattern = "#,##0." + "0".repeat(decimals)
    val formatted = DecimalFormat(pattern, usSymbols).apply {
        roundingMode = RoundingMode.HALF_UP
    }.format(value)
    return "$$formatted"
}

/**
 * CoinMarketCap reports maker_fee/taker_fee already as a percentage value (e.g. `0.05` means
 * "0.05%", not "5%") - confirmed against live exchange/info responses, where Kraken's published
 * 0.00%-0.26% fee range matches the raw field values directly with no *100 conversion needed.
 */
fun formatFeePercent(value: Double?): String {
    if (value == null) return "—"
    val pattern = DecimalFormat("#,##0.####", usSymbols)
    return "${pattern.format(value)}%"
}

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US).withZone(ZoneOffset.UTC)

fun formatLaunchDate(instant: Instant?): String {
    if (instant == null) return "—"
    return dateFormatter.format(instant)
}
