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
import java.util.concurrent.atomic.AtomicBoolean

internal class MapCall<T : Any, K : Any>(
    private val call: Call<T>,
    private val mapper: (T) -> K,
) : Call<K> {

    private var canceled = AtomicBoolean(false)

    override fun cancel() {
        canceled.set(true)
    }

    override fun execute(): Result<K> {
        val resultA = call.execute()

        return if (resultA.isSuccess) {
            val data = mapper(resultA.data())
            Result(data)
        } else {
            val error = resultA.error()
            Result(error)
        }
    }

    override fun enqueue(callback: Call.Callback<K>) {
        call.enqueue callback@{
            if (canceled.get()) {
                return@callback
            }

            if (it.isSuccess) {
                val data = mapper(it.data())
                callback.onResult(Result(data))
            } else {
                val error = it.error()
                callback.onResult(Result(error))
            }
        }
    }
}
