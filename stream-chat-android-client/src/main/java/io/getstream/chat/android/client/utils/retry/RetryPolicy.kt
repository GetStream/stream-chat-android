package io.getstream.chat.android.client.utils.retry

import io.getstream.chat.android.client.errors.ChatError

/**
 * The retry policy is being used to determine if and when the call should be retried if a temporary error occurred.
 */
public interface RetryPolicy {
    /**
     * Determines whether the call should be retried.
     *
     * @param attempt Current retry attempt.
     * @param error The error returned by the previous attempt.
     *
     * @return true if the call should be retried, false otherwise.
     */
    public fun shouldRetry(attempt: Int, error: ChatError): Boolean

    /**
     * Provides a timeout used to delay the next call.
     *
     * @param attempt Current retry attempt.
     * @param error The error returned by the previous attempt.
     *
     * @return The timeout in milliseconds before making a retry.
     */
    public fun retryTimeout(attempt: Int, error: ChatError): Int
}
