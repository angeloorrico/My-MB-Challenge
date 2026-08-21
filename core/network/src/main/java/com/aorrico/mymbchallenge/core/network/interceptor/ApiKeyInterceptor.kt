package com.aorrico.mymbchallenge.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/** Attaches the CoinMarketCap Pro API key header CMC requires on every request. */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-CMC_PRO_API_KEY", apiKey)
            .addHeader("Accept", "application/json")
            .build()
        return chain.proceed(request)
    }
}
