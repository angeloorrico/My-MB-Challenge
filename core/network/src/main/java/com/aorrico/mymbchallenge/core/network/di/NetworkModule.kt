package com.aorrico.mymbchallenge.core.network.di

import android.content.Context
import com.aorrico.mymbchallenge.core.network.BuildConfig
import com.aorrico.mymbchallenge.core.network.interceptor.ApiKeyInterceptor
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private const val BASE_URL = "https://pro-api.coinmarketcap.com/"

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides
    @Singleton
    fun provideOkHttpClient(@ApplicationContext context: Context): OkHttpClient {
        check(BuildConfig.CMC_API_KEY.isNotBlank()) {
            "Missing CoinMarketCap API key. Copy local.properties.sample to local.properties " +
                "and set CMC_API_KEY before building the app."
        }

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        // No BuildConfig.DEBUG check needed here: debug builds pull in the real Chucker library
        // (in-app HTTP inspector), release builds pull in library-no-op, and both expose the same
        // ChuckerInterceptor API - release's is just an empty pass-through.
        val chuckerInterceptor = ChuckerInterceptor.Builder(context).build()

        return OkHttpClient.Builder()
            .addInterceptor(ApiKeyInterceptor(BuildConfig.CMC_API_KEY))
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chuckerInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()
}
