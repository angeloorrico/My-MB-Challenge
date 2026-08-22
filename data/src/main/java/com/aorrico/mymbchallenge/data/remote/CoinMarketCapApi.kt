package com.aorrico.mymbchallenge.data.remote

import com.aorrico.mymbchallenge.data.remote.dto.CmcEnvelope
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeAssetDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeInfoDto
import com.aorrico.mymbchallenge.data.remote.dto.ExchangeMapItemDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CoinMarketCapApi {

    /**
     * Cheap, 1-credit call that returns just id/name/slug. [start] is 1-indexed, matching CMC's
     * pagination convention. Free-tier keys cannot use /v1/exchange/listings/latest, so this
     * endpoint - sorted by volume - is used to pick which page of exchanges to fetch full data for.
     */
    @GET("v1/exchange/map")
    suspend fun getExchangeMap(
        @Query("listing_status") listingStatus: String = "active",
        @Query("sort") sort: String = "volume_24h",
        @Query("start") start: Int,
        @Query("limit") limit: Int,
    ): CmcEnvelope<List<ExchangeMapItemDto>>

    /**
     * Accepts a comma-separated id list and returns all of them in a single credit, keyed by id
     * as strings in the response map. Used both to enrich a listing page and to fetch one
     * exchange's detail.
     */
    @GET("v1/exchange/info")
    suspend fun getExchangeInfo(
        @Query("id") ids: String,
    ): CmcEnvelope<Map<String, ExchangeInfoDto>>

    @GET("v1/exchange/assets")
    suspend fun getExchangeAssets(
        @Query("id") id: Long,
    ): CmcEnvelope<List<ExchangeAssetDto>>
}
