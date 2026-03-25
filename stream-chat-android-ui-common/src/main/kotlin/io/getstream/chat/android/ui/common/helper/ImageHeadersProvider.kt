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
 * Provides HTTP headers for image loading requests.
 *
 * @deprecated Use [io.getstream.chat.android.client.cdn.CDN] instead. Configure a custom CDN via
 * [io.getstream.chat.android.client.ChatClient.Builder.cdn] to provide headers and transform URLs
 * for all image, file, and download requests.
 */
@Deprecated("Use CDN instead. Configure via ChatClient.Builder.cdn().")
public interface ImageHeadersProvider {

    /**
     * Returns a map of headers to be used for the image loading request.
     *
     * @param url The URL of the image to load.
     * @return A map of headers to be used for the image loading request.
     */
    public fun getImageRequestHeaders(url: String): Map<String, String>
}

/**
 * Default implementation of [ImageHeadersProvider] that doesn't provide any headers.
 *
 * @deprecated Use [io.getstream.chat.android.client.cdn.CDN] instead. Configure a custom CDN via
 * [io.getstream.chat.android.client.ChatClient.Builder.cdn] to provide headers and transform URLs
 * for all image, file, and download requests.
 */
@Deprecated("Use CDN instead. Configure via ChatClient.Builder.cdn().")
public object DefaultImageHeadersProvider : ImageHeadersProvider {
    override fun getImageRequestHeaders(url: String): Map<String, String> = emptyMap()
}
