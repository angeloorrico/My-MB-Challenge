package com.aorrico.mymbchallenge.data.mapper

import com.aorrico.mymbchallenge.data.local.entity.RecentlyViewedExchangeEntity
import com.aorrico.mymbchallenge.domain.model.RecentlyViewedExchange
import java.time.Instant

fun RecentlyViewedExchangeEntity.toDomain(): RecentlyViewedExchange = RecentlyViewedExchange(
    exchangeId = exchangeId,
    name = name,
    logoUrl = logoUrl,
    viewedAt = Instant.ofEpochMilli(viewedAtEpochMillis),
)

fun RecentlyViewedExchange.toEntity(): RecentlyViewedExchangeEntity = RecentlyViewedExchangeEntity(
    exchangeId = exchangeId,
    name = name,
    logoUrl = logoUrl,
    viewedAtEpochMillis = viewedAt.toEpochMilli(),
)
