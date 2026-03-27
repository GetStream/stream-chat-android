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

package io.getstream.chat.android.ui.common.images.internal

import coil3.intercept.Interceptor
import coil3.network.httpHeaders
import coil3.request.ImageResult
import io.getstream.chat.android.client.cdn.CDN
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.log.taggedLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A Coil [Interceptor] that intercepts image requests and applies CDN transformations.
 *
 * The interceptor calls [CDN.imageRequest] to obtain a potentially modified URL and additional
 * headers. CDN headers take precedence over any headers already present on the request
 * (e.g. from [io.getstream.chat.android.ui.common.helper.ImageHeadersProvider]), overriding
 * them for the same key.
 *
 * Only HTTP/HTTPS URLs are intercepted; local resources, content URIs, etc. pass through unchanged.
 */
@InternalStreamChatApi
public class CDNImageInterceptor(private val cdn: CDN) : Interceptor {

    private val logger by taggedLogger("Chat:CDNImageInterceptor")

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val request = chain.request
        val url = request.data.toString()

        // Only intercept http/https URLs
        if (!url.startsWith("http://", ignoreCase = true) && !url.startsWith("https://", ignoreCase = true)) {
            return chain.proceed()
        }

        val cdnRequest = try {
            withContext(Dispatchers.IO) {
                cdn.imageRequest(url)
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            logger.e(e) { "[intercept] CDN.imageRequest() failed for url: $url. Falling back to original request." }
            return chain.proceed()
        }

        // Merge headers: existing request headers as base, CDN headers override for same keys
        val existingHeaders = request.httpHeaders
        val mergedHeaders = buildMap {
            existingHeaders.asMap().forEach { (name, values) ->
                values.lastOrNull()?.let { put(name, it) }
            }
            cdnRequest.headers?.let { putAll(it) }
        }.toNetworkHeaders()

        val newRequest = request.newBuilder()
            .data(cdnRequest.url)
            .httpHeaders(mergedHeaders)
            .build()

        return chain.withRequest(newRequest).proceed()
    }
}
