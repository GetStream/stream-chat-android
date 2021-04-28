package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.offline.utils.RetryPolicy as OfflineRetryPolicy

public interface RetryPolicy : OfflineRetryPolicy

internal fun OfflineRetryPolicy.toLiveDataRetryPolicy(): RetryPolicy = object : RetryPolicy {
    override fun shouldRetry(client: ChatClient, attempt: Int, error: ChatError): Boolean {
        return this@toLiveDataRetryPolicy.shouldRetry(client, attempt, error)
    }

    override fun retryTimeout(client: ChatClient, attempt: Int, error: ChatError): Int {
        return this@toLiveDataRetryPolicy.retryTimeout(client, attempt, error)
    }
}
