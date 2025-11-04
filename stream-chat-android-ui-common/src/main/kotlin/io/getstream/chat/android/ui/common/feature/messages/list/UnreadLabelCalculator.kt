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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message

/**
 * Calculator responsible for determining the unread label state in a message list.
 *
 * This class encapsulates the complex logic for calculating when and how to display an unread
 * messages indicator/button in a chat message list. It handles several edge cases:
 *
 * 1. **Standard Unread Messages**: When other users send messages after the current user's last read position.
 * 2. **Own Messages Marked as Unread**: When a user explicitly marks their own message as unread.
 * 3. **Multiple Own Messages Before Unread**: When the current user has sent multiple messages before
 *    receiving new messages from others, determining the correct last read position.
 * 4. **Uncommitted Pending Messages**: Messages awaiting server-side commit.
 * 5. **Messages Synced After Offline**: Messages that were sent offline and later synced with the server.
 *
 * The calculator is designed to be stateless and pure - it performs calculations based on the provided
 * input parameters without maintaining internal state. The caller ([MessageListController]) is responsible
 * for determining when to invoke the calculator and managing state updates.
 *
 * Integrates with [ChannelState] and [ChannelUserRead] to provide accurate unread information that
 * drives UI elements like the "jump to unread" button and unread count badges.
 *
 * @see MessageListController.UnreadLabel
 * @see MessageListController.observeUnreadLabelState
 */
internal class UnreadLabelCalculator {

    /**
     * Calculates the [MessageListController.UnreadLabel] based on the current read state and channel messages.
     *
     * This method implements the core logic for determining unread message state by:
     * - Identifying unread messages based on [ChannelUserRead.lastReadMessageId]
     * - Detecting if the first unread message is from the current user
     * - Handling special cases for own messages marked as unread
     * - Finding the appropriate last read position when multiple own messages exist
     * - Determining button visibility based on non-deleted unread messages
     *
     * ## Edge Cases Handled:
     *
     * ### Case 0: Last Read Message Not in List
     * When the [ChannelUserRead.lastReadMessageId] is not found in the current messages list
     * (e.g., when messages are loaded around a specific message, or the message list is filtered),
     * the calculator skips complex ownership checks and creates a standard unread label using the
     * provided lastReadMessageId directly. This ensures the unread indicator is displayed even when
     * the exact last read message is not currently loaded.
     *
     * ### Case 1: Standard Unread Messages
     * When messages from other users arrive after the last read position, the label is positioned
     * after the last read message.
     *
     * ### Case 2: Own Message Marked as Unread
     * When a user marks their own message as unread, the system detects this by comparing:
     * - `firstUnreadMessage.createdAt > read.lastRead`
     * - `read.lastRead == lastReadMessage.createdAt`
     *
     * This indicates an explicit "mark as unread" action on an own message.
     *
     * ### Case 3: Multiple Own Messages Before Other's Messages
     * When the unread messages start with one or more of the current user's messages, followed by
     * messages from other users, the calculator finds the last own message before the first other
     * user's message and uses that as the lastReadMessageId. This ensures the unread indicator
     * appears before messages from other users, not before the user's own messages. This scenario
     * can occur when:
     * - A message is sent offline and then synced with the server
     * - The user sends a pending message which is not yet committed to the server
     *
     * ## Caller Responsibilities:
     * The caller ([MessageListController.observeUnreadLabelState]) is responsible for:
     * - Checking if the lastReadMessageId has changed before calling this method
     * - Managing state updates and update triggers
     * - Determining when calculation should be skipped (e.g., for threads)
     *
     * @param channelUserRead The read state for the current user, containing last read message ID and timestamp.
     * @param messages The list of messages in the channel, ordered from oldest to newest.
     * @param currentUserId The ID of the currently logged-in user.
     * @param shouldShowButton Whether the unread button should be visible (controlled by user interactions).
     *
     * @return A [MessageListController.UnreadLabel] if there are unread messages that should be indicated,
     *         or `null` if there are no unread messages or they shouldn't be shown.
     */
    fun calculateUnreadLabel(
        channelUserRead: ChannelUserRead,
        messages: List<Message>,
        currentUserId: String?,
        shouldShowButton: Boolean,
    ): MessageListController.UnreadLabel? {
        // Step 1: Calculate the list of unread messages by folding through all messages
        // and accumulating messages that appear after the lastReadMessageId
        val unreadMessages = messages
            .fold(emptyList<Message>()) { acc, message ->
                when {
                    // When we find the last read message, reset the accumulator (start fresh)
                    channelUserRead.lastReadMessageId == message.id -> emptyList()
                    // Otherwise, keep accumulating messages (these are unread)
                    else -> acc + message
                }
            }

        // Step 2: Find the actual last read message for comparison purposes
        val lastReadMessage = messages
            .firstOrNull { it.id == channelUserRead.lastReadMessageId }

        // Step 2.1: If lastReadMessage is not found in the messages list, we still need to check
        // if all unread messages are from the current user (similar to when lastReadMessage exists).
        // This handles cases where the message list was loaded around a specific message that
        // doesn't include the actual last read message, or after logout/login when read state
        // might be out of sync.
        if (lastReadMessage == null) {
            // Check if all unread messages are from the current user
            val allUnreadMessagesAreFromCurrentUser = unreadMessages.isNotEmpty() &&
                unreadMessages.all { it.user.id == currentUserId }

            // If all unread messages are from the current user, don't show the label
            // (user has already seen their own messages)
            if (allUnreadMessagesAreFromCurrentUser) {
                return null
            }
            
            return calculateStandardUnreadLabel(
                channelUserRead = channelUserRead,
                unreadMessages = unreadMessages,
                shouldShowButton = shouldShowButton,
            )
        }

        // Step 3: Identify the first unread message and check if it belongs to the current user
        val firstUnreadMessage = unreadMessages.firstOrNull()
        val isFirstUnreadMessageOwn =
            firstUnreadMessage != null && firstUnreadMessage.user.id == currentUserId

        // Step 4: Determine if the user explicitly marked their own message as unread
        // This is detected when:
        // - The first unread message is from the current user
        // - The first unread message was created AFTER the lastRead timestamp
        // - The lastRead timestamp equals the lastReadMessage's creation time
        // This pattern indicates the user performed a "mark as unread" action on their own message
        val isOwnMessageMarkedAsUnread = isFirstUnreadMessageOwn &&
            firstUnreadMessage.createdAt?.after(channelUserRead.lastRead) == true &&
            channelUserRead.lastRead == lastReadMessage.createdAt

        // Step 5: Calculate the unread label based on message ownership
        return if (!isFirstUnreadMessageOwn || isOwnMessageMarkedAsUnread) {
            // Case A: First unread message is from another user OR user marked their own message as unread
            calculateStandardUnreadLabel(
                channelUserRead = channelUserRead,
                unreadMessages = unreadMessages,
                shouldShowButton = shouldShowButton,
            )
        } else {
            // Case B: First unread message(s) are from the current user, but not marked as unread
            // This can happen when a message is sent offline and then synced with the server, or
            // when the user sends a pending message which is not yet committed to the server.
            calculateUnreadLabelWithOwnMessagesFirst(
                channelUserRead = channelUserRead,
                unreadMessages = unreadMessages,
                currentUserId = currentUserId,
                shouldShowButton = shouldShowButton,
            )
        }
    }

    /**
     * Calculates the unread label for the standard case where the first unread message is from
     * another user, or the current user explicitly marked their own message as unread.
     *
     * @param channelUserRead The read state containing unread count and last read message ID.
     * @param unreadMessages List of all unread messages.
     * @param shouldShowButton Whether the button should be visible.
     *
     * @return An [MessageListController.UnreadLabel] if conditions are met, null otherwise.
     */
    private fun calculateStandardUnreadLabel(
        channelUserRead: ChannelUserRead,
        unreadMessages: List<Message>,
        shouldShowButton: Boolean,
    ): MessageListController.UnreadLabel? {
        return channelUserRead.lastReadMessageId
            // Don't show label if there are no unread messages
            ?.takeUnless { unreadMessages.isEmpty() }
            // Don't show label if the last message in the list is the last read message
            // (meaning all messages are read)
            ?.takeUnless { unreadMessages.lastOrNull()?.id == it }
            ?.let { lastReadMessageId ->
                MessageListController.UnreadLabel(
                    unreadCount = channelUserRead.unreadMessages,
                    lastReadMessageId = lastReadMessageId,
                    // Only show button if requested AND there are non-deleted unread messages
                    buttonVisibility = shouldShowButton && unreadMessages.any { !it.isDeleted() },
                )
            }
    }

    /**
     * Calculates the unread label when the first unread message(s) are from the current user.
     *
     * This method handles the scenario where:
     * 1. The user sends one or more messages
     * 2. Other users send messages afterward
     * 3. The unread indicator should appear before the other users' messages, not before the
     *    current user's own messages
     *
     * The algorithm finds the last message from the current user that appears before the first
     * message from another user, and uses that as the lastReadMessageId.
     *
     * @param channelUserRead The read state containing unread count.
     * @param unreadMessages List of all unread messages.
     * @param currentUserId The ID of the current user.
     * @param shouldShowButton Whether the button should be visible.
     *
     * @return An [MessageListController.UnreadLabel] if there are unread messages from others, null otherwise.
     */
    private fun calculateUnreadLabelWithOwnMessagesFirst(
        channelUserRead: ChannelUserRead,
        unreadMessages: List<Message>,
        currentUserId: String?,
        shouldShowButton: Boolean,
    ): MessageListController.UnreadLabel? {
        // If there are no unread messages at all, return null
        if (channelUserRead.unreadMessages == 0) {
            return null
        }

        // Find the index of the first message from another user (not the current user)
        val firstOtherMessageIndex = unreadMessages.indexOfFirst { it.user.id != currentUserId }

        // Calculate the index of the last own message before the first other user's message
        val lastOwnMessageIndex = if (firstOtherMessageIndex > 0) {
            // There are own messages before the first other message
            firstOtherMessageIndex - 1
        } else {
            // No messages from other users found, or the first message is from another user
            // Note: this should never happen as we guard against this case in the main method
            -1
        }

        // Get the message at the calculated index
        val lastOwnMessageBeforeOthers = if (lastOwnMessageIndex != -1) {
            unreadMessages.getOrNull(lastOwnMessageIndex)
        } else {
            null
        }

        // Create the unread label using the last own message as the anchor point
        return lastOwnMessageBeforeOthers?.let {
            MessageListController.UnreadLabel(
                unreadCount = channelUserRead.unreadMessages,
                lastReadMessageId = it.id,
                // Only show button if requested AND there are non-deleted unread messages
                buttonVisibility = shouldShowButton &&
                    unreadMessages.any { message -> !message.isDeleted() },
            )
        }
    }
}
