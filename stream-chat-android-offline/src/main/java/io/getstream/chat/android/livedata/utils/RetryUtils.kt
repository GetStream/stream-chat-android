package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.logger.TaggedLogger
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.extensions.isPermanent
import kotlinx.coroutines.delay

internal object RetryUtils {

    internal suspend fun <T : Any> runAndRetry(
        client: ChatClient,
        retryPolicy: RetryPolicy,
        logger: TaggedLogger,
        runnable: () -> Call<T>,
    ): Result<T> {
        var attempt = 1
        var result: Result<T>

        while (true) {
            result = runnable().execute()
            if (result.isSuccess || result.error().isPermanent()) {
                break
            } else {
                // retry logic
                val shouldRetry = retryPolicy.shouldRetry(client, attempt, result.error())
                val timeout = retryPolicy.retryTimeout(client, attempt, result.error())

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
