/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import io.getstream.chat.android.ui.common.contract.internal.CaptureMediaContract

/**
 * Defines which media type will be allowed.
 */
public enum class PickerMediaMode {
    PHOTO,
    VIDEO,
    PHOTO_AND_VIDEO,
}

/**
 * Maps [PickerMediaMode] into [CaptureMediaContract.Mode].
 */
internal val PickerMediaMode.mode: CaptureMediaContract.Mode
    get() = when (this) {
        PickerMediaMode.PHOTO -> CaptureMediaContract.Mode.PHOTO
        PickerMediaMode.VIDEO -> CaptureMediaContract.Mode.VIDEO
        PickerMediaMode.PHOTO_AND_VIDEO -> CaptureMediaContract.Mode.PHOTO_AND_VIDEO
    }
