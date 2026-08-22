package com.aorrico.mymbchallenge.domain.model

import java.time.Instant

/** Full exchange record shown on the detail screen. */
data class ExchangeDetail(
    val id: Long,
    val name: String,
    val logoUrl: String?,
    val description: String?,
    val websiteUrl: String?,
    val makerFee: Double?,
    val takerFee: Double?,
    val dateLaunched: Instant?,
)
