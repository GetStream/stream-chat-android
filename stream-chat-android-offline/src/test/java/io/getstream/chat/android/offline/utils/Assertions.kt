package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.client.utils.Result
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.fail

internal infix fun <T, R : Result<T>> R.`should be equal to result`(expected: R) = apply {
    if (isError && expected.isError) {
        val thisError = this.error()
        val expectedError = expected.error()
        if (thisError.message != expectedError.message || thisError.cause != expectedError.cause) {
            fail("Expected $expected, actual $this")
        }
    } else {
        this.shouldBeEqualTo(expected)
    }
}
