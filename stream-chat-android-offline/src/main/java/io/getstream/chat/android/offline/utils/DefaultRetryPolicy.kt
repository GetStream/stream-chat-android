package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError

internal class DefaultRetryPolicy : RetryPolicy {
    override fun shouldRetry(
        client: ChatClient,
        attempt: Int,
        error: ChatError
    ): Boolean {
        return attempt < 3
    }

    /**
     * Returns the timeout in milliseconds
     */
    override fun retryTimeout(
        client: ChatClient,
        attempt: Int,
        error: ChatError
    ): Int {
        return attempt * 1000
    }
}
