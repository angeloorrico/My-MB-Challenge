package com.aorrico.mymbchallenge.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** Every CoinMarketCap response is wrapped in this envelope, success or failure alike. */
@JsonClass(generateAdapter = true)
data class CmcEnvelope<T>(
    val status: CmcStatusDto,
    val data: T?,
)

@JsonClass(generateAdapter = true)
data class CmcStatusDto(
    @Json(name = "error_code") val errorCode: Int,
    @Json(name = "error_message") val errorMessage: String?,
)

/** Shape of an error response body - same envelope, but we only ever need the status block. */
@JsonClass(generateAdapter = true)
data class ErrorEnvelopeDto(
    val status: CmcStatusDto,
)
