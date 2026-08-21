package com.aorrico.mymbchallenge.core.ui.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.core.common.error.CmcErrorCode
import com.aorrico.mymbchallenge.core.ui.R

/** Turns a normalized [AppError] into a message a user (not a developer) should read. */
@Composable
fun AppError.toDisplayMessage(): String = when (this) {
    is AppError.NoConnectivity -> stringResource(R.string.error_no_connectivity)
    is AppError.Timeout -> stringResource(R.string.error_timeout)

    is AppError.Api -> when (code) {
        CmcErrorCode.RATE_LIMIT_MINUTE,
        CmcErrorCode.RATE_LIMIT_DAY,
        CmcErrorCode.RATE_LIMIT_MONTH,
        -> stringResource(R.string.error_rate_limit)

        CmcErrorCode.PLAN_UNSUPPORTED_ENDPOINT -> stringResource(R.string.error_plan_restricted)

        CmcErrorCode.INVALID_OR_MISSING_KEY,
        CmcErrorCode.KEY_MISSING,
        -> stringResource(R.string.error_invalid_key)

        else -> message ?: stringResource(R.string.error_generic)
    }

    is AppError.Http -> stringResource(R.string.error_http, code)
    is AppError.Parsing -> stringResource(R.string.error_parsing)
    is AppError.Unknown -> stringResource(R.string.error_generic)
}
