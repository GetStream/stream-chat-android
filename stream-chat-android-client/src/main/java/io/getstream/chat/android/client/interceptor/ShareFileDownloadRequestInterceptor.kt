/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.interceptor

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import okhttp3.Request

/**
 * Intercepts and customizes the HTTP request used to download an attachment that will be shared.
 *
 * Why this exists
 * - When a user shares a video or other non-image attachment, the SDK downloads the file
 *   to a temporary, shareable location via OkHttp before launching the system share sheet.
 *   Many backends require request customization (e.g., Authorization headers, cookies, custom
 *   User-Agent, Referer, or extra query parameters) to successfully fetch protected files.
 * - This interface lets you inject those customizations at the last moment without replacing
 *   the SDK's download logic.
 *
 * When it is used
 * - Only during the "Share" flow when the SDK must download a remote file to a
 *   temporary cache before launching the Android share sheet.
 * - Triggered from media sharing UIs such as `MediaGalleryPreviewActivity` (Compose)
 *   and `AttachmentGalleryActivity` (UI Components) when sharing videos or other
 *   non-image attachments. Image shares use a bitmap path and typically do not hit
 *   the network.
 *
 * Notes
 * - Intended for additive changes such as headers or query parameters. Changing HTTP method or
 *   target URL is strongly discouraged as it may lead to unexpected behavior.
 * - This interceptor applies only to the share flow (temporary file download via OkHttp). It is
 *   separate from the download interceptor used with Android's [android.app.DownloadManager]
 *   when saving media to device storage.
 *
 * Usage
 * ```kotlin
 * val interceptor = ShareFileDownloadRequestInterceptor { builder ->
 *     builder
 *         .header("Authorization", "Bearer ${tokenProvider()}")
 *         .header("User-Agent", "MyApp/1.0")
 * }
 * ```
 *
 * Common use cases
 * - Add an Authorization header for private CDN/file endpoints.
 * - Attach cookies required by your gateway.
 * - Set a custom User-Agent or Referer required by your infrastructure.
 */
public interface ShareFileDownloadRequestInterceptor {

    /**
     * Intercepts and modifies the download request before it is executed.
     *
     * @param request The base [Request.Builder] to intercept. By default, it's configured with:
     *  - HTTP method: GET
     *  - URL: the file URL
     *
     * Do not call [Request.Builder.build]; just return the (possibly modified) builder.
     *
     * @return The modified [Request.Builder] to be used for the download.
     */
    public fun intercept(request: Request.Builder): Request.Builder
}

/**
 * Default implementation of [ShareFileDownloadRequestInterceptor] that leaves the request unchanged.
 */
@InternalStreamChatApi
public object DefaultShareFileDownloadRequestInterceptor : ShareFileDownloadRequestInterceptor {
    override fun intercept(request: Request.Builder): Request.Builder = request
}
