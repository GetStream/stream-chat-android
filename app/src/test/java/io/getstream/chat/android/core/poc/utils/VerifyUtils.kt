package io.getstream.chat.android.core.poc.utils

import io.getstream.chat.android.core.poc.library.Result
import io.getstream.chat.android.core.poc.library.errors.ChatHttpError
import org.assertj.core.api.Assertions

fun <T> verifyError(result: Result<T>, statusCode: Int) {
    Assertions.assertThat(result.isSuccess).isFalse()
    Assertions.assertThat(result.error()).isInstanceOf(ChatHttpError::class.java)

    val error = result.error() as ChatHttpError
    Assertions.assertThat(error.statusCode).isEqualTo(statusCode)
}

fun <T> verifySuccess(result: Result<T>, equalsTo: T) {
    Assertions.assertThat(result.isSuccess).isTrue()
    Assertions.assertThat(result.data()).isEqualTo(equalsTo)
}
