package com.aorrico.mymbchallenge.domain.usecase

import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.domain.model.CryptoAsset
import com.aorrico.mymbchallenge.domain.repository.ExchangeRepository
import javax.inject.Inject

class GetExchangeAssetsUseCase @Inject constructor(
    private val repository: ExchangeRepository,
) {
    suspend operator fun invoke(id: Long): AppResult<List<CryptoAsset>> = repository.getExchangeAssets(id)
}
