package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError

class DefaultRetryPolicy: RetryPolicy {
    override fun shouldRetry(
        client: ChatClient,
        attempt: Int,
        error: ChatError
    ): Boolean {
        return attempt < 5
    }

    override fun retryTimeout(
        client: ChatClient,
        attempt: Int,
        error: ChatError
    ): Int? {
        return attempt * 1000
    }
}