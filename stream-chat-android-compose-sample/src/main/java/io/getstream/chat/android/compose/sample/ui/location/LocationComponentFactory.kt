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

package io.getstream.chat.android.compose.sample.ui.location

import android.net.Uri
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.utils.message.hasSharedLocation
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.sample.feature.reminders.MessageRemindersComponentFactory
import io.getstream.chat.android.compose.sample.ui.component.SharedLocationItem
import io.getstream.chat.android.compose.sample.vm.SharedLocationViewModelFactory
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerItemState
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentPickerMode
import io.getstream.chat.android.compose.ui.messages.attachments.AttachmentPickerActions
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.AttachmentMetaData
import io.getstream.chat.android.ui.common.state.messages.list.GiphyAction
import io.getstream.chat.android.ui.common.state.messages.list.MessageItemState
import io.getstream.chat.android.ui.common.state.messages.poll.PollSelectionType

/**
 * Factory for creating components related to location sharing.
 */
class LocationComponentFactory(
    private val locationViewModelFactory: SharedLocationViewModelFactory?,
    private val delegate: ChatComponentFactory = MessageRemindersComponentFactory(),
) : ChatComponentFactory by delegate {

    @Composable
    override fun AttachmentTypePicker(
        channel: Channel,
        messageMode: MessageMode,
        selectedMode: AttachmentPickerMode?,
        onModeSelected: (AttachmentPickerMode) -> Unit,
        trailingContent: @Composable RowScope.() -> Unit,
    ) {
        super.AttachmentTypePicker(channel, messageMode, selectedMode, onModeSelected) {
            val isSelected = selectedMode != null && selectedMode::class == LocationPickerMode

            FilledIconToggleButton(
                modifier = Modifier.size(48.dp),
                checked = isSelected,
                onCheckedChange = { onModeSelected(LocationPickerMode) },
                colors = IconButtonDefaults.filledIconToggleButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = ChatTheme.colors.buttonSecondaryText,
                    checkedContainerColor = ChatTheme.colors.backgroundCoreSelected,
                    checkedContentColor = ChatTheme.colors.buttonSecondaryText,
                ),
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = "Share Location",
                )
            }
        }
    }

    @Composable
    override fun AttachmentPickerContent(
        pickerMode: AttachmentPickerMode?,
        commands: List<Command>,
        attachments: List<AttachmentPickerItemState>,
        onLoadAttachments: () -> Unit,
        onUrisSelected: (List<Uri>) -> Unit,
        actions: AttachmentPickerActions,
        onAttachmentsSubmitted: (List<AttachmentMetaData>) -> Unit,
    ) {
        if (pickerMode == LocationPickerMode && locationViewModelFactory != null) {
            LocationPicker(
                viewModelFactory = locationViewModelFactory,
                onDismiss = actions.onDismiss,
            )
        } else {
            super.AttachmentPickerContent(
                pickerMode,
                commands,
                attachments,
                onLoadAttachments,
                onUrisSelected,
                actions,
                onAttachmentsSubmitted,
            )
        }
    }

    @Composable
    override fun MessageContent(
        messageItem: MessageItemState,
        onLongItemClick: (Message) -> Unit,
        onPollUpdated: (Message, Poll) -> Unit,
        onCastVote: (Message, Poll, Option) -> Unit,
        onRemoveVote: (Message, Poll, Vote) -> Unit,
        selectPoll: (Message, Poll, PollSelectionType) -> Unit,
        onAddAnswer: (message: Message, poll: Poll, answer: String) -> Unit,
        onClosePoll: (String) -> Unit,
        onAddPollOption: (poll: Poll, option: String) -> Unit,
        onGiphyActionClick: (GiphyAction) -> Unit,
        onQuotedMessageClick: (Message) -> Unit,
        onLinkClick: ((Message, String) -> Unit)?,
        onUserMentionClick: (User) -> Unit,
        onMediaGalleryPreviewResult: (MediaGalleryPreviewResult?) -> Unit,
    ) {
        val message = messageItem.message
        if (message.hasSharedLocation() && !message.isDeleted()) {
            val location = requireNotNull(message.sharedLocation)
            SharedLocationItem(
                modifier = Modifier.widthIn(max = ChatTheme.dimens.messageItemMaxWidth),
                message = message,
                location = location,
                onMapClick = { url -> onLinkClick?.invoke(message, url) },
                onMapLongClick = { onLongItemClick(message) },
            )
        } else {
            with(delegate) {
                MessageContent(
                    messageItem = messageItem,
                    onLongItemClick = onLongItemClick,
                    onGiphyActionClick = onGiphyActionClick,
                    onQuotedMessageClick = onQuotedMessageClick,
                    onLinkClick = onLinkClick,
                    onUserMentionClick = onUserMentionClick,
                    onMediaGalleryPreviewResult = onMediaGalleryPreviewResult,
                    onPollUpdated = onPollUpdated,
                    onCastVote = onCastVote,
                    onRemoveVote = onRemoveVote,
                    selectPoll = selectPoll,
                    onAddAnswer = onAddAnswer,
                    onClosePoll = onClosePoll,
                    onAddPollOption = onAddPollOption,
                )
            }
        }
    }
}
