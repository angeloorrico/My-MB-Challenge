package com.aorrico.mymbchallenge.domain.usecase

import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.aorrico.mymbchallenge.domain.repository.RecentlyViewedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveRecentlyViewedExchangesUseCase @Inject constructor(
    private val repository: RecentlyViewedRepository,
) {
    operator fun invoke(): Flow<List<RecentlyViewedExchange>> = repository.observeRecentlyViewed()
}
