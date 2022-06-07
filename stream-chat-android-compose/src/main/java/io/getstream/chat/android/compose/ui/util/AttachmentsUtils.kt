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

package io.getstream.chat.android.compose.ui.util

import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment

/**
 * Returns a string representation for the given attachment.
 */
public val Attachment.previewText: String
    get() = title ?: name ?: ""

/**
 * Attachment types that represent media content.
 */
private val MEDIA_ATTACHMENT_TYPES: Collection<String> = listOf(ModelType.attach_image, ModelType.attach_giphy)

/**
 * @return If the [Attachment] is media content or not.
 */
internal fun Attachment.isMedia(): Boolean = type in MEDIA_ATTACHMENT_TYPES

/**
 * @return If the [Attachment] is a link attachment or not.
 */
internal fun Attachment.hasLink(): Boolean = titleLink != null || ogUrl != null

/**
 * @return If the [Attachment] is a file or not.
 */
internal fun Attachment.isFile(): Boolean {
    return uploadId != null ||
        upload != null ||
        type == ModelType.attach_file ||
        type == ModelType.attach_video ||
        type == ModelType.attach_audio
}

/**
 * @return If the attachment is currently being uploaded to the server.
 */
internal fun Attachment.isUploading(): Boolean {
    return (uploadState is Attachment.UploadState.InProgress || uploadState is Attachment.UploadState.Idle) &&
        upload != null &&
        uploadId != null
}
