/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest
import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.NotificationThreadMessageNewEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.ReminderCreatedEvent
import io.getstream.chat.android.client.events.ReminderDeletedEvent
import io.getstream.chat.android.client.events.ReminderUpdatedEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.extensions.internal.processPoll
import io.getstream.chat.android.client.extensions.internal.toMessageReminderInfo
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.event.handler.internal.utils.toChannelUserRead
import io.getstream.chat.android.state.model.querychannels.pagination.internal.QueryChannelPaginationRequest
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import java.util.Date

@Suppress("TooManyFunctions")
internal class LivestreamChannelLogic(
    private val channelType: String,
    private val channelId: String,
    private val userPresence: Boolean,
    private val stateLogic: ChannelStateLogic,
    private val getCurrentUserId: () -> String?,
    private val messageLimit: Int,
) : ChannelLogic {

    private val logger by taggedLogger("Chat:LivestreamChannelLogic")
    private val mutableState: ChannelMutableState = stateLogic.writeChannelState()

    private var enableMessageNewEvents: Boolean = true

    override val cid: String
        get() = "$channelType:$channelId"

    override fun stateLogic(): ChannelStateLogic {
        return stateLogic
    }

    override suspend fun updateStateFromDatabase(request: QueryChannelRequest) {
        logger.d { "[updateStateFromDatabase] request: $request" }
        // if (request.isNotificationUpdate) return
        // stateLogic.refreshMuteState()
    }

    override suspend fun onQueryChannelResult(request: QueryChannelRequest, result: Result<Channel>) {
        logger.d { "[onQueryChannelResult]" }
        when (result) {
            is Result.Success -> handleQueryChannelSuccess(request, result.value)
            is Result.Failure -> stateLogic.propagateQueryError(result.value)
        }
    }

    override suspend fun watch(userPresence: Boolean, limit: Int): Result<Channel> {
        logger.d { "[watch] userPresence: $userPresence, limit: $limit" }
        if (stateLogic.writeChannelState().loading.value) {
            return Result.Failure(
                Error.GenericError(
                    "Another request to watch this channel is in progress. Ignoring this request.",
                ),
            )
        }
        val request = QueryChannelPaginationRequest(limit)
            .toWatchChannelRequest(userPresence)
            .apply { shouldRefresh = true }
        return queryChannel(request)
    }

    override suspend fun loadNewerMessages(messageId: String, limit: Int): Result<Channel> {
        logger.d { "[loadNewerMessages] messageId: $messageId, limit: $limit" }
        stateLogic.loadingNewerMessages()
        return queryChannel(newerWatchChannelRequest(messageId, limit))
    }

    override suspend fun loadOlderMessages(messageId: String?, limit: Int): Result<Channel> {
        logger.d { "[loadOlderMessages] messageId: $messageId, limit: $limit" }
        stateLogic.loadingOlderMessages()
        val actualMessageId = if (messageId != null) {
            messageId
        } else {
            // If no messageId is provided, we can use the last message in the channel
            mutableState.sortedMessages.value.first().id
        }
        return queryChannel(olderWatchChannelRequest(actualMessageId, limit))
    }

    override suspend fun loadMessagesAroundId(messageId: String): Result<Channel> {
        logger.d { "[loadMessagesAroundId] messageId: $messageId" }
        return queryChannel(aroundIdWatchChannelRequest(messageId))
    }

    override fun deleteMessage(message: Message) {
        logger.d { "[deleteMessage] message.id: ${message.id}, message.text: ${message.text}" }
        stateLogic.deleteMessage(message)
    }

    override fun upsertMessage(message: Message) {
        logger.d { "[upsertMessage] message.id: ${message.id}, message.text: ${message.text}" }
        if (enableMessageNewEvents) {
            logger.d { "[upsertMessage] Adding: ${message.text}" }
            stateLogic.upsertMessage(message)
            stateLogic.trimOldMessagesIfNeeded(keep = messageLimit)
        } else {
            logger.d { "[upsertMessage] Skipping: ${message.text}" }
        }
    }

    override fun getMessage(messageId: String): Message? =
        mutableState.visibleMessages.value[messageId]?.copy()

    @Suppress("LongMethod")
    override fun handleEvent(event: ChatEvent) {
        when (event) {
            is NewMessageEvent -> {
                if (enableMessageNewEvents) {
                    logger.d { "[handleEvent] adding message.new " }
                    upsertEventMessage(event.message)
                    stateLogic.trimOldMessagesIfNeeded(keep = messageLimit)
                    stateLogic.updateCurrentUserRead(event.createdAt, event.message)
                    stateLogic.takeUnless { event.message.shadowed }?.toggleHidden(false)
                } else {
                    logger.d { "[handleEvent] skipping message.new: ${event.message.text}" }
                }
            }
            is MessageUpdatedEvent -> {
                event.message.copy(
                    replyTo = event.message.replyMessageId
                        ?.let { mutableState.getMessageById(it) }
                        ?: event.message.replyTo,
                ).let(::upsertEventMessage)
                stateLogic.toggleHidden(false)
            }
            is MessageDeletedEvent -> {
                if (event.hardDelete) {
                    deleteMessage(event.message)
                } else {
                    upsertEventMessage(event.message)
                }
                stateLogic.toggleHidden(false)
            }
            is ReactionNewEvent -> upsertEventMessage(event.message)
            is ReactionUpdateEvent -> upsertEventMessage(event.message)
            is ReactionDeletedEvent -> upsertEventMessage(event.message)
            is ChannelUpdatedEvent -> stateLogic.updateChannelData(event)

            is NotificationMessageNewEvent -> {
                if (enableMessageNewEvents) {
                    if (!mutableState.insideSearch.value) {
                        logger.d { "[handleEvent] adding notification.message_new " }
                        upsertEventMessage(event.message)
                        stateLogic.trimOldMessagesIfNeeded(keep = messageLimit)
                    }
                    stateLogic.updateCurrentUserRead(event.createdAt, event.message)
                    stateLogic.toggleHidden(false)
                } else {
                    logger.d { "[handleEvent] skipping notification.message_new " }
                }
            }
            is NotificationThreadMessageNewEvent -> upsertEventMessage(event.message)
            is MemberAddedEvent -> {
                stateLogic.addMember(event.member)
                // Set the channel.membership if the current user is added to the channel
                if (event.member.getUserId() == getCurrentUserId()) {
                    stateLogic.addMembership(event.member)
                }
            }
            is MemberRemovedEvent -> {
                stateLogic.deleteMember(event.member)
                // Remove the channel.membership if the current user is removed from the channel
                if (event.member.getUserId() == getCurrentUserId()) {
                    stateLogic.removeMembership()
                }
            }
            is MemberUpdatedEvent -> {
                stateLogic.upsertMember(event.member)
                stateLogic.updateMembership(event.member)
            }
            is NotificationAddedToChannelEvent -> {
                stateLogic.upsertMembers(event.channel.members)
            }
            is NotificationRemovedFromChannelEvent -> {
                stateLogic.setMembers(event.channel.members, event.channel.memberCount)
                stateLogic.setWatchers(event.channel.watchers, event.channel.watcherCount)
            }
            is UserStartWatchingEvent -> stateLogic.upsertWatcher(event)
            is UserStopWatchingEvent -> stateLogic.deleteWatcher(event)
            is ChannelUpdatedByUserEvent -> stateLogic.updateChannelData(event)
            is ChannelHiddenEvent -> {
                stateLogic.toggleHidden(true)
                if (event.clearHistory) {
                    removeMessagesBefore(event.createdAt)
                }
            }
            is ChannelVisibleEvent -> stateLogic.toggleHidden(false)
            is ChannelDeletedEvent -> {
                removeMessagesBefore(event.createdAt)
                stateLogic.deleteChannel(event.createdAt)
            }
            is ChannelTruncatedEvent -> {
                removeMessagesBefore(event.createdAt, event.message)
            }
            is NotificationChannelTruncatedEvent -> removeMessagesBefore(event.createdAt)
            is TypingStartEvent -> stateLogic.setTyping(event.user.id, event)
            is TypingStopEvent -> stateLogic.setTyping(event.user.id, null)
            is MessageReadEvent -> if (event.thread == null) {
                stateLogic.updateRead(event.toChannelUserRead())
            }
            is NotificationMarkReadEvent -> if (event.thread == null) {
                stateLogic.updateRead(event.toChannelUserRead())
            }
            is NotificationMarkUnreadEvent -> {
                stateLogic.updateRead(event.toChannelUserRead())
            }
            is NotificationInviteAcceptedEvent -> {
                stateLogic.addMember(event.member)
                stateLogic.updateChannelData(event)
            }
            is NotificationInviteRejectedEvent -> {
                stateLogic.deleteMember(event.member)
                stateLogic.updateChannelData(event)
            }
            is ChannelUserBannedEvent -> {
                stateLogic.updateMemberBanned(
                    memberUserId = event.user.id,
                    banned = true,
                    banExpires = event.expiration,
                    shadow = event.shadow,
                )
            }
            is ChannelUserUnbannedEvent -> {
                stateLogic.updateMemberBanned(
                    memberUserId = event.user.id,
                    banned = false,
                    banExpires = null,
                    shadow = false,
                )
            }
            is PollClosedEvent -> stateLogic.upsertPoll(event.processPoll(stateLogic::getPoll))
            is PollUpdatedEvent -> stateLogic.upsertPoll(event.processPoll(stateLogic::getPoll))
            is PollDeletedEvent -> stateLogic.deletePoll(event.poll)
            is VoteCastedEvent ->
                stateLogic.upsertPoll(event.processPoll(getCurrentUserId(), stateLogic::getPoll))
            is VoteChangedEvent ->
                stateLogic.upsertPoll(event.processPoll(getCurrentUserId(), stateLogic::getPoll))
            is VoteRemovedEvent -> stateLogic.upsertPoll(event.processPoll(stateLogic::getPoll))
            is AnswerCastedEvent -> stateLogic.upsertPoll(event.processPoll(stateLogic::getPoll))
            is ReminderCreatedEvent -> upsertReminder(event.messageId, event.reminder)
            is ReminderUpdatedEvent -> upsertReminder(event.messageId, event.reminder)
            is ReminderDeletedEvent -> deleteReminder(event.messageId)
            is UserPresenceChangedEvent -> upsertUserPresence(event.user)
            is UserUpdatedEvent -> upsertUser(event.user)
            is MarkAllReadEvent -> stateLogic.updateRead(event.toChannelUserRead())
            is NotificationChannelMutesUpdatedEvent -> event.me.channelMutes.any { mute ->
                mute.channel?.cid == mutableState.cid
            }.let(stateLogic::updateMute)
            else -> Unit
        }
    }

    override fun updateDataForChannel(
        channel: Channel,
        messageLimit: Int,
        shouldRefreshMessages: Boolean,
        scrollUpdate: Boolean,
        isNotificationUpdate: Boolean,
        isChannelsStateUpdate: Boolean,
    ) {
        stateLogic.updateDataForChannel(
            channel,
            messageLimit,
            shouldRefreshMessages,
            scrollUpdate,
            isNotificationUpdate,
            isChannelsStateUpdate = isChannelsStateUpdate,
        )
    }

    private fun newerWatchChannelRequest(messageId: String, limit: Int): WatchChannelRequest {
        return watchChannelRequest(messageId, limit, Pagination.GREATER_THAN)
    }

    private fun olderWatchChannelRequest(messageId: String, limit: Int): WatchChannelRequest {
        return watchChannelRequest(messageId, limit, Pagination.LESS_THAN)
    }

    private fun aroundIdWatchChannelRequest(messageId: String): WatchChannelRequest {
        return QueryChannelPaginationRequest()
            .apply {
                messageFilterDirection = Pagination.AROUND_ID
                messageFilterValue = messageId
            }
            .toWatchChannelRequest(userPresence)
            .apply { shouldRefresh = true }
    }

    private fun watchChannelRequest(messageId: String, limit: Int, pagination: Pagination): WatchChannelRequest {
        return QueryChannelPaginationRequest(limit)
            .apply {
                messageFilterDirection = pagination
                messageFilterValue = messageId
            }
            .toWatchChannelRequest(userPresence)
    }

    private suspend fun queryChannel(request: WatchChannelRequest): Result<Channel> {
        return ChatClient.instance()
            .queryChannel(channelType, channelId, request)
            .await()
    }

    private fun handleQueryChannelSuccess(request: QueryChannelRequest, channel: Channel) {
        val isEnd = request.messagesLimit() > channel.messages.size
        val pagination = request.pagination()?.first
        when (pagination) {
            Pagination.LESS_THAN, Pagination.LESS_THAN_OR_EQUAL -> {
                // Older messages
                logger.d {
                    "[handleQueryChannelSuccess] Adding older messages: ${channel.messages.size}, isEnd: $isEnd"
                }
                stateLogic.addOlderMessages(channel.messages, isEnd)
                stateLogic.trimNewMessagesIfNeeded(keep = messageLimit)
            }

            Pagination.GREATER_THAN, Pagination.GREATER_THAN_OR_EQUAL -> {
                // Newer messages
                logger.d {
                    "[handleQueryChannelSuccess] Adding newer messages: ${channel.messages.size}, isEnd: $isEnd"
                }
                stateLogic.addNewerMessages(channel.messages, isEnd)
                stateLogic.trimOldMessagesIfNeeded(keep = messageLimit)
            }

            Pagination.AROUND_ID -> {
                // Around ID messages
                logger.d {
                    "[handleQueryChannelSuccess] Replacing messages around ID: " +
                        "${channel.messages.size}, isEnd: $isEnd"
                }
                stateLogic.replaceMessages(channel.messages, loadedAroundId = true, isEnd = isEnd)
            }

            null -> {
                // No pagination
                logger.d {
                    "[handleQueryChannelSuccess] Replacing messages without pagination: " +
                        "${channel.messages.size}, isEnd: $isEnd"
                }
                stateLogic.replaceMessages(channel.messages, loadedAroundId = false, isEnd = isEnd)
            }
        }
        // Handle enable/disable of "message.new" events based on whether the newest message is loaded
        enableMessageNewEvents = mutableState.endOfNewerMessages.value
    }

    private fun upsertEventMessage(message: Message) {
        val ownReactions = getMessage(message.id)?.ownReactions ?: message.ownReactions
        stateLogic.upsertMessage(message.copy(ownReactions = ownReactions))
        stateLogic.delsertPinnedMessage(message.copy(ownReactions = ownReactions))
    }

    private fun removeMessagesBefore(date: Date, systemMessage: Message? = null) {
        stateLogic.removeMessagesBefore(date, systemMessage)
    }

    private fun upsertReminder(messageId: String, reminder: MessageReminder) {
        val message = reminder.message ?: mutableState.getMessageById(messageId) ?: return
        upsertEventMessage(message.copy(reminder = reminder.toMessageReminderInfo()))
    }

    private fun deleteReminder(messageId: String) {
        val message = mutableState.getMessageById(messageId) ?: return
        upsertEventMessage(message.copy(reminder = null))
    }

    private fun upsertUserPresence(user: User) {
        stateLogic.upsertUserPresence(user)
    }

    private fun upsertUser(user: User) {
        upsertUserPresence(user)
    }
}
