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

package io.getstream.chat.android.ui.message.list.options.message

import android.content.Context
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.ChannelCapabilities
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.common.state.Copy
import io.getstream.chat.android.common.state.Delete
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.Flag
import io.getstream.chat.android.common.state.MuteUser
import io.getstream.chat.android.common.state.Pin
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.common.state.Resend
import io.getstream.chat.android.common.state.ThreadReply
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getDrawableCompat
import io.getstream.chat.android.ui.message.list.MessageListViewStyle
import io.getstream.chat.android.uiutils.extension.hasLink

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

        val isTextOnlyMessage = selectedMessage.text.isNotEmpty() && selectedMessage.attachments.isEmpty()
        val hasLinks = selectedMessage.attachments.any { it.hasLink() && it.type != ModelType.attach_giphy }
        val isOwnMessage = selectedMessageUserId == currentUser?.id
        val isUserMuted = currentUser?.mutes?.any { it.target.id == selectedMessageUserId } ?: false
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

        return listOfNotNull(
            if (style.retryMessageEnabled && isOwnMessage && isMessageFailed) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_resend_message),
                    optionIcon = context.getDrawableCompat(style.retryIcon)!!,
                    messageAction = Resend(selectedMessage),
                )
            } else null,
            if (style.replyEnabled && isMessageSynced && canQuoteMessage) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_reply),
                    optionIcon = context.getDrawableCompat(style.replyIcon)!!,
                    messageAction = Reply(selectedMessage),
                )
            } else null,
            if (style.threadsEnabled && !isInThread && isMessageSynced && canThreadReply) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_thread_reply),
                    optionIcon = context.getDrawableCompat(style.threadReplyIcon)!!,
                    messageAction = ThreadReply(selectedMessage),
                )
            } else null,
            if (style.copyTextEnabled && (isTextOnlyMessage || hasLinks)) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_copy_message),
                    optionIcon = context.getDrawableCompat(style.copyIcon)!!,
                    messageAction = Copy(selectedMessage),
                )
            } else null,
            if (style.editMessageEnabled && ((isOwnMessage && canEditOwnMessage) || canEditAnyMessage) &&
                selectedMessage.command != ModelType.attach_giphy
            ) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_edit_message),
                    optionIcon = context.getDrawableCompat(style.editIcon)!!,
                    messageAction = Edit(selectedMessage),
                )
            } else null,
            if (style.flagEnabled && !isOwnMessage) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_flag_message),
                    optionIcon = context.getDrawableCompat(style.flagIcon)!!,
                    messageAction = Flag(selectedMessage),
                )
            } else null,
            if (style.pinMessageEnabled && isMessageSynced && canPinMessage) {
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
            } else null,
            if (style.deleteMessageEnabled && (canDeleteAnyMessage || (isOwnMessage && canDeleteOwnMessage))) {
                MessageOptionItem(
                    optionText = context.getString(R.string.stream_ui_message_list_delete_message),
                    optionIcon = context.getDrawableCompat(style.deleteIcon)!!,
                    messageAction = Delete(selectedMessage),
                    isWarningItem = true,
                )
            } else null,
            if (style.muteEnabled && !isOwnMessage) {
                val (muteText, muteIcon) = if (isUserMuted) {
                    R.string.stream_ui_message_list_unmute_user to style.unmuteIcon
                } else {
                    R.string.stream_ui_message_list_mute_user to style.muteIcon
                }

                MessageOptionItem(
                    optionText = context.getString(muteText),
                    optionIcon = context.getDrawableCompat(muteIcon)!!,
                    messageAction = MuteUser(selectedMessage),
                )
            } else null
        )
    }
}
