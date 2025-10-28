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

package io.getstream.chat.android.client.api

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class ErrorCall<T : Any>(
    private val scope: CoroutineScope,
    private val e: Error,
) : Call<T> {
    override fun cancel() {
        // Not supported
    }

    override fun execute(): Result<T> = Result.Failure(e)

    override fun enqueue(callback: Call.Callback<T>) {
        scope.launch(DispatcherProvider.Main) {
            callback.onResult(Result.Failure(e))
        }
    }

    override suspend fun await(): Result<T> = withContext(scope.coroutineContext) {
        Result.Failure(e)
    }
}
