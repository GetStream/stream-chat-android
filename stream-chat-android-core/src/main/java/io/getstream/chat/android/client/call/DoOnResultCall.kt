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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class DoOnResultCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val consumer: suspend (Result<T>) -> Unit,
) : Call<T> {

    private var job: Job? = null

    override fun execute(): Result<T> = runBlocking {
        originalCall.execute().also { consumer(it) }
    }

    override fun enqueue(callback: Call.Callback<T>) {
        originalCall.enqueue { result ->
            job = scope.launch { consumer(result) }
            callback.onResult(result)
        }
    }

    override fun cancel() {
        job?.cancel()
    }
}
