package io.getstream.chat.android.ui.feature.messages.list.internal

import io.getstream.chat.android.client.utils.attachment.isGiphy
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.feature.messages.list.MessageListViewStyle
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

internal fun MessageListViewStyle.canReplyToMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = replyEnabled && message.isSynced() && ownCapabilities.contains(ChannelCapabilities.QUOTE_MESSAGE)

internal fun MessageListViewStyle.canThreadReplyToMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = threadsEnabled && message.isSynced() && ownCapabilities.contains(ChannelCapabilities.QUOTE_MESSAGE)

internal fun MessageListViewStyle.canCopyMessage(
    message: Message,
): Boolean = copyTextEnabled && (message.isTextOnly() || message.hasLinks())

internal fun MessageListViewStyle.canEditMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = editMessageEnabled &&
    with(ownCapabilities) { ((message.isOwnMessage(currentUser) && canEditOwnMessage()) || canEditAnyMessage()) } &&
    !message.isGiphyCommand()

internal fun MessageListViewStyle.canDeleteMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = deleteMessageEnabled &&
    with(ownCapabilities) { ((message.isOwnMessage(currentUser) && canDeleteOwnMessage()) || canDeleteAnyMessage()) }

internal fun MessageListViewStyle.canFlagMessage(
    currentUser: User?,
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = flagEnabled && ownCapabilities.canFlagMessage() && !message.isOwnMessage(currentUser)

internal fun MessageListViewStyle.canPinMessage(
    message: Message,
    ownCapabilities: Set<String>,
): Boolean = pinMessageEnabled && message.isSynced() && ownCapabilities.canPinMessage()

internal fun MessageListViewStyle.canBlockUser(
    currentUser: User?,
    message: Message,
): Boolean = blockUserEnabled && !message.isOwnMessage(currentUser)

internal fun MessageListViewStyle.canMarkAsUnread(
    ownCapabilities: Set<String>,
): Boolean = markAsUnreadEnabled && ownCapabilities.canMarkAsUnread()

internal fun MessageListViewStyle.canRetryMessage(
    currentUser: User?,
    message: Message,
): Boolean = retryMessageEnabled && message.isOwnMessage(currentUser) && message.isMessageFailed()