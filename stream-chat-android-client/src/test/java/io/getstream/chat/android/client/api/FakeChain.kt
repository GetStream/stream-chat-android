/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.Mother
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.TimeUnit

internal class FakeChain(
    vararg val response: FakeResponse,
    val request: Request = Mother.randomGetRequest(url = "https://hello.url"),
) : Interceptor.Chain {

    var chainIndex = 0

    fun processChain() {
        chainIndex++
    }

    override fun call(): Call {
        return null!!
    }

    override fun connectTimeoutMillis(): Int {
        return 0
    }

    override fun connection(): Connection? {
        return null
    }

    override fun proceed(request: Request): Response {
        val response = response[chainIndex]

        return Response.Builder()
            .code(response.statusCode)
            .request(request)
            .protocol(Protocol.HTTP_2)
            .body(response.body)
            .message("ok")
            .build()
    }

    override fun readTimeoutMillis(): Int {
        return 0
    }

    override fun request(): Request {
        return request
    }

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }

    override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }

    override fun writeTimeoutMillis(): Int {
        return 0
    }
}
