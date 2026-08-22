package com.aorrico.mymbchallenge.domain.model

/**
 * A cryptocurrency reported in an exchange's proof-of-reserves wallets. Not every exchange
 * publishes this - an empty list is a normal response, not an error (Kraken, for instance,
 * currently reports zero proof-of-reserve assets).
 */
data class CryptoAsset(
    val symbol: String,
    val name: String,
    val priceUsd: Double,
)
