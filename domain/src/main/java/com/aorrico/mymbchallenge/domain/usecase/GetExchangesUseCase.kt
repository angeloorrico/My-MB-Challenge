package com.aorrico.mymbchallenge.domain.usecase

import androidx.paging.PagingData
import com.aorrico.mymbchallenge.domain.model.Exchange
import com.aorrico.mymbchallenge.domain.repository.ExchangeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetExchangesUseCase @Inject constructor(
    private val repository: ExchangeRepository,
) {
    operator fun invoke(): Flow<PagingData<Exchange>> = repository.getExchanges()
}
