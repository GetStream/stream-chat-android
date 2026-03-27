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

package io.getstream.chat.android.client.cdn

/**
 * Class defining a CDN (Content Delivery Network) interface.
 * Override to transform requests loading images/files from the custom CDN.
 */
public interface CDN {

    /**
     * Transforms a request for loading an image from the CDN.
     *
     * Implementations that perform blocking or network I/O must use `withContext` to switch to the
     * appropriate dispatcher (e.g. `Dispatchers.IO`).
     *
     * @param url Original CDN url for the image.
     * @return A [CDNRequest] holding the modified request URL and/or custom headers to include with the request.
     */
    public suspend fun imageRequest(url: String): CDNRequest = CDNRequest(url)

    /**
     * Transforms a request for loading a non-image file from the CDN.
     *
     * Implementations that perform blocking or network I/O must use `withContext` to switch to the
     * appropriate dispatcher (e.g. `Dispatchers.IO`).
     *
     * @param url Original CDN url for the file.
     * @return A [CDNRequest] holding the modified request URL and/or custom headers to include with the request.
     */
    public suspend fun fileRequest(url: String): CDNRequest = CDNRequest(url)
}
