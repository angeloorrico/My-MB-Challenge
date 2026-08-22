package com.aorrico.mymbchallenge.domain.usecase

import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.domain.model.ExchangeDetail
import com.aorrico.mymbchallenge.domain.repository.ExchangeRepository
import javax.inject.Inject

class GetExchangeDetailUseCase @Inject constructor(
    private val repository: ExchangeRepository,
) {
    suspend operator fun invoke(id: Long): AppResult<ExchangeDetail> = repository.getExchangeDetail(id)
}
