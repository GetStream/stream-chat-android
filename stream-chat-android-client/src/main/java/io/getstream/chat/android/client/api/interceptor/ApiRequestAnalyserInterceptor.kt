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

package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import io.getstream.chat.android.core.internal.StreamHandsOff
import io.getstream.chat.android.models.Constants
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.OutputStream

/**
 * Retrofit's [Interceptor] to use [ApiRequestsAnalyser] so all requests are recorded then the user can
 * analyse then latter.
 */
internal class ApiRequestAnalyserInterceptor(private val requestsAnalyser: ApiRequestsAnalyser) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val buffer = Buffer()
        val stringOutputStream = StringOutputStream()

        chain.request().body?.writeTo(buffer)
        writeRequestBody(stringOutputStream, buffer)

        requestsAnalyser.registerRequest(request.url.toString(), mapOf("body" to stringOutputStream.toString()))

        return chain.proceed(request)
    }

    @StreamHandsOff(
        reason = "Request body shouldn't be written entirely as it might produce OutOfMemory " +
            "exceptions when sending big files." +
            " The output will be limited to ${Constants.MAX_REQUEST_BODY_LENGTH} bytes.",
    )
    private fun writeRequestBody(stringOutputStream: StringOutputStream, buffer: Buffer) {
        buffer.writeTo(stringOutputStream, minOf(buffer.size, Constants.MAX_REQUEST_BODY_LENGTH))
    }
}

private class StringOutputStream : OutputStream() {

    private val stringBuilder = StringBuilder()

    override fun write(b: Int) {
        stringBuilder.append(b.toChar())
    }

    override fun toString(): String = stringBuilder.toString().ifEmpty { "no_body" }
}
