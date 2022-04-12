/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.retry.CallRetryService
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * A wrapper around [Call] that allows retrying the original call based on [io.getstream.chat.android.client.utils.retry.RetryPolicy].
 *
 * @param originalCall The original call.
 * @param scope Coroutine scope where the call should be run.
 * @param callRetryService A service responsible for retrying calls based on [io.getstream.chat.android.client.utils.retry.RetryPolicy].
 */
internal class RetryCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val callRetryService: CallRetryService,
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> = runBlocking {
        callRetryService.runAndRetry {
            originalCall
        }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        scope.launch {
            val result = callRetryService.runAndRetry {
                originalCall
            }
            withContext(DispatcherProvider.Main) {
                callback.onResult(result)
            }
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}
