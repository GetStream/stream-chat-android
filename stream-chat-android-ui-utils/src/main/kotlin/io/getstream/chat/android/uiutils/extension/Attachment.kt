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

package io.getstream.chat.android.uiutils.extension

import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.utils.attachment.isAudio
import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isFile
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.models.Attachment

/**
 * @return If the [Attachment] is a file or not.
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
 * @return If the attachment is currently being uploaded to the server.
 */
public fun Attachment.isUploading(): Boolean {
    return (uploadState is Attachment.UploadState.InProgress || uploadState is Attachment.UploadState.Idle) &&
        upload != null &&
        uploadId != null
}

/**
 * @return If the attachment has been failed when uploading to the server.
 */
public fun Attachment.isFailed(): Boolean {
    return (uploadState is Attachment.UploadState.Failed) &&
        upload != null &&
        uploadId != null
}

/**
 * @return If the [Attachment] is a link attachment or not.
 */
public fun Attachment.hasLink(): Boolean = titleLink != null || ogUrl != null
