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

package io.getstream.chat.android.client.cdn.internal

import io.getstream.chat.android.client.cdn.CDN
import io.getstream.chat.android.client.cdn.CDNRequest
import io.getstream.log.taggedLogger
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * OkHttp interceptor applying transformations to CDN requests.
 */
internal class CDNOkHttpInterceptor(private val cdn: CDN) : Interceptor {

    private val logger by taggedLogger("Chat:CDNOkHttpInterceptor")

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalUrl = chain.request().url.toString()
        val (url, headers) = try {
            runBlocking {
                cdn.fileRequest(originalUrl)
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.e(e) {
                "[intercept] CDN.fileRequest() failed for url: $originalUrl. " +
                    "Falling back to original request."
            }
            CDNRequest(originalUrl)
        }
        val request = chain.request().newBuilder()
            .url(url)
            .apply {
                headers?.forEach {
                    header(it.key, it.value)
                }
            }
            .build()
        return chain.proceed(request)
    }
}
