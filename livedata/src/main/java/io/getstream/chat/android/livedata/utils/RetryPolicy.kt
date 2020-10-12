package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError

/**
 * When creating a channel, adding a reaction or sending any temporary error will trigger the retry policy
 * The retry policy interface exposes 2 methods
 * - shouldRetry: returns a boolean if the request should be retried
 * - retryTimeout: How many milliseconds to wait till the next attempt
 */
interface RetryPolicy {
    /**
     * Should Retry evaluates if we should retry the failure
     */
    fun shouldRetry(
        client: ChatClient,
        attempt: Int,
        error: ChatError
    ): Boolean

    /**
     * In the case that we want to retry a failed request the retryTimeout method is called
     * to determine the timeout
     */
    fun retryTimeout(
        client: ChatClient,
        attempt: Int,
        error: ChatError
    ): Int
}
