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

package io.getstream.chat.android.models

/**
 * Represents types of attachments.
 */
public object AttachmentType {
    public const val IMAGE: String = "image"
    public const val IMGUR: String = "imgur"
    public const val GIPHY: String = "giphy"
    public const val VIDEO: String = "video"
    public const val AUDIO: String = "audio"
    public const val PRODUCT: String = "product"
    public const val FILE: String = "file"
    public const val LINK: String = "link"
    public const val AUDIO_RECORDING: String = "voiceRecording"
    public const val UNKNOWN: String = "unknown"
}
