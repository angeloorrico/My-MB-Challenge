package com.aorrico.mymbchallenge.domain.model

import java.time.Instant

/** Row shown in the exchange listing screen. */
data class Exchange(
    val id: Long,
    val name: String,
    val slug: String,
    val logoUrl: String?,
    val spotVolumeUsd: Double?,
    val dateLaunched: Instant?,
)
