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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.mapping.DtoMapping
import io.getstream.chat.android.client.api2.mapping.EventMapping
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser2.MoshiChatParser
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import kotlinx.coroutines.CoroutineScope
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class RetroSuccess<T : Any>(val result: T) : Call<T> {

    fun toRetrofitCall(): RetrofitCall<T> {
        return RetrofitCall(
            call = this,
            parser = MoshiChatParser(
                EventMapping(
                    DomainMapping(
                        { "" },
                        NoOpChannelTransformer,
                        NoOpMessageTransformer,
                    )
                ),
                DtoMapping(
                    NoOpMessageTransformer
                ),
            ),
            CoroutineScope(DispatcherProvider.IO),
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
        return Response.success(result)
    }

    override fun request(): Request {
        return null!!
    }

    override fun timeout(): Timeout {
        return Timeout()
    }
}
