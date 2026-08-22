package com.aorrico.mymbchallenge.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Lightweight row from GET /v1/exchange/map - just enough to know which IDs exist and their order. */
@JsonClass(generateAdapter = true)
data class ExchangeMapItemDto(
    val id: Long,
    val name: String,
    val slug: String,
    @Json(name = "is_active") val isActive: Int?,
)
