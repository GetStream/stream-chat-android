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

package io.getstream.chat.android.models

/**
 * Represents types of attachments.
 */
public object AttachmentType {
    /** Image attachment type. */
    public const val IMAGE: String = "image"

    /** Imgur attachment type. */
    public const val IMGUR: String = "imgur"

    /** Giphy attachment type. */
    public const val GIPHY: String = "giphy"

    /** Video attachment type. */
    public const val VIDEO: String = "video"

    /** Audio attachment type. */
    public const val AUDIO: String = "audio"

    /** Product attachment type. */
    public const val PRODUCT: String = "product"

    /** File attachment type. */
    public const val FILE: String = "file"

    /**
     * @deprecated The attachment of type 'LINK' is not officially supported, and the Attachment.type can never have a
     * value == 'link'.
     */
    @Deprecated(
        message = "The attachment of type 'LINK' is not officially supported, and the Attachment.type can never have " +
            "a value == 'link'",
        level = DeprecationLevel.WARNING,
    )
    public const val LINK: String = "link"

    /** Audio recording (voice message) attachment type. */
    public const val AUDIO_RECORDING: String = "voiceRecording"

    /** Unknown attachment type. */
    public const val UNKNOWN: String = "unknown"
}
