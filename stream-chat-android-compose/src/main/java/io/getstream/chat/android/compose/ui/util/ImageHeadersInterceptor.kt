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

package io.getstream.chat.android.compose.ui.util

import coil3.intercept.Interceptor
import coil3.network.httpHeaders
import coil3.request.ImageResult
import io.getstream.chat.android.ui.common.helper.AsyncImageHeadersProvider
import io.getstream.chat.android.ui.common.images.internal.toNetworkHeaders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A Coil [Interceptor] that injects HTTP headers provided by [AsyncImageHeadersProvider] into
 * each image request. The provider is invoked as part of Coil's background pipeline, so
 * blocking or suspending operations (e.g. fetching an auth token) are safe to perform inside
 * [AsyncImageHeadersProvider.getImageRequestHeaders].
 */
internal class ImageHeadersInterceptor(private val headersProvider: AsyncImageHeadersProvider) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val url = chain.request.data.toString()
        val headers = withContext(Dispatchers.IO) {
            headersProvider.getImageRequestHeaders(url)
        }
        val newRequest = chain.request.newBuilder()
            .httpHeaders(headers.toNetworkHeaders())
            .build()
        return chain.withRequest(newRequest).proceed()
    }
}
