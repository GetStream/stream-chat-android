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
 * Represents an attachment picker mode.
 */
public interface AttachmentPickerMode

/**
 * Mode for picking images and videos from the gallery.
 *
 * @param allowMultipleSelection Whether multiple items can be selected.
 * @param mediaType The type of media to show in the gallery.
 */
public data class GalleryPickerMode(
    val allowMultipleSelection: Boolean = true,
    val mediaType: MediaType = MediaType.ImagesAndVideos,
) : AttachmentPickerMode

/**
 * Mode for picking files from storage.
 *
 * @param allowMultipleSelection Whether multiple files can be selected.
 */
public data class FilePickerMode(
    val allowMultipleSelection: Boolean = true,
) : AttachmentPickerMode

/**
 * Mode for capturing photos or videos with the camera.
 *
 * @param captureMode The type of media that can be captured.
 */
public data class CameraPickerMode(
    val captureMode: CaptureMode = CaptureMode.PhotoAndVideo,
) : AttachmentPickerMode

/**
 * Mode for creating a poll.
 *
 * @param autoShowCreateDialog Whether to automatically show the create poll dialog when the picker is opened.
 */
public data class PollPickerMode(
    val autoShowCreateDialog: Boolean = true,
) : AttachmentPickerMode

/**
 * Mode for selecting a command.
 */
public data object CommandPickerMode : AttachmentPickerMode

/**
 * The type of media to show in the gallery picker.
 */
public enum class MediaType {
    ImagesOnly,
    VideosOnly,
    ImagesAndVideos,
}

/**
 * The type of media that can be captured with the camera.
 */
public enum class CaptureMode {
    Photo,
    Video,
    PhotoAndVideo,
}
