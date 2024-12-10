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

package io.getstream.chat.android.compose.ui.components.messageoptions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.message.isGiphy
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.BlockUser
import io.getstream.chat.android.ui.common.state.messages.Copy
import io.getstream.chat.android.ui.common.state.messages.Delete
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.Flag
import io.getstream.chat.android.ui.common.state.messages.MarkAsUnread
import io.getstream.chat.android.ui.common.state.messages.Pin
import io.getstream.chat.android.ui.common.state.messages.Reply
import io.getstream.chat.android.ui.common.state.messages.Resend
import io.getstream.chat.android.ui.common.state.messages.ThreadReply
import io.getstream.chat.android.uiutils.extension.hasLink

/**
 * Displays all available [MessageOptionItem]s.
 *
 * @param options List of available options.
 * @param onMessageOptionSelected Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 * @param itemContent Composable that allows the user to customize the individual items shown in [MessageOptions].
 * By default shows individual message items.
 */
@Composable
public fun MessageOptions(
    options: List<MessageOptionItemState>,
    onMessageOptionSelected: (MessageOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable ColumnScope.(MessageOptionItemState) -> Unit = { option ->
        DefaultMessageOptionItem(
            option = option,
            onMessageOptionSelected = onMessageOptionSelected,
        )
    },
) {
    Column(modifier = modifier) {
        options.forEach { option ->
            key(option.action) {
                itemContent(option)
            }
        }
    }
}

/**
 * The default message option item.
 *
 * @param option The represented option.
 * @param onMessageOptionSelected Handler when the user selects the option.
 */
@Composable
internal fun DefaultMessageOptionItem(
    option: MessageOptionItemState,
    onMessageOptionSelected: (MessageOptionItemState) -> Unit,
) {
    MessageOptionItem(
        modifier = Modifier
            .fillMaxWidth()
            .height(ChatTheme.dimens.messageOptionsItemHeight)
            .clickable(
                onClick = { onMessageOptionSelected(option) },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
            ),
        option = option,
    )
}

/**
 * Builds the default message options we show to our users. A different set of options
 * is shown for pending and sent messages.
 *
 * @param selectedMessage Currently selected message, used to callbacks.
 * @param currentUser Current user, used to expose different states for messages.
 * @param isInThread If the message is in a thread or not, to block off some options.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 */
@Composable
public fun defaultMessageOptionsState(
    selectedMessage: Message,
    currentUser: User?,
    isInThread: Boolean,
    ownCapabilities: Set<String>,
): List<MessageOptionItemState> {
    if (selectedMessage.id.isEmpty()) {
        return emptyList()
    }

    val selectedMessageUserId = selectedMessage.user.id

    val isTextOnlyMessage = selectedMessage.text.isNotEmpty() && selectedMessage.attachments.isEmpty()
    val hasLinks = selectedMessage.attachments.any { it.hasLink() && !it.isGiphy() }
    val isOwnMessage = selectedMessageUserId == currentUser?.id
    val isMessageSynced = selectedMessage.syncStatus == SyncStatus.COMPLETED
    val isMessageFailed = selectedMessage.syncStatus == SyncStatus.FAILED_PERMANENTLY

    // user capabilities
    val canQuoteMessage = ownCapabilities.contains(ChannelCapabilities.QUOTE_MESSAGE)
    val canThreadReply = ownCapabilities.contains(ChannelCapabilities.SEND_REPLY)
    val canPinMessage = ownCapabilities.contains(ChannelCapabilities.PIN_MESSAGE)
    val canDeleteOwnMessage = ownCapabilities.contains(ChannelCapabilities.DELETE_OWN_MESSAGE)
    val canDeleteAnyMessage = ownCapabilities.contains(ChannelCapabilities.DELETE_ANY_MESSAGE)
    val canEditOwnMessage = ownCapabilities.contains(ChannelCapabilities.UPDATE_OWN_MESSAGE)
    val canEditAnyMessage = ownCapabilities.contains(ChannelCapabilities.UPDATE_ANY_MESSAGE)
    val canMarkAsUnread = ownCapabilities.contains(ChannelCapabilities.READ_EVENTS)
    val canFlagMessage = ownCapabilities.contains(ChannelCapabilities.FLAG_MESSAGE)

    val isThreadReplyPossible = !isInThread && isMessageSynced && canThreadReply
    val isEditMessagePossible = ((isOwnMessage && canEditOwnMessage) || canEditAnyMessage) && !selectedMessage.isGiphy()
    val isDeleteMessagePossible = canDeleteAnyMessage || (isOwnMessage && canDeleteOwnMessage)

    // options menu item visibility
    val visibility = ChatTheme.messageOptionsTheme.optionVisibility

    return listOfNotNull(
        if (visibility.isRetryMessageVisible && isOwnMessage && isMessageFailed) {
            MessageOptionItemState(
                title = R.string.stream_compose_resend_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_resend),
                action = Resend(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (visibility.isReplyVisible && isMessageSynced && canQuoteMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_reply,
                iconPainter = painterResource(R.drawable.stream_compose_ic_reply),
                action = Reply(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (visibility.isThreadReplyVisible && isThreadReplyPossible) {
            MessageOptionItemState(
                title = R.string.stream_compose_thread_reply,
                iconPainter = painterResource(R.drawable.stream_compose_ic_thread),
                action = ThreadReply(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (visibility.isMarkAsUnreadVisible && canMarkAsUnread) {
            MessageOptionItemState(
                title = R.string.stream_compose_mark_as_unread,
                iconPainter = painterResource(R.drawable.stream_compose_ic_mark_as_unread),
                action = MarkAsUnread(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (visibility.isCopyTextVisible && (isTextOnlyMessage || hasLinks)) {
            MessageOptionItemState(
                title = R.string.stream_compose_copy_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_copy),
                action = Copy(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (visibility.isEditMessageVisible && isEditMessagePossible) {
            MessageOptionItemState(
                title = R.string.stream_compose_edit_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_edit),
                action = Edit(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (visibility.isFlagMessageVisible && canFlagMessage && !isOwnMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_flag_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_flag),
                action = Flag(selectedMessage),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconColor = ChatTheme.colors.textLowEmphasis,
            )
        } else {
            null
        },
        if (visibility.isPinMessageVisible && isMessageSynced && canPinMessage) {
            MessageOptionItemState(
                title = if (selectedMessage.pinned) R.string.stream_compose_unpin_message else R.string.stream_compose_pin_message,
                action = Pin(selectedMessage),
                iconPainter = painterResource(
                    id = if (selectedMessage.pinned) {
                        R.drawable.stream_compose_ic_unpin
                    } else {
                        R.drawable.stream_compose_ic_pin
                    },
                ),
                iconColor = ChatTheme.colors.textLowEmphasis,
                titleColor = ChatTheme.colors.textHighEmphasis,
            )
        } else {
            null
        },
        if (visibility.isBlockUserVisible && !isOwnMessage) {
            MessageOptionItemState(
                title = R.string.stream_compose_block_user,
                iconPainter = painterResource(R.drawable.stream_compose_ic_clear),
                action = BlockUser(selectedMessage),
                iconColor = ChatTheme.colors.textLowEmphasis,
                titleColor = ChatTheme.colors.textHighEmphasis,
            )
        } else {
            null
        },
        if (visibility.isDeleteMessageVisible && isDeleteMessagePossible) {
            MessageOptionItemState(
                title = R.string.stream_compose_delete_message,
                iconPainter = painterResource(R.drawable.stream_compose_ic_delete),
                action = Delete(selectedMessage),
                iconColor = ChatTheme.colors.errorAccent,
                titleColor = ChatTheme.colors.errorAccent,
            )
        } else {
            null
        },
    )
}

/**
 * Preview of [MessageOptions] for a delivered message of the current user.
 */
@Preview(showBackground = true, name = "MessageOptions Preview (Own Message)")
@Composable
private fun MessageOptionsForOwnMessagePreview() {
    MessageOptionsPreview(
        messageUser = PreviewUserData.user1,
        currentUser = PreviewUserData.user1,
        syncStatus = SyncStatus.COMPLETED,
    )
}

/**
 * Preview of [MessageOptions] for theirs message.
 */
@Preview(showBackground = true, name = "MessageOptions Preview (Theirs Message)")
@Composable
private fun MessageOptionsForTheirsMessagePreview() {
    MessageOptionsPreview(
        messageUser = PreviewUserData.user1,
        currentUser = PreviewUserData.user2,
        syncStatus = SyncStatus.COMPLETED,
    )
}

/**
 * Preview of [MessageOptions] for a failed message.
 */
@Preview(showBackground = true, name = "MessageOptions Preview (Failed Message)")
@Composable
private fun MessageOptionsForFailedMessagePreview() {
    MessageOptionsPreview(
        messageUser = PreviewUserData.user1,
        currentUser = PreviewUserData.user1,
        syncStatus = SyncStatus.FAILED_PERMANENTLY,
    )
}

/**
 * Shows [MessageOptions] preview for the provided parameters.
 *
 * @param messageUser The user who sent the message.
 * @param currentUser The currently logged in user.
 * @param syncStatus The message sync status.
 */
@Composable
private fun MessageOptionsPreview(
    messageUser: User,
    currentUser: User,
    syncStatus: SyncStatus,
) {
    ChatTheme {
        val selectedMMessage = PreviewMessageData.message1.copy(
            user = messageUser,
            syncStatus = syncStatus,
        )

        val messageOptionsStateList = defaultMessageOptionsState(
            selectedMessage = selectedMMessage,
            currentUser = currentUser,
            isInThread = false,
            ownCapabilities = ChannelCapabilities.toSet(),
        )

        MessageOptions(options = messageOptionsStateList, onMessageOptionSelected = {})
    }
}
