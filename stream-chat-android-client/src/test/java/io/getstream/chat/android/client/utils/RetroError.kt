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
import io.getstream.chat.android.client.parser2.ParserFactory
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class RetroError<T : Any>(
    private val statusCode: Int,
    private val streamCode: Int? = FAKE_ERROR_CODE,
    private val message: String? = "Test Error",
    private val exceptionFields: Map<String, String> = mapOf(Pair("Test", "Error")),
    private val duration: Float? = 0.2f,
    private val moreInfo: String = "https://getstream.io/chat/docs/api_errors_response",
    private val mediaType: MediaType = "text/plain".toMediaType(),
) : Call<T> {

    fun toRetrofitCall(): RetrofitCall<T> {
        return RetrofitCall(
            call = this,
            parser = ParserFactory.createMoshiChatParser(),
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
        return Response.error(
            statusCode,
            toStreamApiErrorResponseBody(
                statusCode = statusCode,
                streamCode = streamCode,
                message = message,
                exceptionFields = exceptionFields,
                duration = duration,
                moreInfo = moreInfo,
                mediaType = mediaType,
            ),
        )
    }

    override fun request(): Request {
        return null!!
    }

    override fun timeout(): Timeout {
        return Timeout()
    }

    /**
     * Creates a facsimile of the error response sent by the backend.
     */
    private fun toStreamApiErrorResponseBody(
        streamCode: Int? = null,
        message: String? = null,
        exceptionFields: Map<String, String> = mapOf(),
        statusCode: Int? = null,
        duration: Float? = null,
        moreInfo: String? = null,
        mediaType: MediaType,
    ): ResponseBody {
        val errorJsonMembers = mutableListOf<String>()

        if (streamCode != null) {
            errorJsonMembers.add(""""code":"$streamCode"""")
        }

        if (message != null) {
            errorJsonMembers.add(""""message":"Sync failed with error: $message"""")
        }

        if (exceptionFields.isNotEmpty()) {
            val exceptionFieldMembers = mutableListOf<String>()

            exceptionFields.forEach { (key, value) ->
                exceptionFieldMembers.add(""""$key":"$value"""")
            }

            errorJsonMembers.add(
                """"exception_fields":""" +
                    exceptionFieldMembers.joinToString(separator = ",", prefix = "{", postfix = "}"),
            )
        }

        if (statusCode != null) {
            errorJsonMembers.add(""""StatusCode":"$statusCode"""")
        }

        if (duration != null) {
            errorJsonMembers.add(""""duration":"${String.format(format = "%.2f", duration)}ms"""")
        }

        if (moreInfo != null) {
            errorJsonMembers.add(""""more_info":"$moreInfo"""")
        }

        return errorJsonMembers.joinToString(",", prefix = "{", postfix = "}").toResponseBody(mediaType)
    }

    companion object {
        internal const val FAKE_ERROR_CODE = -1
    }
}
