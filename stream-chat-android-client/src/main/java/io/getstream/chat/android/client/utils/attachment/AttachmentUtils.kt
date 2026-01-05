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

package io.getstream.chat.android.client.utils.attachment

import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType

/**
 * @return If the attachment type is image.
 */
public fun Attachment.isImage(): Boolean = type == AttachmentType.IMAGE

/**
 * @return If the attachment type is video.
 */
public fun Attachment.isVideo(): Boolean = type == AttachmentType.VIDEO

/**
 * @return If the attachment type is video.
 */
public fun Attachment.isAudio(): Boolean = type == AttachmentType.AUDIO

/**
 * @return If the attachment type is file.
 */
public fun Attachment.isFile(): Boolean = type == AttachmentType.FILE

/**
 * @return If the attachment type is giphy.
 */
public fun Attachment.isGiphy(): Boolean = type == AttachmentType.GIPHY

/**
 * @return If the attachment type is imgur.
 */
public fun Attachment.isImgur(): Boolean = type == AttachmentType.IMGUR

/**
 * @return If the attachment type is link.
 */
@Deprecated(
    message = "The attachment of type 'LINK' is not officially supported, and the Attachment.type can never have " +
        "a value == 'link'. This method will always return false.",
    level = DeprecationLevel.WARNING,
)
public fun Attachment.isLink(): Boolean = type == AttachmentType.LINK

/**
 * @return If the attachment type is audio recording.
 */
public fun Attachment.isAudioRecording(): Boolean = type == AttachmentType.AUDIO_RECORDING
