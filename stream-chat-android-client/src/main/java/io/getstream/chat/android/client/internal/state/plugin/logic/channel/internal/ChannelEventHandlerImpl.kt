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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

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
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageDeliveredEvent
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
import io.getstream.chat.android.client.events.UserMessagesDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.processPoll
import io.getstream.chat.android.client.extensions.internal.toMessageReminderInfo
import io.getstream.chat.android.client.internal.state.event.handler.internal.utils.toChannelUserRead
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.utils.message.isPinned
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminder
import kotlinx.coroutines.CoroutineScope

/**
 * Implementation of [ChannelEventHandler] responsible for processing channel-related events
 * and updating the local [ChannelStateImpl] accordingly.
 *
 * @param cid The channel identifier in the format "type:id".
 * @param state The [ChannelStateImpl] instance to update based on incoming events.
 * @param globalState The [MutableGlobalState] for emitting global state changes (e.g., typing events).
 * @param coroutineScope The [CoroutineScope] used for managing typing event pruning coroutines.
 * @param getCurrentUserId A function that returns the currently logged-in user's ID, or null if not logged in.
 * @param now A function that returns the current time in milliseconds. Defaults to [System.currentTimeMillis].
 *            Used for determining pinned message validity.
 */
internal class ChannelEventHandlerImpl(
    private val cid: String,
    private val state: ChannelStateImpl,
    private val globalState: MutableGlobalState,
    private val coroutineScope: CoroutineScope,
    private val getCurrentUserId: () -> String?,
    private val now: () -> Long = { System.currentTimeMillis() },
) : ChannelEventHandler {

    private val typingEventPruner = TypingEventPruner(
        channelId = cid.cidToTypeAndId().second,
        coroutineScope = coroutineScope,
    ) { _, typingEvent ->
        state.setTyping(typingEvent)
        globalState.tryEmitTypingEvent(cid, typingEvent)
    }

    /**
     * Handles the given [event] and updates the channel state accordingly.
     *
     * @param event The [ChatEvent] to handle.
     */
    override fun handle(event: ChatEvent) {
        when (event) {
            is CidEvent -> handleCidEvent(event)
            is UserPresenceChangedEvent -> state.upsertUserPresence(event.user)
            is UserUpdatedEvent -> state.upsertUserPresence(event.user)
            is MarkAllReadEvent -> state.updateRead(event.toChannelUserRead())
            is NotificationChannelMutesUpdatedEvent -> {
                val mutes = event.me.channelMutes
                val muted = mutes.any { it.channel?.cid == cid }
                state.setMuted(muted)
            }

            is UserMessagesDeletedEvent -> state.deleteMessagesFromUser(
                userId = event.user.id,
                hard = event.hardDelete,
                deletedAt = event.createdAt,
            )

            else -> Unit // Ignore other events
        }
    }

    @Suppress("LongMethod")
    private fun handleCidEvent(event: CidEvent) {
        when (event) {
            // Message events
            is NewMessageEvent -> {
                // Preserve createdLocallyAt only for messages created by current user, to ensure they are
                // sorted properly
                val preserveCreatedLocallyAt = event.message.user.id == getCurrentUserId()
                val enrichedMessage = if (preserveCreatedLocallyAt) {
                    val oldMessage = state.getMessageById(event.message.id)
                    event.message.copy(createdLocallyAt = oldMessage?.createdLocallyAt)
                } else {
                    event.message
                }
                // Update channel read state for the current user
                state.updateCurrentUserRead(event.createdAt, enrichedMessage)
                // Update hidden state if the message is not shadowed
                if (!enrichedMessage.shadowed) {
                    state.setHidden(false)
                }
                // Update message count
                event.channelMessageCount?.let(state::setMessageCount)
                // Update last message at
                state.updateLastMessageAt(enrichedMessage)
                // Insert the message into the appropriate message list
                if (state.insideSearch.value) {
                    state.upsertCachedMessage(enrichedMessage)
                } else {
                    state.upsertMessage(enrichedMessage)
                }
                // Insert pinned message if needed
                if (enrichedMessage.isPinned(now)) {
                    state.addPinnedMessage(enrichedMessage)
                }
            }

            is NotificationMessageNewEvent -> {
                // Update channel read state
                state.updateCurrentUserRead(event.createdAt, event.message)
                // Update hidden state if the message is not shadowed
                if (!event.message.shadowed) {
                    state.setHidden(false)
                }
                // Update last message at
                state.updateLastMessageAt(event.message)
                // Insert the message into the appropriate message list
                if (state.insideSearch.value) {
                    state.upsertCachedMessage(event.message)
                } else {
                    state.upsertMessage(event.message)
                }
                // Insert pinned message if needed
                if (event.message.isPinned(now)) {
                    state.addPinnedMessage(event.message)
                }
            }

            is MessageUpdatedEvent -> {
                // 1. Enrich with own_reactions (not present in the event)
                // 2. Enrich with own poll (workaround for backend not sending poll in message.updated event)
                val originalMessage = state.getMessageById(event.message.id)
                val poll = event.message.poll ?: originalMessage?.poll
                val enrichedMessage = enrichWithOwnReactions(event.message)
                    .copy(poll = poll)
                // 3. Update message in state
                state.updateMessage(enrichedMessage)
                // 4. Update Quoted Messages references
                state.updateQuotedMessageReferences(enrichedMessage)
                // 5. Handle Pinned status
                if (event.message.isPinned(now)) {
                    state.addPinnedMessage(enrichedMessage)
                } else {
                    state.deletePinnedMessage(event.message.id)
                }
            }

            is MessageDeletedEvent -> {
                if (event.hardDelete) {
                    state.deleteMessage(event.message.id)
                    state.deleteQuotedMessageReferences(event.message.id)
                } else {
                    val enrichedMessage = enrichWithOwnReactions(event.message)
                    state.updateMessage(enrichedMessage)
                    state.updateQuotedMessageReferences(enrichedMessage)
                }
                // Update message count
                event.channelMessageCount?.let(state::setMessageCount)
                // Update pinned messages
                if (event.message.pinned) {
                    state.deletePinnedMessage(event.message.id)
                }
            }

            is NotificationThreadMessageNewEvent -> {
                // Handle only if thread reply was sent to channel as well
                if (event.message.showInChannel) {
                    // Update hidden state if the message is not shadowed
                    if (!event.message.shadowed) {
                        state.setHidden(false)
                    }
                    // Insert the message into the appropriate message list
                    if (state.insideSearch.value) {
                        state.upsertCachedMessage(event.message)
                    } else {
                        state.upsertMessage(event.message)
                    }
                }
            }
            // Reaction events
            // TODO: Rework the reaction events handling logic
            is ReactionNewEvent -> updateMessage(event.message)
            is ReactionUpdateEvent -> updateMessage(event.message)
            is ReactionDeletedEvent -> updateMessage(event.message)
            // Member events
            is MemberAddedEvent -> {
                state.addMember(event.member)
                // Set the channel.membership if the current user is added to the channel
                if (event.member.getUserId() == getCurrentUserId()) {
                    state.setMembership(event.member)
                }
            }

            is MemberUpdatedEvent -> {
                state.upsertMember(event.member)
                // Update the channel.membership if the current user's member info is updated
                if (event.member.getUserId() == getCurrentUserId()) {
                    state.setMembership(event.member)
                }
            }

            is MemberRemovedEvent -> {
                state.deleteMember(event.member.getUserId())
                // Remove the channel.membership if the current user is removed from the channel
                if (event.member.getUserId() == getCurrentUserId()) {
                    state.deleteMembership()
                }
            }

            is NotificationAddedToChannelEvent -> {
                state.upsertMembers(event.channel.members)
            }

            is NotificationRemovedFromChannelEvent -> {
                state.setMemberCount(event.channel.memberCount)
                state.setMembers(event.channel.members)
                state.setWatchers(event.channel.watchers, event.channel.watcherCount)
            }
            // Watcher events
            is UserStartWatchingEvent -> state.upsertWatcher(event)
            is UserStopWatchingEvent -> state.deleteWatcher(event)
            // Channel update events
            is ChannelUpdatedEvent -> state.updateChannelData(event)
            is ChannelUpdatedByUserEvent -> state.updateChannelData(event)
            is ChannelHiddenEvent -> {
                state.setHidden(true)
                if (event.clearHistory) {
                    state.removeMessagesBefore(event.createdAt)
                }
            }

            is ChannelVisibleEvent -> state.setHidden(false)
            is ChannelDeletedEvent -> {
                state.removeMessagesBefore(event.createdAt)
                state.deleteChannel(event.createdAt)
            }

            is ChannelTruncatedEvent -> state.removeMessagesBefore(event.createdAt, event.message)
            is NotificationChannelTruncatedEvent -> state.removeMessagesBefore(event.createdAt)
            // Typing events
            is TypingStartEvent -> {
                if (event.user.id != getCurrentUserId()) {
                    typingEventPruner.processEvent(event.user.id, event)
                }
            }

            is TypingStopEvent -> {
                if (event.user.id != getCurrentUserId()) {
                    typingEventPruner.processEvent(event.user.id, null)
                }
            }
            // Read/delivery receipt events
            is MessageReadEvent -> {
                if (event.thread == null) {
                    state.updateRead(event.toChannelUserRead())
                }
            }

            is NotificationMarkReadEvent -> {
                if (event.thread == null) {
                    state.updateRead(event.toChannelUserRead())
                }
            }

            is NotificationMarkUnreadEvent -> state.updateRead(event.toChannelUserRead())
            is MessageDeliveredEvent -> state.updateDelivered(event.toChannelUserRead())
            // Invitation events
            is NotificationInviteAcceptedEvent -> {
                state.addMember(event.member)
                state.updateChannelData(event)
            }

            is NotificationInviteRejectedEvent -> {
                state.deleteMember(event.member.getUserId())
                state.updateChannelData(event)
            }
            // Ban events
            is ChannelUserBannedEvent -> {
                state.updateMemberBan(
                    memberId = event.user.id,
                    banned = true,
                    expiry = event.expiration,
                    shadow = event.shadow,
                )
            }

            is ChannelUserUnbannedEvent -> {
                state.updateMemberBan(
                    memberId = event.user.id,
                    banned = false,
                    expiry = null,
                    shadow = false,
                )
            }
            // Poll events
            is PollClosedEvent -> state.upsertPoll(event.processPoll(state::getPoll))
            is PollUpdatedEvent -> state.upsertPoll(event.processPoll(state::getPoll))
            is PollDeletedEvent -> state.deletePoll(event.poll)
            is VoteCastedEvent -> state.upsertPoll(event.processPoll(getCurrentUserId(), state::getPoll))
            is VoteChangedEvent -> state.upsertPoll(event.processPoll(getCurrentUserId(), state::getPoll))
            is VoteRemovedEvent -> state.upsertPoll(event.processPoll(state::getPoll))
            is AnswerCastedEvent -> state.upsertPoll(event.processPoll(state::getPoll))
            // Reminder events
            is ReminderCreatedEvent -> updateReminder(event.messageId, event.reminder)
            is ReminderUpdatedEvent -> updateReminder(event.messageId, event.reminder)
            is ReminderDeletedEvent -> deleteReminder(event.messageId)
            else -> Unit // Ignore other events
        }
    }

    private fun updateReminder(messageId: String, reminder: MessageReminder) {
        val message = state.getMessageById(messageId) ?: return
        val updatedMessage = message.copy(reminder = reminder.toMessageReminderInfo())
        state.updateMessage(updatedMessage)
    }

    private fun deleteReminder(messageId: String) {
        val message = state.getMessageById(messageId) ?: return
        val updatedMessage = message.copy(reminder = null)
        state.updateMessage(updatedMessage)
    }

    private fun updateMessage(message: Message) {
        val oldMessage = state.getMessageById(message.id) ?: return
        val ownReactions = oldMessage.ownReactions
        val enrichedMessage = message.copy(ownReactions = ownReactions)
        state.updateMessage(enrichedMessage)
    }

    private fun enrichWithOwnReactions(message: Message): Message {
        val oldMessage = state.getMessageById(message.id) ?: return message
        val ownReactions = oldMessage.ownReactions
        return message.copy(ownReactions = ownReactions)
    }
}
