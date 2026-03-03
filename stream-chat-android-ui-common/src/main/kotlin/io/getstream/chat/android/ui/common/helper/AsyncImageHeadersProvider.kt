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

package io.getstream.chat.android.ui.common.helper

/**
 * Provides HTTP headers for image loading requests in a suspending, thread-safe manner.
 *
 * Unlike [ImageHeadersProvider], this interface is designed for async operations such as
 * reading an auth token from encrypted storage or fetching one from a remote endpoint.
 * Implementations are invoked on a background thread inside Coil's image loading pipeline,
 * so blocking calls are safe.
 *
 * Prefer this over [ImageHeadersProvider] when integrating with [ChatTheme].
 *
 * @see ImageHeadersProvider
 */
public interface AsyncImageHeadersProvider {

    /**
     * Returns a map of headers to be used for the image loading request.
     *
     * This function is called on a background thread as part of Coil's interceptor chain,
     * so blocking operations are safe.
     *
     * @param url The URL of the image to load.
     * @return A map of headers to be used for the image loading request.
     */
    public suspend fun getImageRequestHeaders(url: String): Map<String, String>
}
