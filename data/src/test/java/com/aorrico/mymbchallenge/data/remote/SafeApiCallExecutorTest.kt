package com.aorrico.mymbchallenge.data.remote

import com.aorrico.mymbchallenge.core.common.error.AppError
import com.aorrico.mymbchallenge.core.common.result.AppResult
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

/**
 * Exercises SafeApiCallExecutor against a real Retrofit stack (via MockWebServer) rather than
 * mocking HttpException directly - Retrofit's exception shapes are easy to get subtly wrong by
 * hand, so this proves the real integration between Retrofit/OkHttp/Moshi and our error mapping.
 */
class SafeApiCallExecutorTest {

    @JsonClass(generateAdapter = true)
    data class TestBody(val value: String)

    private lateinit var server: MockWebServer
    private lateinit var api: TestApi
    private lateinit var connectivityObserver: FakeConnectivityObserver
    private lateinit var executor: SafeApiCallExecutor

    interface TestApi {
        @GET("/test")
        suspend fun call(): com.aorrico.mymbchallenge.data.remote.dto.CmcEnvelope<TestBody>
    }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        val moshi = Moshi.Builder().build()
        // A short read timeout keeps the timeout test fast instead of waiting out OkHttp's
        // 10s default; it doesn't affect the other tests since none of them delay a response.
        val okHttpClient = OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.MILLISECONDS)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        api = retrofit.create(TestApi::class.java)
        connectivityObserver = FakeConnectivityObserver(connected = true)
        executor = SafeApiCallExecutor(moshi, connectivityObserver)
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `successful envelope with error_code zero returns Success`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"status":{"error_code":0,"error_message":null},"data":{"value":"ok"}}""",
            ),
        )

        val result = executor.execute { api.call() }

        assertThat(result).isEqualTo(AppResult.Success(TestBody("ok")))
    }

    @Test
    fun `HTTP 200 with non-zero error_code maps to Api error`() = runBlocking {
        server.enqueue(
            MockResponse().setBody(
                """{"status":{"error_code":1006,"error_message":"Plan not supported"},"data":null}""",
            ),
        )

        val result = executor.execute { api.call() } as AppResult.Error

        assertThat(result.error).isEqualTo(AppError.Api(1006, "Plan not supported"))
    }

    @Test
    fun `HTTP 401 with a CMC error body maps to Api error parsed from the response`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"status":{"error_code":1001,"error_message":"This API Key is invalid."}}"""),
        )

        val result = executor.execute { api.call() } as AppResult.Error

        assertThat(result.error).isEqualTo(AppError.Api(1001, "This API Key is invalid."))
    }

    @Test
    fun `HTTP error with an unparseable body falls back to Http error with the status code`() = runBlocking {
        server.enqueue(MockResponse().setResponseCode(503).setBody("upstream is down"))

        val result = executor.execute { api.call() } as AppResult.Error

        assertThat(result.error).isInstanceOf(AppError.Http::class.java)
        assertThat((result.error as AppError.Http).code).isEqualTo(503)
    }

    @Test
    fun `malformed JSON body maps to Parsing error`() = runBlocking {
        server.enqueue(MockResponse().setBody("""{"status": "not-an-object"}"""))

        val result = executor.execute { api.call() } as AppResult.Error

        assertThat(result.error).isInstanceOf(AppError.Parsing::class.java)
    }

    @Test
    fun `a request that fails mid-flight maps to NoConnectivity`() = runBlocking {
        server.shutdown()

        val result = executor.execute { api.call() } as AppResult.Error

        assertThat(result.error).isInstanceOf(AppError.NoConnectivity::class.java)
    }

    @Test
    fun `a response that never arrives in time maps to Timeout, not NoConnectivity`() = runBlocking {
        server.enqueue(
            MockResponse()
                .setHeadersDelay(1, TimeUnit.SECONDS)
                .setBody("""{"status":{"error_code":0,"error_message":null},"data":{"value":"ok"}}"""),
        )

        val result = executor.execute { api.call() } as AppResult.Error

        assertThat(result.error).isInstanceOf(AppError.Timeout::class.java)
    }

    @Test
    fun `known-offline short-circuits to NoConnectivity without making a request`() = runBlocking {
        connectivityObserver.setConnected(false)

        val result = executor.execute { api.call() } as AppResult.Error

        assertThat(result.error).isInstanceOf(AppError.NoConnectivity::class.java)
        assertThat(server.requestCount).isEqualTo(0)
    }
}
