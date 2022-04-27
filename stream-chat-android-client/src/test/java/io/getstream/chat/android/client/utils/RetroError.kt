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

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser2.MoshiChatParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class RetroError<T : Any>(val statusCode: Int) : Call<T> {

    fun toRetrofitCall(): RetrofitCall<T> {
        return RetrofitCall(
            call = this,
            parser = MoshiChatParser(),
            callbackExecutor = { runnable -> runnable.run() },
        )
    }

    override fun enqueue(callback: Callback<T>) {
        callback.onResponse(this, execute())
    }

    override fun isExecuted(): Boolean {
        return true
    }

    override fun clone(): Call<T> {
        return this
    }

    override fun isCanceled(): Boolean {
        return false
    }

    override fun cancel() {
        // no-op
    }

    override fun execute(): Response<T> {
        return Response.error(
            statusCode,
            "{Server error}".toResponseBody("text/plain".toMediaType())
        )
    }

    override fun request(): Request {
        return null!!
    }

    override fun timeout(): Timeout {
        return Timeout()
    }
}
