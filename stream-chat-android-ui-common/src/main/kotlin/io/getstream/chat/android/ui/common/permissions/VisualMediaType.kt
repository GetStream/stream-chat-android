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

package io.getstream.chat.android.ui.common.permissions

import androidx.activity.result.contract.ActivityResultContracts
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Defines the type of media that can be picked.
 *
 * @param value The name of the media type.
 */
public enum class VisualMediaType(public val value: String) {

    /**
     * Media of type - Image.
     */
    IMAGE("image"),

    /**
     * Media of type - Video.
     */
    VIDEO("video"),

    /**
     * Media of type - Image/Video.
     */
    IMAGE_AND_VIDEO("image_and_video"),
}

/**
 * Maps the given [VisualMediaType] to the corresponding [ActivityResultContracts.PickVisualMedia.VisualMediaType].
 */
@InternalStreamChatApi
public fun VisualMediaType.toContractVisualMediaType(): ActivityResultContracts.PickVisualMedia.VisualMediaType =
    when (this) {
        VisualMediaType.IMAGE -> ActivityResultContracts.PickVisualMedia.ImageOnly
        VisualMediaType.VIDEO -> ActivityResultContracts.PickVisualMedia.VideoOnly
        VisualMediaType.IMAGE_AND_VIDEO -> ActivityResultContracts.PickVisualMedia.ImageAndVideo
    }
