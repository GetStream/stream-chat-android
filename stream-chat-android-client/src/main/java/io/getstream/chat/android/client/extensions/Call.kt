package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.RetryCall
import io.getstream.chat.android.client.utils.retry.CallRetryService
import io.getstream.chat.android.client.utils.retry.RetryPolicy
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope

/**
 * Wraps the original call with [RetryCall] wrapper.
 * Allows to retry the original call based on [io.getstream.chat.android.client.utils.retry.RetryPolicy]
 *
 * @param scope Coroutine scope where the call should be run.
 * @param retryPolicy A policy used for retrying the call.
 */
@InternalStreamChatApi
// TODO: Make internal after migrating ChatDomain
public fun <T : Any> Call<T>.retry(scope: CoroutineScope, retryPolicy: RetryPolicy): Call<T> =
    RetryCall(this, scope, CallRetryService(retryPolicy))
