package com.aorrico.mymbchallenge.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Row from GET /v1/exchange/assets - one proof-of-reserve wallet holding one currency. */
@JsonClass(generateAdapter = true)
data class ExchangeAssetDto(
    @Json(name = "wallet_address") val walletAddress: String?,
    val balance: Double?,
    val currency: ExchangeAssetCurrencyDto?,
)

@JsonClass(generateAdapter = true)
data class ExchangeAssetCurrencyDto(
    @Json(name = "crypto_id") val cryptoId: Long?,
    val symbol: String?,
    val name: String?,
    @Json(name = "price_usd") val priceUsd: Double?,
)
