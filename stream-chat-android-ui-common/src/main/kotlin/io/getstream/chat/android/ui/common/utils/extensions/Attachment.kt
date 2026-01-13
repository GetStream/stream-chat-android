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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.utils.attachment.isAudio
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isFile
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.ui.common.helper.internal.StorageHelper
import io.getstream.chat.android.ui.common.utils.StringUtils

/**
 * Generates a displayable name for the attachment. Builds the name using the following priority:
 * 1. Title of the attachment
 * 2. Name of the attachment
 * 3. Name of the uploaded file
 *
 * Additionally, it removes any time prefix from the name.
 */
public fun Attachment.getDisplayableName(): String? {
    return StringUtils.removeTimePrefix(title ?: name ?: upload?.name, StorageHelper.TIME_FORMAT)
}

/**
 * Retrieves the image preview URL for the attachment (if available).
 *
 * It first checks for the thumbnail URL, and if not present, falls back to the main image URL.
 */
public val Attachment.imagePreviewUrl: String?
    get() = thumbUrl ?: imageUrl

/**
 * Checks if the attachment is of any file type (file, video, audio or audio recording).
 */
public fun Attachment.isAnyFileType(): Boolean {
    return uploadId != null ||
        upload != null ||
        isFile() ||
        isVideo() ||
        isAudio() ||
        isAudioRecording()
}

/**
 * Checks if the attachment is currently being uploaded to the server.
 */
public fun Attachment.isUploading(): Boolean {
    return (uploadState is Attachment.UploadState.InProgress || uploadState is Attachment.UploadState.Idle) &&
        upload != null &&
        uploadId != null
}

/**
 * Checks if the attachment upload has failed.
 */
public fun Attachment.isFailed(): Boolean {
    return (uploadState is Attachment.UploadState.Failed) &&
        upload != null &&
        uploadId != null
}

/**
 * Checks if the attachment is a link attachment.
 */
public fun Attachment.hasLink(): Boolean = titleLink != null || ogUrl != null
