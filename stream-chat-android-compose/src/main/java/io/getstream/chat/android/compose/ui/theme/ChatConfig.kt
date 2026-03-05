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

package io.getstream.chat.android.compose.ui.theme

import android.os.Parcelable
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CommandPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import kotlinx.parcelize.Parcelize

/**
 * Defines where the mute indicator icon is placed in the channel list item.
 */
public enum class MuteIndicatorPosition {
    /** Icon appears inline after the channel name in the title row. */
    InlineTitle,

    /** Icon appears at the trailing end of the message/preview row. */
    TrailingBottom,
}

/**
 * Behavioral configuration for the channel list.
 *
 * @param muteIndicatorPosition Where the mute icon is placed in the channel list item.
 * @param swipeActionsEnabled Whether swipe-to-reveal actions are enabled on channel list items.
 */
public data class ChannelListConfig(
    val muteIndicatorPosition: MuteIndicatorPosition = MuteIndicatorPosition.InlineTitle,
    val swipeActionsEnabled: Boolean = true,
)

/**
 * Central behavioral configuration for the Chat SDK, accessible through `ChatTheme.config`.
 *
 * Groups all feature-flag and behavioral settings by feature area so integrators have a single
 * place to discover and configure SDK behaviour. Properties are ordered from most cross-cutting
 * to most component-scoped.
 *
 * @param translation Configuration for message translation (cross-cutting: channel list, message list, composer).
 * @param messageList Configuration for the message list behavior.
 * @param mediaGallery Configuration for the media gallery preview screen.
 * @param composer Configuration for the message composer behavior.
 * @param channelList Configuration for the channel list behavior.
 * @param attachmentPicker Configuration for the attachment picker behavior.
 */
public data class ChatConfig(
    val translation: TranslationConfig = TranslationConfig(),
    val messageList: MessageListConfig = MessageListConfig(),
    val mediaGallery: MediaGalleryConfig = MediaGalleryConfig(),
    val composer: ComposerConfig = ComposerConfig(),
    val channelList: ChannelListConfig = ChannelListConfig(),
    val attachmentPicker: AttachmentPickerConfig = AttachmentPickerConfig(),
)

/**
 * Behavioral configuration for message translation.
 *
 * This is a cross-cutting concern that affects how message text is resolved in the channel list
 * (message previews), message list (display and labels), composer (quoted replies), and clipboard.
 *
 * @param enabled Whether automatic message translation is enabled.
 * @param showOriginalEnabled Whether users can toggle to see the original (untranslated) text.
 */
public data class TranslationConfig(
    val enabled: Boolean = false,
    val showOriginalEnabled: Boolean = false,
)

/**
 * Behavioral configuration for the message list.
 *
 * @param readCountEnabled Whether the read count badge is displayed for messages.
 * @param videoThumbnailsEnabled Whether video thumbnails are displayed inside video previews.
 */
public data class MessageListConfig(
    val readCountEnabled: Boolean = true,
    val videoThumbnailsEnabled: Boolean = true,
)

/**
 * Configuration for the media gallery preview screen.
 * By default, all UI elements are visible.
 *
 * This class implements [Parcelable] because it is currently transported via Intent extras to
 * [MediaGalleryPreviewActivity][io.getstream.chat.android.compose.ui.attachments.preview.MediaGalleryPreviewActivity].
 *
 * @param isCloseVisible If the close button is visible.
 * @param isOptionsVisible If the options button is visible.
 * @param isShareVisible If the share button is visible.
 * @param isGalleryVisible If the gallery button is visible.
 * @param optionsConfig The configuration for the options in the media gallery.
 */
@Parcelize
public data class MediaGalleryConfig(
    val isCloseVisible: Boolean = true,
    val isOptionsVisible: Boolean = true,
    val isShareVisible: Boolean = true,
    val isGalleryVisible: Boolean = true,
    val optionsConfig: MediaGalleryOptionsConfig = MediaGalleryOptionsConfig(),
) : Parcelable

/**
 * Configuration for the individual options in the media gallery options menu.
 * By default, all options are visible.
 *
 * This class implements [Parcelable] because it is nested inside [MediaGalleryConfig].
 *
 * @param isShowInChatVisible If the "Show in chat" option is visible.
 * @param isReplyVisible If the "Reply" option is visible.
 * @param isSaveMediaVisible If the "Save media" option is visible.
 * @param isDeleteVisible If the "Delete" option is visible.
 */
@Parcelize
public data class MediaGalleryOptionsConfig(
    val isShowInChatVisible: Boolean = true,
    val isReplyVisible: Boolean = true,
    val isSaveMediaVisible: Boolean = true,
    val isDeleteVisible: Boolean = true,
) : Parcelable

/**
 * Behavioral configuration for the message composer.
 *
 * @param audioRecordingEnabled Whether the audio recording feature is enabled.
 * @param audioRecordingSendOnComplete If `true`, sends the recording on "Complete" button click.
 * If `false`, attaches it for manual sending.
 * @param linkPreviewEnabled Whether link previews are shown in the composer.
 * @param floatingStyleEnabled Whether the message composer uses the floating style.
 */
public data class ComposerConfig(
    val audioRecordingEnabled: Boolean = false,
    val audioRecordingSendOnComplete: Boolean = true,
    val linkPreviewEnabled: Boolean = false,
    val floatingStyleEnabled: Boolean = false,
)

/**
 * Configuration for the attachment picker component.
 *
 * @param useSystemPicker When `true` (default), uses the system's native file/media picker which does not require
 * storage permissions. When `false`, uses the in-app picker which shows a grid of media files but requires
 * storage permissions to be granted and declared in your manifest.
 * @param modes The list of [AttachmentPickerMode] instances that define which attachment types are available.
 * The order of modes determines the order of tabs in the picker. Defaults to gallery, files, camera, poll,
 * and commands.
 */
public data class AttachmentPickerConfig(
    val useSystemPicker: Boolean = true,
    val modes: List<AttachmentPickerMode> = DefaultAttachmentPickerModes,
)

private val DefaultAttachmentPickerModes = listOf(
    GalleryPickerMode(),
    FilePickerMode(),
    CameraPickerMode(),
    PollPickerMode(),
    CommandPickerMode,
)
