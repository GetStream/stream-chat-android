package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.retry.RetryPolicy

internal class NoRetryPolicy : RetryPolicy {
    override fun shouldRetry(attempt: Int, error: ChatError): Boolean = false

    override fun retryTimeout(attempt: Int, error: ChatError): Int = 0
}
