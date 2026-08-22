package com.aorrico.mymbchallenge.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Full record from GET /v1/exchange/info - covers both the listing and detail screen fields. */
@JsonClass(generateAdapter = true)
data class ExchangeInfoDto(
    val id: Long,
    val name: String,
    val slug: String,
    val description: String?,
    val logo: String?,
    val urls: ExchangeUrlsDto?,
    @Json(name = "date_launched") val dateLaunched: String?,
    @Json(name = "maker_fee") val makerFee: Double?,
    @Json(name = "taker_fee") val takerFee: Double?,
    @Json(name = "spot_volume_usd") val spotVolumeUsd: Double?,
)

@JsonClass(generateAdapter = true)
data class ExchangeUrlsDto(
    val website: List<String>?,
)
