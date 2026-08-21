package com.aorrico.mymbchallenge.core.common.result

import com.aorrico.mymbchallenge.core.common.error.AppError
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppResultTest {

    @Test
    fun `map transforms the value on Success`() {
        val result: AppResult<Int> = AppResult.Success(2)

        assertThat(result.map { it * 10 }).isEqualTo(AppResult.Success(20))
    }

    @Test
    fun `map does not touch an Error`() {
        val error = AppError.NoConnectivity()
        val result: AppResult<Int> = AppResult.Error(error)

        assertThat(result.map { it * 10 }).isEqualTo(AppResult.Error(error))
    }

    @Test
    fun `onSuccess runs only for Success`() {
        var ran = false
        (AppResult.Success(1) as AppResult<Int>).onSuccess { ran = true }
        assertThat(ran).isTrue()

        ran = false
        (AppResult.Error(AppError.Unknown(null)) as AppResult<Int>).onSuccess { ran = true }
        assertThat(ran).isFalse()
    }

    @Test
    fun `onError runs only for Error`() {
        var ran = false
        (AppResult.Error(AppError.Unknown(null)) as AppResult<Int>).onError { ran = true }
        assertThat(ran).isTrue()

        ran = false
        (AppResult.Success(1) as AppResult<Int>).onError { ran = true }
        assertThat(ran).isFalse()
    }

    @Test
    fun `getOrNull returns the value on Success and null on Error`() {
        assertThat((AppResult.Success(5) as AppResult<Int>).getOrNull()).isEqualTo(5)
        assertThat((AppResult.Error(AppError.Unknown(null)) as AppResult<Int>).getOrNull()).isNull()
    }
}
