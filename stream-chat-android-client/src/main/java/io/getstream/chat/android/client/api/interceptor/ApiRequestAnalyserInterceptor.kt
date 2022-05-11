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

package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.OutputStream

internal class ApiRequestAnalyserInterceptor(private val requestsAnalyser: ApiRequestsAnalyser) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val buffer = Buffer()
        val stringOutputStream = StringOutputStream()

        chain.request().body?.writeTo(buffer)
        buffer.writeTo(stringOutputStream)

        requestsAnalyser.registerRequest(request.url.toString(), mapOf("body" to stringOutputStream.toString()))

        return chain.proceed(request)
    }
}

private class StringOutputStream : OutputStream() {

    private val stringBuilder = StringBuilder()

    override fun write(b: Int) {
        stringBuilder.append(b.toChar())
    }

    override fun toString(): String = stringBuilder.toString().ifEmpty { "no_body" }
}
