package com.aorrico.mymbchallenge.data.remote

import com.aorrico.mymbchallenge.core.common.connectivity.ConnectivityObserver
import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.aorrico.mymbchallenge.data.remote.dto.CmcEnvelope
import com.aorrico.mymbchallenge.data.remote.dto.ErrorEnvelopeDto
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

/**
 * Runs a Retrofit call and normalizes every failure mode into [AppResult]/[AppError] so
 * repositories never leak Retrofit/OkHttp/Moshi types to the domain layer.
 *
 * CoinMarketCap reports failures two different ways (confirmed against the live API): plan/auth/
 * validation problems (401, 400, 403...) come back as real HTTP error statuses with a JSON body
 * describing the CMC error code, but other failures arrive as HTTP 200 with
 * `status.error_code != 0` in the body instead. Both are handled here.
 *
 * [ConnectivityObserver] is checked before every call purely for speed: without it, a
 * disconnected device would still sit through OkHttp's full connect timeout before landing on the
 * exact same [AppError.NoConnectivity] this returns instantly instead.
 */
class SafeApiCallExecutor @Inject constructor(
    private val moshi: Moshi,
    private val connectivityObserver: ConnectivityObserver,
) {
    suspend fun <T> execute(apiCall: suspend () -> CmcEnvelope<T>): AppResult<T> {
        if (!connectivityObserver.isConnected.value) {
            return AppResult.Error(AppError.NoConnectivity())
        }
        return runCatch(apiCall)
    }

    private suspend fun <T> runCatch(apiCall: suspend () -> CmcEnvelope<T>): AppResult<T> = try {
        val envelope = apiCall()
        when {
            envelope.status.errorCode != 0 ->
                AppResult.Error(AppError.Api(envelope.status.errorCode, envelope.status.errorMessage))

            envelope.data == null ->
                AppResult.Error(AppError.Parsing("CoinMarketCap returned a successful status with no data"))

            else -> AppResult.Success(envelope.data)
        }
    } catch (e: HttpException) {
        AppResult.Error(e.toAppError())
    } catch (e: SocketTimeoutException) {
        // Must be caught ahead of the IOException branch below: SocketTimeoutException is an
        // IOException, and Kotlin/Java catch clauses match in declaration order.
        AppResult.Error(AppError.Timeout(e.message))
    } catch (e: IOException) {
        AppResult.Error(AppError.NoConnectivity(e.message))
    } catch (e: JsonDataException) {
        AppResult.Error(AppError.Parsing(e.message))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        AppResult.Error(AppError.Unknown(e.message))
    }

    private fun HttpException.toAppError(): AppError {
        val body = runCatching { response()?.errorBody()?.string() }.getOrNull()
        val status = body?.let {
            runCatching { moshi.adapter(ErrorEnvelopeDto::class.java).fromJson(it)?.status }.getOrNull()
        }
        return if (status != null) {
            AppError.Api(status.errorCode, status.errorMessage)
        } else {
            AppError.Http(code(), message())
        }
    }
}
