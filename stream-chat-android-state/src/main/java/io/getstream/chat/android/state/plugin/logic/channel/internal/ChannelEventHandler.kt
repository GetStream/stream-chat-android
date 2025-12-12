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
import io.getstream.chat.android.client.extensions.internal.processPoll
import io.getstream.chat.android.client.extensions.internal.toMessageReminderInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.state.event.handler.internal.utils.toChannelUserRead
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelStateUpdates

/**
 * Class responsible for updating the local database based on channel-related events.
 *
 * @param cid The channel identifier.
 * @param state The [ChannelStateUpdates] instance used to update the channel state.
 * @param getCurrentUserId Returns the currently logged in user ID.
 */
internal class ChannelEventHandler(
    private val cid: String,
    private val state: ChannelStateUpdates,
    private val getCurrentUserId: () -> String?,
) {

    /**
     * Handles the given [event] and updates the channel state accordingly.
     *
     * @param event The [ChatEvent] to handle.
     */
    fun handle(event: ChatEvent) {
        when (event) {
            is CidEvent -> handleCidEvent(event)
            is UserPresenceChangedEvent -> state.upsertUserPresence(event.user)
            is UserUpdatedEvent -> state.upsertUserPresence(event.user)
            is MarkAllReadEvent -> state.updateRead(event.toChannelUserRead())
            is NotificationChannelMutesUpdatedEvent -> {
                val mutes = event.me.channelMutes
                val isMuted = mutes.any { it.channel?.cid == cid }
                state.setMuted(isMuted)
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
                upsertMessage(event.message, preserveCreatedLocallyAt)
                // Update channel read state
                state.updateCurrentUserRead(event.createdAt, event.message)
                // Update hidden state if the message is not shadowed
                if (!event.message.shadowed) {
                    state.setHidden(false)
                }
                // Update message count
                event.channelMessageCount?.let(state::setMessageCount)
            }

            is NotificationMessageNewEvent -> {
                upsertMessage(event.message)
                // Update channel read state
                state.updateCurrentUserRead(event.createdAt, event.message)
                // Update hidden state if the message is not shadowed
                if (!event.message.shadowed) {
                    state.setHidden(false)
                }
            }

            is MessageUpdatedEvent -> {
                val originalMessage = state.getMessageById(event.message.id)
                // Enrich the poll as it might not be present in the event
                val poll = event.message.poll ?: originalMessage?.poll
                // Enrich the reply message (if present)
                val replyTo = event.message.replyMessageId
                    ?.let { state.getMessageById(it) }
                    ?: event.message.replyTo
                val enrichedMessage = event.message.copy(
                    poll = poll,
                    replyTo = replyTo,
                )
                updateMessage(enrichedMessage)
            }

            is MessageDeletedEvent -> {
                if (event.hardDelete) {
                    state.deleteMessage(event.message)
                } else {
                    updateMessage(event.message)
                }
                // Update message count
                event.channelMessageCount?.let(state::setMessageCount)
            }

            is NotificationThreadMessageNewEvent -> {
                // Handle only if thread reply was sent to channel as well
                if (event.message.showInChannel) {
                    upsertMessage(event.message)
                    // Update hidden state if the message is not shadowed
                    if (!event.message.shadowed) {
                        state.setHidden(false)
                    }
                }
            }
            // Reaction events
            is ReactionNewEvent -> updateMessage(event.message)
            is ReactionUpdateEvent -> updateMessage(event.message)
            is ReactionDeletedEvent -> updateMessage(event.message)
            // Member events
            is MemberAddedEvent -> {
                state.addMember(event.member)
                // Set the channel.membership if the current user is added to the channel
                if (event.member.getUserId() == getCurrentUserId()) {
                    state.addMembership(event.member)
                }
            }

            is MemberRemovedEvent -> {
                state.deleteMember(event.member)
                // Remove the channel.membership if the current user is removed from the channel
                if (event.member.getUserId() == getCurrentUserId()) {
                    state.removeMembership()
                }
            }

            is MemberUpdatedEvent -> {
                state.upsertMember(event.member)
                state.updateMembership(event.member)
            }

            is NotificationAddedToChannelEvent -> {
                state.upsertMembers(event.channel.members)
            }

            is NotificationRemovedFromChannelEvent -> {
                state.setMembers(event.channel.members, event.channel.memberCount)
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
            is TypingStartEvent -> state.setTyping(event.user.id, event)
            is TypingStopEvent -> state.setTyping(event.user.id, null)
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
                state.deleteMember(event.member)
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

    private fun upsertMessage(
        message: Message,
        preserveCreatedLocallyAt: Boolean = false,
    ) {
        val oldMessage = getMessage(message.id)
        val createdLocallyAt = if (preserveCreatedLocallyAt) {
            oldMessage?.createdLocallyAt
        } else {
            message.createdLocallyAt
        }
        val ownReactions = oldMessage?.ownReactions ?: message.ownReactions
        val updatedMessage = message.copy(createdLocallyAt = createdLocallyAt, ownReactions = ownReactions)
        state.upsertMessage(updatedMessage)
        state.delsertPinnedMessage(updatedMessage)
    }

    private fun updateMessage(message: Message) {
        val oldMessage = getMessage(message.id) ?: return
        val ownReactions = oldMessage.ownReactions
        val enrichedMessage = message.copy(ownReactions = ownReactions)
        state.updateMessage(enrichedMessage)
    }

    private fun getMessage(id: String): Message? {
        return state.visibleMessages.value[id]?.copy()
    }

    private fun updateReminder(messageId: String, reminder: MessageReminder) {
        // Update reminder only if message exists
        val message = state.getMessageById(messageId) ?: return
        updateMessage(message.copy(reminder = reminder.toMessageReminderInfo()))
    }

    private fun deleteReminder(messageId: String) {
        val message = state.getMessageById(messageId) ?: return
        updateMessage(message.copy(reminder = null))
    }
}
