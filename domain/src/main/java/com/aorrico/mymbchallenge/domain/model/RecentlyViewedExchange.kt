package com.aorrico.mymbchallenge.domain.model

import java.time.Instant

/**
 * A locally persisted record of an exchange the user has successfully opened, shown as a
 * shortcut row above the list. Only holds display fields that don't go stale (name, logo,
 * [viewedAt]) - no spot volume or anything else volatile. This is a "jump back in" shortcut,
 * not a data source of record.
 */
data class RecentlyViewedExchange(
    val exchangeId: Long,
    val name: String,
    val logoUrl: String?,
    val viewedAt: Instant,
)
