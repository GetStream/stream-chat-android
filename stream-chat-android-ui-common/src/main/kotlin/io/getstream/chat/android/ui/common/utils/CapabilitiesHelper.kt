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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.ui.common.utils

import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.client.utils.message.hasSharedLocation
import io.getstream.chat.android.client.utils.message.isThreadReply
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.uiutils.extension.hasLink

private fun Message.isMessageFailed(): Boolean = syncStatus == SyncStatus.FAILED_PERMANENTLY
private fun Message.isSynced(): Boolean = syncStatus == SyncStatus.COMPLETED
private fun Message.isTextOnly(): Boolean = text.isNotEmpty() && attachments.isEmpty()
private fun Message.hasLinks(): Boolean = attachments.any { it.hasLink() && !it.isGiphy() }
private fun Message.isOwnMessage(currentUser: User?): Boolean = user.id == currentUser?.id
private fun Message.isGiphyCommand(): Boolean = command == AttachmentType.GIPHY
private fun Set<String>.canEditOwnMessage(): Boolean = contains(ChannelCapabilities.UPDATE_OWN_MESSAGE)
private fun Set<String>.canEditAnyMessage(): Boolean = contains(ChannelCapabilities.UPDATE_ANY_MESSAGE)
private fun Set<String>.canDeleteOwnMessage(): Boolean = contains(ChannelCapabilities.DELETE_OWN_MESSAGE)
private fun Set<String>.canDeleteAnyMessage(): Boolean = contains(ChannelCapabilities.DELETE_ANY_MESSAGE)
private fun Set<String>.canFlagMessage(): Boolean = contains(ChannelCapabilities.FLAG_MESSAGE)
private fun Set<String>.canPinMessage(): Boolean = contains(ChannelCapabilities.PIN_MESSAGE)
private fun Set<String>.canMarkAsUnread(): Boolean = contains(ChannelCapabilities.READ_EVENTS)

/**
 * Determines whether a reply (quote) can be made to the given message.
 *
 * A reply is allowed when:
 * - Reply functionality is enabled in the UI configuration
 * - The message has been successfully synced with the backend
 * - The user has the capability to quote messages in the channel
 *
 * @param replyEnabled Whether the reply feature is enabled in the UI.
 * @param message The message to check for reply eligibility.
 * @param ownCapabilities The set of capabilities the current user has in the channel.
 * @return `true` if a reply can be made to the message, `false` otherwise.
 */
public fun canReplyToMessage(
    replyEnabled: Boolean,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = replyEnabled && message.isSynced() && ownCapabilities.contains(ChannelCapabilities.QUOTE_MESSAGE)

/**
 * Determines whether a thread reply can be made to the given message.
 *
 * A thread reply is allowed when:
 * - Thread functionality is enabled in the UI configuration
 * - The user is not currently viewing a thread
 * - The message is not a thread reply
 * - The message has been successfully synced with the backend
 * - The user has the capability to send replies in the channel
 *
 * @param threadsEnabled Whether the thread feature is enabled in the UI.
 * @param isInThread Whether the user is currently viewing messages within a thread.
 * @param message The message to check for thread reply eligibility.
 * @param ownCapabilities The set of capabilities the current user has in the channel.
 * @return `true` if a thread reply can be made to the message, `false` otherwise.
 */
public fun canThreadReplyToMessage(
    threadsEnabled: Boolean,
    isInThread: Boolean,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean =
    threadsEnabled &&
        !isInThread &&
        !message.isThreadReply() &&
        message.isSynced() &&
        ownCapabilities.contains(ChannelCapabilities.SEND_REPLY)

/**
 * Determines whether the given message can be copied to the clipboard.
 *
 * A message can be copied when:
 * - Copy text functionality is enabled in the UI configuration
 * - The message contains text only (no attachments) OR contains links
 *
 * @param copyTextEnabled Whether the copy text feature is enabled in the UI.
 * @param message The message to check for copy eligibility.
 * @return `true` if the message can be copied, `false` otherwise.
 */
public fun canCopyMessage(
    copyTextEnabled: Boolean,
    message: Message,
): Boolean = copyTextEnabled && (message.isTextOnly() || message.hasLinks())

/**
 * Determines whether the given message can be edited.
 *
 * A message can be edited when:
 * - Edit message functionality is enabled in the UI configuration
 * - The user has permission to edit their own messages and it's their message, OR they can edit any message
 * - The message is not a Giphy command
 * - The message does not contain a shared location attachment
 *
 * @param editMessageEnabled Whether the edit message feature is enabled in the UI.
 * @param currentUser The currently authenticated user.
 * @param message The message to check for edit eligibility.
 * @param ownCapabilities The set of capabilities the current user has in the channel.
 * @return `true` if the message can be edited, `false` otherwise.
 */
public fun canEditMessage(
    editMessageEnabled: Boolean,
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = editMessageEnabled &&
    with(ownCapabilities) { ((message.isOwnMessage(currentUser) && canEditOwnMessage()) || canEditAnyMessage()) } &&
    !message.isGiphyCommand() && !message.hasSharedLocation()

/**
 * Determines whether the given message can be deleted.
 *
 * A message can be deleted when:
 * - Delete message functionality is enabled in the UI configuration
 * - The user has permission to delete their own messages and it's their message, OR they can delete any message
 *
 * @param deleteMessageEnabled Whether the delete message feature is enabled in the UI.
 * @param currentUser The currently authenticated user.
 * @param message The message to check for delete eligibility.
 * @param ownCapabilities The set of capabilities the current user has in the channel.
 * @return `true` if the message can be deleted, `false` otherwise.
 */
public fun canDeleteMessage(
    deleteMessageEnabled: Boolean,
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = deleteMessageEnabled &&
    with(ownCapabilities) { ((message.isOwnMessage(currentUser) && canDeleteOwnMessage()) || canDeleteAnyMessage()) }

/**
 * Determines whether the given message can be flagged.
 *
 * A message can be flagged when:
 * - Flag functionality is enabled in the UI configuration
 * - The user has the capability to flag messages in the channel
 * - The message was not sent by the current user (users cannot flag their own messages)
 *
 * @param flagEnabled Whether the flag message feature is enabled in the UI.
 * @param currentUser The currently authenticated user.
 * @param message The message to check for flag eligibility.
 * @param ownCapabilities The set of capabilities the current user has in the channel.
 * @return `true` if the message can be flagged, `false` otherwise.
 */
public fun canFlagMessage(
    flagEnabled: Boolean,
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = flagEnabled && ownCapabilities.canFlagMessage() && !message.isOwnMessage(currentUser)

/**
 * Determines whether the given message can be pinned or unpinned.
 *
 * A message can be pinned when:
 * - Pin message functionality is enabled in the UI configuration
 * - The message has been successfully synced with the backend
 * - The user has the capability to pin messages in the channel
 *
 * @param pinMessageEnabled Whether the pin message feature is enabled in the UI.
 * @param message The message to check for pin eligibility.
 * @param ownCapabilities The set of capabilities the current user has in the channel.
 * @return `true` if the message can be pinned or unpinned, `false` otherwise.
 */
public fun canPinMessage(
    pinMessageEnabled: Boolean,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = pinMessageEnabled && message.isSynced() && ownCapabilities.canPinMessage()

/**
 * Determines whether the user who sent the given message can be blocked.
 *
 * A user can be blocked when:
 * - Block user functionality is enabled in the UI configuration
 * - The message was not sent by the current user (users cannot block themselves)
 *
 * @param blockUserEnabled Whether the block user feature is enabled in the UI.
 * @param currentUser The currently authenticated user.
 * @param message The message whose sender to check for block eligibility.
 * @return `true` if the message sender can be blocked, `false` otherwise.
 */
public fun canBlockUser(
    blockUserEnabled: Boolean,
    currentUser: User?,
    message: Message,
): Boolean = blockUserEnabled && !message.isOwnMessage(currentUser)

/**
 * Determines whether messages in the channel can be marked as unread.
 *
 * Messages can be marked as unread when:
 * - Mark as unread functionality is enabled in the UI configuration
 * - The user has the capability to receive read events in the channel
 *
 * @param markAsUnreadEnabled Whether the mark as unread feature is enabled in the UI.
 * @param ownCapabilities The set of capabilities the current user has in the channel.
 * @return `true` if messages can be marked as unread, `false` otherwise.
 */
public fun canMarkAsUnread(
    markAsUnreadEnabled: Boolean,
    ownCapabilities: Set<String>,
): Boolean = markAsUnreadEnabled && ownCapabilities.canMarkAsUnread()

/**
 * Determines whether the given message can be retried.
 *
 * A message can be retried when:
 * - Retry message functionality is enabled in the UI configuration
 * - The message was sent by the current user
 * - The message has failed permanently
 *
 * @param retryMessageEnabled Whether the retry message feature is enabled in the UI.
 * @param currentUser The currently authenticated user.
 * @param message The message to check for retry eligibility.
 * @return `true` if the message can be retried, `false` otherwise.
 */
public fun canRetryMessage(
    retryMessageEnabled: Boolean,
    currentUser: User?,
    message: Message,
): Boolean = retryMessageEnabled && message.isOwnMessage(currentUser) && message.isMessageFailed()
