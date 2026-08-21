package com.aorrico.mymbchallenge.core.network.connectivity

import javax.inject.Qualifier

/** A [kotlinx.coroutines.CoroutineScope] that lives as long as the process, for singletons like [AndroidConnectivityObserver]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
