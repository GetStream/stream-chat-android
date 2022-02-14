package io.getstream.chat.android.client.utils.retry

import io.getstream.chat.android.client.errors.ChatError

/**
 * Default retry policy that won't retry any calls.
 */
internal class DefaultNoRetryPolicy : RetryPolicy {
    /**
     * Shouldn't retry any calls.
     *
     * @return false
     */
    override fun shouldRetry(attempt: Int, error: ChatError): Boolean = false

    /**
     * Should never be called as the policy doesn't allow retrying.
     */
    override fun retryTimeout(attempt: Int, error: ChatError): Int = 0
}
