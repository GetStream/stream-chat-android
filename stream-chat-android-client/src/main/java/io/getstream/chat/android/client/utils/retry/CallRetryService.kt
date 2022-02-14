package io.getstream.chat.android.client.utils.retry

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.delay

/**
 * Service that allows retrying calls based on [RetryPolicy].
 *
 * @param retryPolicy The policy used for determining if the call should be retried.
 */
internal class CallRetryService(private val retryPolicy: RetryPolicy) {

    private val logger = ChatLogger.get("CallRetryService")

    /**
     * Runs the call and retries based on [RetryPolicy].
     *
     * @param runnable The call to be run.
     */
    suspend fun <T : Any> runAndRetry(runnable: () -> Call<T>): Result<T> {
        var attempt = 1
        var result: Result<T>

        while (true) {
            result = runnable().await()
            if (result.isSuccess || result.error().isPermanent()) {
                break
            } else {
                // retry logic
                val shouldRetry = retryPolicy.shouldRetry(attempt, result.error())
                val timeout = retryPolicy.retryTimeout(attempt, result.error())

                if (shouldRetry) {
                    // temporary failure, continue
                    logger.logI("API call failed (attempt $attempt), retrying in $timeout seconds. Error was ${result.error()}")
                    delay(timeout.toLong())
                    attempt += 1
                } else {
                    logger.logI("API call failed (attempt $attempt). Giving up for now, will retry when connection recovers. Error was ${result.error()}")
                    break
                }
            }
        }
        // permanent failure case return
        return result
    }
}
