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

package io.getstream.chat.android.ui.feature.messages.list.options.message

import android.content.Context
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.R
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
import io.getstream.chat.android.ui.common.state.messages.UnblockUser
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
import io.getstream.chat.android.ui.feature.messages.list.internal.canBlockUser
import io.getstream.chat.android.ui.feature.messages.list.internal.canCopyMessage
import io.getstream.chat.android.ui.feature.messages.list.internal.canDeleteMessage
import io.getstream.chat.android.ui.feature.messages.list.internal.canEditMessage
import io.getstream.chat.android.ui.feature.messages.list.internal.canFlagMessage
import io.getstream.chat.android.ui.feature.messages.list.internal.canMarkAsUnread
import io.getstream.chat.android.ui.feature.messages.list.internal.canPinMessage
import io.getstream.chat.android.ui.feature.messages.list.internal.canReplyToMessage
import io.getstream.chat.android.ui.feature.messages.list.internal.canRetryMessage
import io.getstream.chat.android.ui.feature.messages.list.internal.canThreadReplyToMessage
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat

/**
 * An interface that allows the creation of message option items.
 */
public interface MessageOptionItemsFactory {

    /**
     * Creates [MessageOptionItem]s for the selected message.
     *
     * @param selectedMessage The currently selected message.
     * @param currentUser The currently logged in user.
     * @param isInThread If the message is being displayed in a thread.
     * @param ownCapabilities Set of capabilities the user is given for the current channel.
     * @param style The style to be applied to the view.
     * @return The list of message option items to display.
     */
    public fun createMessageOptionItems(
        selectedMessage: Message,
        currentUser: User?,
        isInThread: Boolean,
        ownCapabilities: Set<String>,
        style: MessageListViewStyle,
    ): List<MessageOptionItem>

    public companion object {
        /**
         * Builds the default message option items factory.
         *
         * @return The default implementation of [MessageOptionItemsFactory].
         */
        public fun defaultFactory(context: Context): MessageOptionItemsFactory {
            return DefaultMessageOptionItemsFactory(context)
        }
    }
}

/**
 * The default implementation of [MessageOptionItemsFactory].
 *
 * @param context The context to load resources.
 */
public open class DefaultMessageOptionItemsFactory(
    private val context: Context,
) : MessageOptionItemsFactory {

    /**
     * Creates [MessageOptionItem]s for the selected message.
     *
     * @param selectedMessage The currently selected message.
     * @param currentUser The currently logged in user.
     * @param isInThread If the message is being displayed in a thread.
     * @param ownCapabilities Set of capabilities the user is given for the current channel.
     * @param style The style to be applied to the view.
     * @return The list of message option items to display.
     */
    override fun createMessageOptionItems(
        selectedMessage: Message,
        currentUser: User?,
        isInThread: Boolean,
        ownCapabilities: Set<String>,
        style: MessageListViewStyle,
    ): List<MessageOptionItem> {
        if (selectedMessage.id.isEmpty()) {
            return emptyList()
        }

        val selectedMessageUserId = selectedMessage.user.id

        return listOfNotNull(
            if (style.canRetryMessage(currentUser, selectedMessage)) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_resend_message),
                    optionIcon = context.getDrawableCompat(style.retryIcon)!!,
                    messageAction = Resend(selectedMessage),
                )
            } else {
                null
            },
            if (style.canReplyToMessage(selectedMessage, ownCapabilities)) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_reply),
                    optionIcon = context.getDrawableCompat(style.replyIcon)!!,
                    messageAction = Reply(selectedMessage),
                )
            } else {
                null
            },
            if (style.canThreadReplyToMessage(selectedMessage, ownCapabilities) && !isInThread) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_thread_reply),
                    optionIcon = context.getDrawableCompat(style.threadReplyIcon)!!,
                    messageAction = ThreadReply(selectedMessage),
                )
            } else {
                null
            },
            if (style.canMarkAsUnread(ownCapabilities)) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_mark_as_unread),
                    optionIcon = context.getDrawableCompat(style.markAsUnreadIcon)!!,
                    messageAction = MarkAsUnread(selectedMessage),
                )
            } else {
                null
            },
            if (style.canCopyMessage(selectedMessage)) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_copy_message),
                    optionIcon = context.getDrawableCompat(style.copyIcon)!!,
                    messageAction = Copy(selectedMessage),
                )
            } else {
                null
            },
            if (style.canEditMessage(currentUser, selectedMessage, ownCapabilities)) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_edit_message),
                    optionIcon = context.getDrawableCompat(style.editIcon)!!,
                    messageAction = Edit(selectedMessage),
                )
            } else {
                null
            },
            if (style.canFlagMessage(currentUser, selectedMessage, ownCapabilities)) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_flag_message),
                    optionIcon = context.getDrawableCompat(style.flagIcon)!!,
                    messageAction = Flag(selectedMessage),
                )
            } else {
                null
            },
            if (style.canPinMessage(selectedMessage, ownCapabilities)) {
                val (pinText, pinIcon) = if (selectedMessage.pinned) {
                    R.string.stream_ui_message_list_unpin_message to style.unpinIcon
                } else {
                    R.string.stream_ui_message_list_pin_message to style.pinIcon
                }

                MessageOptionItem(
                    optionText = context.getString(pinText),
                    optionIcon = context.getDrawableCompat(pinIcon)!!,
                    messageAction = Pin(selectedMessage),
                )
            } else {
                null
            },
            if (style.canBlockUser(currentUser, selectedMessage)) {
                val isSenderBlocked = currentUser?.blockedUserIds?.contains(selectedMessageUserId) == true
                val text = if (isSenderBlocked) {
                    R.string.stream_ui_message_list_unblock_user
                } else {
                    R.string.stream_ui_message_list_block_user
                }
                val icon = if (isSenderBlocked) {
                    style.unblockUserIcon
                } else {
                    style.blockUserIcon
                }
                val action = if (isSenderBlocked) {
                    UnblockUser(selectedMessage)
                } else {
                    BlockUser(selectedMessage)
                }
                MessageOptionItem(
                    optionText = context.getString(text),
                    optionIcon = context.getDrawableCompat(icon)!!,
                    messageAction = action,
                )
            } else {
                null
            },
            if (style.canDeleteMessage(currentUser, selectedMessage, ownCapabilities)) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_delete_message),
                    optionIcon = context.getDrawableCompat(style.deleteIcon)!!,
                    messageAction = Delete(selectedMessage),
                    isWarningItem = true,
                )
            } else {
                null
            },
        )
    }
}
