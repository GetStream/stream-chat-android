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

package io.getstream.chat.android.compose.ui.messages.attachments

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.CameraPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.FilePickerMode
import io.getstream.chat.android.compose.state.messages.attachments.GalleryPickerMode
import io.getstream.chat.android.compose.state.messages.attachments.PollPickerMode
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.AttachmentsPickerViewModel
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData

/**
 * The main attachment picker component that allows users to select and attach files to messages.
 *
 * This composable displays tabs for different attachment types (gallery, files, camera, polls, commands)
 * and the corresponding content for the selected tab. It is typically wrapped by [AttachmentPickerMenu]
 * which handles the animated expand/collapse behavior and positioning.
 *
 * The picker behavior is configured through [ChatTheme.attachmentPickerConfig]:
 * - When `useSystemPicker` is `true`, shows buttons that launch system pickers (no permissions required)
 * - When `useSystemPicker` is `false`, shows an in-app grid picker (requires storage permissions)
 *
 * The picker integrates with [AttachmentsPickerViewModel] to manage state and with the message composer
 * to add selected attachments to messages.
 *
 * For customization, override the picker components in [ChatTheme.componentFactory]:
 * - `AttachmentTypePicker` - The tab bar for switching between modes
 * - `AttachmentPickerContent` - The content area for each mode
 * - `AttachmentSystemPicker` - The system picker variant
 *
 * @param attachmentsPickerViewModel The [AttachmentsPickerViewModel] that manages picker state,
 * including the current mode, available attachments, and selection state.
 * @param modifier The modifier to be applied to the picker container.
 * @param messageMode The current message mode ([MessageMode.Normal] or [MessageMode.MessageThread]).
 * Used to determine if poll creation is available (not available in threads).
 * @param actions The [AttachmentPickerActions] that handle user interactions within the picker.
 * Use [AttachmentPickerActions.pickerDefaults] for standalone usage or
 * [AttachmentPickerActions.defaultActions] for full integration with the message composer.
 */
@Composable
public fun AttachmentPicker(
    attachmentsPickerViewModel: AttachmentsPickerViewModel,
    modifier: Modifier = Modifier,
    messageMode: MessageMode = MessageMode.Normal,
    actions: AttachmentPickerActions = AttachmentPickerActions.pickerDefaults(attachmentsPickerViewModel),
) {
    BackHandler(onBack = actions.onDismiss)

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        attachmentsPickerViewModel.submittedAttachments.collect { submitted ->
            if (submitted.hasUnsupportedFiles) {
                Toast.makeText(context, R.string.stream_compose_message_composer_file_not_supported, Toast.LENGTH_SHORT)
                    .show()
            }
            if (submitted.attachments.isNotEmpty()) {
                actions.onAttachmentsSelected(submitted.attachments)
            }
        }
    }

    val onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit = { metaData ->
        actions.onAttachmentsSelected(attachmentsPickerViewModel.getAttachmentsFromMetadata(metaData))
    }

    Surface(
        modifier = modifier.testTag("Stream_AttachmentsPicker"),
        color = ChatTheme.colors.backgroundElevationElevation1,
    ) {
        if (ChatTheme.attachmentPickerConfig.useSystemPicker) {
            ChatTheme.componentFactory.AttachmentSystemPicker(
                channel = attachmentsPickerViewModel.channel,
                messageMode = messageMode,
                attachments = attachmentsPickerViewModel.attachments,
                actions = actions,
                onUrisSelected = attachmentsPickerViewModel::resolveAndSubmitUris,
                onAttachmentsSubmitted = onAttachmentsSubmitted,
            )
        } else {
            Column {
                ChatTheme.componentFactory.AttachmentTypePicker(
                    channel = attachmentsPickerViewModel.channel,
                    messageMode = messageMode,
                    selectedMode = attachmentsPickerViewModel.pickerMode,
                    onModeSelected = attachmentsPickerViewModel::setPickerMode,
                    trailingContent = {},
                )
                AnimatedContent(
                    targetState = attachmentsPickerViewModel.pickerMode,
                ) { pickerMode ->
                    ChatTheme.componentFactory.AttachmentPickerContent(
                        pickerMode = pickerMode,
                        commands = attachmentsPickerViewModel.channel.config.commands,
                        attachments = attachmentsPickerViewModel.attachments,
                        onLoadAttachments = attachmentsPickerViewModel::loadAttachments,
                        onUrisSelected = attachmentsPickerViewModel::resolveAndSubmitUris,
                        actions = actions,
                        onAttachmentsSubmitted = onAttachmentsSubmitted,
                    )
                }
            }
        }
    }
}

/**
 * A helper property to check if the current [AttachmentPickerMode] supports multiple selections.
 *
 * This will return:
 * - `true` or `false` for [FilePickerMode] and [GalleryPickerMode], based on their `allowMultipleSelection` property.
 * - `null` for other modes like [CameraPickerMode] or [PollPickerMode] that do not support this concept.
 */
internal val AttachmentPickerMode.allowMultipleSelection: Boolean?
    get() = when (this) {
        is FilePickerMode -> allowMultipleSelection
        is GalleryPickerMode -> allowMultipleSelection
        else -> null
    }
