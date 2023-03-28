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

import io.getstream.chat.android.client.call.Call.Companion.callCanceledError
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.result.Result
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

internal class MapCall<T : Any, K : Any>(
    private val call: Call<T>,
    private val mapper: (T) -> K,
) : Call<K> {

    private val canceled = AtomicBoolean(false)

    override fun cancel() {
        canceled.set(true)
        call.cancel()
    }

    override fun execute(): Result<K> = runBlocking { await() }

    override fun enqueue(callback: Call.Callback<K>) {
        call.enqueue {
            it.takeUnless { canceled.get() }
                ?.map(mapper)
                ?.let(callback::onResult)
        }
    }

    override suspend fun await(): Result<K> = withContext(DispatcherProvider.IO) {
        call.await()
            .takeUnless { canceled.get() }
            ?.map(mapper)
            .takeUnless { canceled.get() }
            ?: callCanceledError()
    }
}
