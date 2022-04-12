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

import android.os.Handler
import android.os.Looper
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser.ChatParser
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.concurrent.Executor

internal class RetrofitCallAdapterFactory private constructor(
    private val chatParser: ChatParser,
    private val callbackExecutor: Executor
) : CallAdapter.Factory() {

    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != RetrofitCall::class.java) {
            return null
        }
        if (returnType !is ParameterizedType) {
            throw IllegalArgumentException("Call return type must be parameterized as Call<Foo>")
        }
        val responseType: Type = getParameterUpperBound(0, returnType)
        return RetrofitCallAdapter<Any>(responseType, chatParser, callbackExecutor)
    }

    companion object {
        private val mainThreadExecutor: Executor = object : Executor {
            val handler: Handler by lazy { Handler(Looper.getMainLooper()) }
            override fun execute(command: Runnable?) {
                command?.let(handler::post)
            }
        }

        fun create(
            chatParser: ChatParser,
            callbackExecutor: Executor? = null,
        ): RetrofitCallAdapterFactory = RetrofitCallAdapterFactory(chatParser, callbackExecutor ?: mainThreadExecutor)
    }
}

internal class RetrofitCallAdapter<T : Any>(
    private val responseType: Type,
    private val parser: ChatParser,
    private val callbackExecutor: Executor
) : CallAdapter<T, Call<T>> {

    override fun adapt(call: retrofit2.Call<T>): Call<T> {
        return RetrofitCall(call, parser, callbackExecutor)
    }

    override fun responseType(): Type = responseType
}
