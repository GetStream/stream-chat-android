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

import android.net.Uri
import io.getstream.chat.android.models.Attachment

/**
 * Generates a download URI for the given attachment.
 *
 * @deprecated Use [io.getstream.chat.android.client.cdn.CDN] instead. Configure a custom CDN via
 * [io.getstream.chat.android.client.ChatClient.Builder.cdn] to transform URLs for all image, file,
 * and download requests.
 */
@Deprecated("Use CDN instead. Configure via ChatClient.Builder.cdn().")
public fun interface DownloadAttachmentUriGenerator {

    /**
     * Generates a download URI for the given attachment.
     *
     * @param attachment The attachment to generate the download URI for.
     *
     * @return The download URI for the given attachment.
     */
    public fun generateDownloadUri(attachment: Attachment): Uri
}

/**
 * Default implementation of [DownloadAttachmentUriGenerator] that generates a download URI based on the asset URL
 * or image URL of the attachment.
 *
 * @deprecated Use [io.getstream.chat.android.client.cdn.CDN] instead. Configure a custom CDN via
 * [io.getstream.chat.android.client.ChatClient.Builder.cdn] to transform URLs for all image, file,
 * and download requests.
 */
@Deprecated("Use CDN instead. Configure via ChatClient.Builder.cdn().")
public object DefaultDownloadAttachmentUriGenerator : DownloadAttachmentUriGenerator {
    override fun generateDownloadUri(attachment: Attachment): Uri =
        Uri.parse(attachment.assetUrl ?: attachment.imageUrl)
}
