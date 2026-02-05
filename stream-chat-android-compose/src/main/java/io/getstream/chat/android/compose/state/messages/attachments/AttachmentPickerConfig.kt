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

package io.getstream.chat.android.compose.state.messages.attachments

import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Configuration for the attachment picker component.
 *
 * This class allows you to customize the behavior and available options in the attachment picker.
 * You can configure it through [ChatTheme.attachmentPickerConfig].
 *
 * Example usage:
 * ```kotlin
 * ChatTheme(
 *     attachmentPickerConfig = AttachmentPickerConfig(
 *         useSystemPicker = true,
 *         modes = listOf(GalleryPickerMode(), FilePickerMode(), CameraPickerMode()),
 *     )
 * ) {
 *     // Your chat UI
 * }
 * ```
 *
 * @param useSystemPicker When `true`, uses the system's native file/media picker which does not require
 * storage permissions. When `false` (default), uses the in-app picker which shows a grid of media files
 * but requires storage permissions to be granted.
 * @param modes The list of [AttachmentPickerMode] instances that define which attachment types are available.
 * The order of modes determines the order of tabs in the picker. Defaults to gallery, files, camera, poll,
 * and commands.
 */
public data class AttachmentPickerConfig(
    val useSystemPicker: Boolean = false,
    val modes: List<AttachmentPickerMode> = DefaultAttachmentPickerModes,
)

private val DefaultAttachmentPickerModes = listOf(
    GalleryPickerMode(),
    FilePickerMode(),
    CameraPickerMode(),
    PollPickerMode(),
    CommandPickerMode,
)
