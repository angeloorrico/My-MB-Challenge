package com.aorrico.mymbchallenge.data.di

import com.aorrico.mymbchallenge.data.remote.CoinMarketCapApi
import com.aorrico.mymbchallenge.data.repository.ExchangeRepositoryImpl
import com.aorrico.mymbchallenge.domain.repository.ExchangeRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideCoinMarketCapApi(retrofit: Retrofit): CoinMarketCapApi =
        retrofit.create(CoinMarketCapApi::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindExchangeRepository(impl: ExchangeRepositoryImpl): ExchangeRepository
}
