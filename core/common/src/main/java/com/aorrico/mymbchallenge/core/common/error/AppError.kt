package com.aorrico.mymbchallenge.core.common.error

/**
 * Every failure a repository can surface to the presentation layer, normalized away from
 * transport-specific exceptions (Retrofit/OkHttp/Moshi) so ViewModels never depend on them.
 */
sealed class AppError(open val message: String?) {

    /** Device has no network connectivity. */
    data class NoConnectivity(override val message: String? = null) : AppError(message)

    /**
     * The connection was there, but the server didn't respond in time (connect/read/write
     * timeout - see [core.network.di.NetworkModule]'s 15s `OkHttpClient` timeouts). Separate from
     * [NoConnectivity] because the device's connection is fine here; it's just this request that
     * stalled, and the user-facing message should say so.
     */
    data class Timeout(override val message: String? = null) : AppError(message)

    /** HTTP-level failure (4xx/5xx) that isn't a recognized CoinMarketCap API error payload. */
    data class Http(val code: Int, override val message: String?) : AppError(message)

    /**
     * CoinMarketCap responded with `status.error_code != 0`. [code] mirrors CMC's own error
     * codes, e.g. 1002 (key missing), 1006 (plan doesn't support this endpoint), 1008 (rate limit).
     */
    data class Api(val code: Int, override val message: String?) : AppError(message)

    /** Response body could not be parsed into the expected shape. */
    data class Parsing(override val message: String?) : AppError(message)

    data class Unknown(override val message: String?) : AppError(message)
}

/** Bridges [AppResult]-style error handling into throw/catch APIs (e.g. Paging 3's LoadResult.Error). */
class AppErrorException(val appError: AppError) : Exception(appError.message)

fun AppError.asException(): AppErrorException = AppErrorException(this)

fun Throwable.toAppError(): AppError = (this as? AppErrorException)?.appError ?: AppError.Unknown(message)

/** CoinMarketCap-specific error codes worth branching on in the UI. */
object CmcErrorCode {
    const val INVALID_OR_MISSING_KEY = 1001
    const val KEY_MISSING = 1002
    const val PLAN_UNSUPPORTED_ENDPOINT = 1006
    const val RATE_LIMIT_MINUTE = 1008
    const val RATE_LIMIT_DAY = 1009
    const val RATE_LIMIT_MONTH = 1010
}
