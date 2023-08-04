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

package io.getstream.chat.android.compose.state.messages.attachments

/**
 * Represents the currently active attachment picker mode.
 */
public sealed class AttachmentsPickerMode

/**
 * Represents the mode with media files from the device.
 */
public object Images : AttachmentsPickerMode() { override fun toString(): String = "Images" }

/**
 * Represents the mode with files from the device.
 */
public object Files : AttachmentsPickerMode() { override fun toString(): String = "Files" }

/**
 * Represents the mode with media capture.
 */
public object MediaCapture : AttachmentsPickerMode() { override fun toString(): String = "MediaCapture" }

/**
 * User-customizable picker mode, with any number of extra properties.
 *
 * @param extraProperties Map of key-value pairs that let you store extra data for this picker mode.
 */
public data class CustomPickerMode(
    public val extraProperties: Map<String, Any> = emptyMap(),
) : AttachmentsPickerMode()
