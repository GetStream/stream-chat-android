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
 
package io.getstream.chat.android.test

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

public class TestCall<T : Any>(public val result: Result<T>) : Call<T> {
    public var cancelled: Boolean = false

    override fun cancel() {
        cancelled = true
    }

    override fun enqueue(callback: Call.Callback<T>) {
        callback.onResult(result)
    }

    override fun execute(): Result<T> {
        return result
    }
}

public fun <T : Any> callFrom(valueProvider: () -> T): Call<T> = TestCall(Result(valueProvider()))

public fun <T : Any> T.asCall(): Call<T> = TestCall(Result(this))

public inline fun <reified T : Any> failedCall(message: String = "", cause: Throwable? = null): Call<T> {
    return object : Call<T> {
        public var cancelled: Boolean = false

        override fun execute(): Result<T> = Result(ChatError(message, cause))

        override fun enqueue(callback: Call.Callback<T>) {
            callback.onResult(Result(ChatError(message, cause)))
        }

        override fun cancel() {
            cancelled = true
        }
    }
}
