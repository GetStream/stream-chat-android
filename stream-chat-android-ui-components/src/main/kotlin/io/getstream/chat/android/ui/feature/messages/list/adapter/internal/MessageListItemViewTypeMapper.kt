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

package io.getstream.chat.android.ui.feature.messages.list.adapter.internal

import io.getstream.chat.android.client.utils.attachment.isAudioRecording
import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.client.utils.message.isError
import io.getstream.chat.android.client.utils.message.isGiphyEphemeral
import io.getstream.chat.android.client.utils.message.isModerationBounce
import io.getstream.chat.android.client.utils.message.isSystem
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItem
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.CUSTOM_ATTACHMENTS
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.ERROR_MESSAGE
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.FILE_ATTACHMENTS
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.GIPHY
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.GIPHY_ATTACHMENT
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.LINK_ATTACHMENTS
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.LOADING_INDICATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.MEDIA_ATTACHMENT
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.MESSAGE_DELETED
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.PLAIN_TEXT
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.POLL
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.START_OF_THE_CHANNEL
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.SYSTEM_MESSAGE
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.THREAD_PLACEHOLDER
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.THREAD_SEPARATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.TYPING_INDICATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.UNREAD_SEPARATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.uiutils.extension.hasLink
import io.getstream.chat.android.uiutils.extension.isFailed
import io.getstream.chat.android.uiutils.extension.isUploading

internal object MessageListItemViewTypeMapper {

    fun getViewTypeValue(messageListItem: MessageListItem, attachmentFactoryManager: AttachmentFactoryManager): Int = when (messageListItem) {
        is MessageListItem.DateSeparatorItem -> DATE_DIVIDER
        is MessageListItem.LoadingMoreIndicatorItem -> LOADING_INDICATOR
        is MessageListItem.ThreadSeparatorItem -> THREAD_SEPARATOR
        is MessageListItem.MessageItem -> messageItemToViewType(messageListItem, attachmentFactoryManager)
        is MessageListItem.TypingItem -> TYPING_INDICATOR
        is MessageListItem.ThreadPlaceholderItem -> THREAD_PLACEHOLDER
        is MessageListItem.UnreadSeparatorItem -> UNREAD_SEPARATOR
        is MessageListItem.StartOfTheChannelItem -> START_OF_THE_CHANNEL
    }

    /**
     * Transforms the given [messageItem] to the type of the message we should show in the list.
     *
     * @param messageItem The message item that holds all the information required to generate a message type.
     * @param attachmentFactoryManager A manager for the registered custom attachment factories.
     * @return The [Int] message type.
     */
    private fun messageItemToViewType(
        messageItem: MessageListItem.MessageItem,
        attachmentFactoryManager: AttachmentFactoryManager,
    ): Int {
        val message = messageItem.message

        val (linksAndGiphy, _) = message.attachments.partition { attachment -> attachment.hasLink() }
        val containsGiphy = linksAndGiphy.any(Attachment::isGiphy)
        val hasAttachments = message.attachments.isNotEmpty()

        val containsOnlyLinks = message.containsOnlyLinkAttachments()

        return when {
            message.isPoll() -> POLL
            message.isDeleted() -> MESSAGE_DELETED
            message.isError() && !message.isModerationBounce() -> ERROR_MESSAGE
            message.isSystem() -> SYSTEM_MESSAGE
            message.isGiphyEphemeral() -> GIPHY
            hasAttachments -> when {
                attachmentFactoryManager.canHandle(message) -> CUSTOM_ATTACHMENTS
                containsGiphy -> GIPHY_ATTACHMENT
                containsOnlyLinks -> LINK_ATTACHMENTS
                message.isMediaAttachment() -> MEDIA_ATTACHMENT
                else -> FILE_ATTACHMENTS
            }
            else -> PLAIN_TEXT
        }
    }

    private fun Message.isPoll() = poll != null

    /**
     * Checks if the message contains only image or video attachments (Can also optionally contain links).
     */
    private fun Message.isMediaAttachment(): Boolean = attachments.isNotEmpty() &&
        attachments.all { it.isImage() || it.isVideo() || it.hasLink() || it.isAudioRecording() } &&
        attachments.none { it.isUploading() || it.isFailed() }

    /**
     * Checks if all attachments are link attachments.
     */
    private fun Message.containsOnlyLinkAttachments(): Boolean {
        if (this.attachments.isEmpty()) return false

        return this.attachments.all { attachment -> attachment.hasLink() }
    }
}
