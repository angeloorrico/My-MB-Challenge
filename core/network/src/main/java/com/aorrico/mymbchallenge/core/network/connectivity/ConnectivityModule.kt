package com.aorrico.mymbchallenge.core.network.connectivity

import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import com.aorrico.mymbchallenge.core.common.dispatcher.DefaultDispatcherProvider
import com.aorrico.mymbchallenge.core.common.dispatcher.DispatcherProvider
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ConnectivityModule {
    @Binds
    @Singleton
    abstract fun bindConnectivityObserver(impl: AndroidConnectivityObserver): ConnectivityObserver

    // First real consumer of DispatcherProvider - it existed since the initial scaffolding but
    // nothing needed it injected as an interface until this module did, so this is also where
    // its @Binds first became necessary.
    @Binds
    @Singleton
    abstract fun bindDispatcherProvider(impl: DefaultDispatcherProvider): DispatcherProvider

    companion object {
        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(dispatcherProvider: DispatcherProvider): CoroutineScope =
            CoroutineScope(SupervisorJob() + dispatcherProvider.default)
    }
}
