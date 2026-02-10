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

/**
 * Represents a mode (tab) in the attachment picker.
 *
 * Each mode defines a specific type of attachment that users can select or create.
 * Implement this interface to create custom picker modes, or use the built-in implementations:
 * - [GalleryPickerMode] - Pick images and videos from device gallery
 * - [FilePickerMode] - Pick files from device storage
 * - [CameraPickerMode] - Capture photos or videos with the camera
 * - [PollPickerMode] - Create a poll
 * - [CommandPickerMode] - Select a slash command
 *
 * @see AttachmentPickerConfig for configuring which modes are available
 */
public interface AttachmentPickerMode

/**
 * Mode for picking images and videos from the device gallery.
 *
 * When selected, displays a grid of media files from the device's storage. Requires storage
 * permissions when using the in-app picker (not required for system picker).
 *
 * @param allowMultipleSelection When `true`, users can select multiple items. When `false`,
 * only single selection is allowed. Defaults to `true`.
 * @param mediaType Filters which media types are shown. See [MediaType] for options.
 * Defaults to [MediaType.ImagesAndVideos].
 */
public data class GalleryPickerMode(
    val allowMultipleSelection: Boolean = true,
    val mediaType: MediaType = MediaType.ImagesAndVideos,
) : AttachmentPickerMode

/**
 * Mode for picking files from device storage.
 *
 * When selected, displays a list of files from the device's storage including documents,
 * audio files, and other file types. Requires storage permissions when using the in-app picker.
 *
 * @param allowMultipleSelection When `true`, users can select multiple files. When `false`,
 * only single selection is allowed. Defaults to `true`.
 */
public data class FilePickerMode(
    val allowMultipleSelection: Boolean = true,
) : AttachmentPickerMode

/**
 * Mode for capturing photos or videos using the device camera.
 *
 * When selected, launches the camera to capture media. The captured media is automatically
 * added to the message composer as an attachment.
 *
 * @param captureMode Determines what type of media can be captured. See [CaptureMode] for options.
 * Defaults to [CaptureMode.PhotoAndVideo].
 */
public data class CameraPickerMode(
    val captureMode: CaptureMode = CaptureMode.PhotoAndVideo,
) : AttachmentPickerMode

/**
 * Mode for creating a poll attachment.
 *
 * When selected, shows the poll creation interface where users can configure poll options,
 * questions, and settings. The created poll is attached to the message.
 *
 * Note: Poll creation is only available when the channel has the "polls" capability enabled.
 *
 * @param autoShowCreateDialog When `true`, automatically shows the poll creation dialog when
 * this mode is selected. When `false`, shows a button to open the dialog. Defaults to `true`.
 */
public data class PollPickerMode(
    val autoShowCreateDialog: Boolean = true,
) : AttachmentPickerMode

/**
 * Mode for selecting a slash command.
 *
 * When selected, displays a list of available commands (like /giphy, /mute, etc.) that can
 * be inserted into the message composer. The available commands are determined by the
 * channel configuration.
 */
public data object CommandPickerMode : AttachmentPickerMode

/**
 * Defines which media types are shown in the gallery picker.
 *
 * @property ImagesOnly Show only image files (jpg, png, gif, webp, etc.)
 * @property VideosOnly Show only video files (mp4, mov, etc.)
 * @property ImagesAndVideos Show both images and videos
 */
public enum class MediaType {
    ImagesOnly,
    VideosOnly,
    ImagesAndVideos,
}

/**
 * Defines what type of media can be captured with the camera.
 *
 * @property Photo Only allow photo capture
 * @property Video Only allow video recording
 * @property PhotoAndVideo Allow both photo capture and video recording
 */
public enum class CaptureMode {
    Photo,
    Video,
    PhotoAndVideo,
}
