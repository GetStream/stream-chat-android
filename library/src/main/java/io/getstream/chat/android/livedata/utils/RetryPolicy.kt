package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError

interface RetryPolicy {
    /**
     * Should Retry evaluates if we should retry the failure
     *
     * @param client
     * @param attempt
     * @param errMsg
     * @param errCode
     * @return
     */
    fun shouldRetry(
        client: ChatClient,
        attempt: Int,
        error: ChatError
    ): Boolean

    /**
     * In the case that we want to retry a failed request the retryTimeout method is called
     * to determine the timeout
     *
     * @param client
     * @param attempt
     * @param errMsg
     * @param errCode
     * @return
     */
    fun retryTimeout(
        client: ChatClient,
        attempt: Int,
        error: ChatError
    ): Int
}
