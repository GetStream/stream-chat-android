package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatNetworkError
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue

internal fun <T : Any> verifyError(result: Result<T>, statusCode: Int) {
    result.isSuccess.shouldBeFalse()
    result.error().shouldBeInstanceOf<ChatNetworkError>()

    val error = result.error() as ChatNetworkError
    error.statusCode shouldBeEqualTo statusCode
}

internal fun <T : Any> verifySuccess(result: Result<T>, equalsTo: T) {
    result.isSuccess.shouldBeTrue()
    result.data() shouldBeEqualTo equalsTo
}
