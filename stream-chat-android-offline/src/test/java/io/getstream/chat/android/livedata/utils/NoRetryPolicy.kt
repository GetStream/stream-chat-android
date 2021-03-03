package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError

/**
 * A retry policy which prohibits retries for easier testing.
 */
internal class NoRetryPolicy : RetryPolicy {
    override fun shouldRetry(client: ChatClient, attempt: Int, error: ChatError): Boolean {
        return false
    }

    override fun retryTimeout(client: ChatClient, attempt: Int, error: ChatError): Int {
        return 1000
    }
}
