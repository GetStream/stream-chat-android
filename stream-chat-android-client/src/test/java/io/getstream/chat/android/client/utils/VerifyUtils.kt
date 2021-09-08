package io.getstream.chat.android.client.utils

import com.google.common.truth.Truth.assertThat
import io.getstream.chat.android.client.errors.ChatNetworkError

internal fun <T : Any> verifyError(result: Result<T>, statusCode: Int) {
    assertThat(result.isSuccess).isFalse()
    assertThat(result.error()).isInstanceOf(ChatNetworkError::class.java)

    val error = result.error() as ChatNetworkError
    assertThat(error.statusCode).isEqualTo(statusCode)
}

internal fun <T : Any> verifySuccess(result: Result<T>, equalsTo: T) {
    assertThat(result.isSuccess).isTrue()
    assertThat(result.data()).isEqualTo(equalsTo)
}
