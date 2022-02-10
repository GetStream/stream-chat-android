package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.RetryCall
import io.getstream.chat.android.client.utils.retry.CallRetryService
import kotlinx.coroutines.CoroutineScope

/**
 * Wraps the original call with [RetryCall] wrapper.
 * Allows to retry the original call based on [io.getstream.chat.android.client.utils.retry.RetryPolicy]
 *
 * @param scope Coroutine scope where the call should be run.
 * @param callRetryService A service responsible for retrying calls based on [io.getstream.chat.android.client.utils.retry.RetryPolicy].
 */
internal fun <T : Any> Call<T>.retry(scope: CoroutineScope, callRetryService: CallRetryService): Call<T> =
    RetryCall(this, scope, callRetryService)
