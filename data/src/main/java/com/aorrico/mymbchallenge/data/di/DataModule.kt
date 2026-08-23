package com.aorrico.mymbchallenge.data.di

import android.content.Context
import androidx.room.Room
import com.aorrico.mymbchallenge.data.local.AppDatabase
import com.aorrico.mymbchallenge.data.local.dao.RecentlyViewedExchangeDao
import com.aorrico.mymbchallenge.data.remote.CoinMarketCapApi
import com.aorrico.mymbchallenge.data.repository.ExchangeRepositoryImpl
import com.aorrico.mymbchallenge.data.repository.RecentlyViewedRepositoryImpl
import com.aorrico.mymbchallenge.domain.repository.ExchangeRepository
import com.aorrico.mymbchallenge.domain.repository.RecentlyViewedRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "my-mb-challenge.db").build()

    @Provides
    @Singleton
    fun provideRecentlyViewedExchangeDao(database: AppDatabase): RecentlyViewedExchangeDao =
        database.recentlyViewedExchangeDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindExchangeRepository(impl: ExchangeRepositoryImpl): ExchangeRepository

    @Binds
    @Singleton
    abstract fun bindRecentlyViewedRepository(impl: RecentlyViewedRepositoryImpl): RecentlyViewedRepository
}
