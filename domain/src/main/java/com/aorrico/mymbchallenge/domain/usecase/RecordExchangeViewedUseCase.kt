package com.aorrico.mymbchallenge.domain.usecase

import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import com.aorrico.mymbchallenge.domain.repository.RecentlyViewedRepository
import javax.inject.Inject

class RecordExchangeViewedUseCase @Inject constructor(
    private val repository: RecentlyViewedRepository,
) {
    suspend operator fun invoke(exchange: RecentlyViewedExchange) = repository.recordView(exchange)
}
