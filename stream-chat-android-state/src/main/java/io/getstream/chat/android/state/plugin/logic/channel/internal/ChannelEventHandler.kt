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
import io.getstream.chat.android.state.plugin.state.channel.internal.ChannelMutableState

/**
 * Class responsible for updating the local database based on channel-related events.
 *
 * @param cid The channel identifier.
 * @param stateLogic The [ChannelStateLogic] instance used to update the channel state.
 * @param getCurrentUserId Returns the currently logged in user ID.
 */
internal class ChannelEventHandler(
    private val cid: String,
    private val stateLogic: ChannelStateLogic,
    private val getCurrentUserId: () -> String?,
) {

    private val mutableState: ChannelMutableState
        get() = stateLogic.writeChannelState()

    /**
     * Handles the given [event] and updates the channel state accordingly.
     *
     * @param event The [ChatEvent] to handle.
     */
    fun handle(event: ChatEvent) {
        when (event) {
            is CidEvent -> handleCidEvent(event)
            is UserPresenceChangedEvent -> stateLogic.upsertUserPresence(event.user)
            is UserUpdatedEvent -> stateLogic.upsertUserPresence(event.user)
            is MarkAllReadEvent -> stateLogic.updateRead(event.toChannelUserRead())
            is NotificationChannelMutesUpdatedEvent -> {
                val mutes = event.me.channelMutes
                val isMuted = mutes.any { it.channel?.cid == cid }
                stateLogic.updateMute(isMuted)
            }

            is UserMessagesDeletedEvent -> stateLogic.deleteMessagesFromUser(
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
                stateLogic.updateCurrentUserRead(event.createdAt, event.message)
                // Update hidden state if the message is not shadowed
                if (!event.message.shadowed) {
                    stateLogic.setHidden(false)
                }
                // Update message count
                event.channelMessageCount?.let(stateLogic::updateMessageCount)
            }

            is NotificationMessageNewEvent -> {
                if (!mutableState.insideSearch.value) {
                    upsertMessage(event.message)
                }
                // Update channel read state
                stateLogic.updateCurrentUserRead(event.createdAt, event.message)
                // Update hidden state if the message is not shadowed
                if (!event.message.shadowed) {
                    stateLogic.setHidden(false)
                }
            }

            is MessageUpdatedEvent -> {
                val originalMessage = mutableState.getMessageById(event.message.id)
                // Enrich the poll as it might not be present in the event
                val poll = event.message.poll ?: originalMessage?.poll
                // Enrich the reply message (if present)
                val replyTo = event.message.replyMessageId
                    ?.let { mutableState.getMessageById(it) }
                    ?: event.message.replyTo
                val enrichedMessage = event.message.copy(
                    poll = poll,
                    replyTo = replyTo,
                )
                updateMessage(enrichedMessage)
                // Update the pinned messages state if the pinned status changed
                stateLogic.delsertPinnedMessage(enrichedMessage)
            }

            is MessageDeletedEvent -> {
                if (event.hardDelete) {
                    stateLogic.deleteMessage(event.message)
                } else {
                    updateMessage(event.message)
                }
                // Update message count
                event.channelMessageCount?.let(stateLogic::updateMessageCount)
            }

            is NotificationThreadMessageNewEvent -> {
                // Handle only if thread reply was sent to channel as well
                if (event.message.showInChannel) {
                    upsertMessage(event.message)
                    // Update hidden state if the message is not shadowed
                    if (!event.message.shadowed) {
                        stateLogic.setHidden(false)
                    }
                }
            }
            // Reaction events
            is ReactionNewEvent -> updateMessage(event.message)
            is ReactionUpdateEvent -> updateMessage(event.message)
            is ReactionDeletedEvent -> updateMessage(event.message)
            // Member events
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
            // Watcher events
            is UserStartWatchingEvent -> stateLogic.upsertWatcher(event)
            is UserStopWatchingEvent -> stateLogic.deleteWatcher(event)
            // Channel update events
            is ChannelUpdatedEvent -> stateLogic.updateChannelData(event)
            is ChannelUpdatedByUserEvent -> stateLogic.updateChannelData(event)
            is ChannelHiddenEvent -> {
                stateLogic.setHidden(true)
                if (event.clearHistory) {
                    stateLogic.removeMessagesBefore(event.createdAt)
                }
            }

            is ChannelVisibleEvent -> stateLogic.setHidden(false)
            is ChannelDeletedEvent -> {
                stateLogic.removeMessagesBefore(event.createdAt)
                stateLogic.deleteChannel(event.createdAt)
            }

            is ChannelTruncatedEvent -> stateLogic.removeMessagesBefore(event.createdAt, event.message)
            is NotificationChannelTruncatedEvent -> stateLogic.removeMessagesBefore(event.createdAt)
            // Typing events
            is TypingStartEvent -> stateLogic.setTyping(event.user.id, event)
            is TypingStopEvent -> stateLogic.setTyping(event.user.id, null)
            // Read/delivery receipt events
            is MessageReadEvent -> {
                if (event.thread == null) {
                    stateLogic.updateRead(event.toChannelUserRead())
                }
            }

            is NotificationMarkReadEvent -> {
                if (event.thread == null) {
                    stateLogic.updateRead(event.toChannelUserRead())
                }
            }
            is NotificationMarkUnreadEvent -> stateLogic.updateRead(event.toChannelUserRead())
            is MessageDeliveredEvent -> stateLogic.updateDelivered(event.toChannelUserRead())
            // Invitation events
            is NotificationInviteAcceptedEvent -> {
                stateLogic.addMember(event.member)
                stateLogic.updateChannelData(event)
            }

            is NotificationInviteRejectedEvent -> {
                stateLogic.deleteMember(event.member)
                stateLogic.updateChannelData(event)
            }
            // Ban events
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
            // Poll events
            is PollClosedEvent -> stateLogic.upsertPoll(event.processPoll(stateLogic::getPoll))
            is PollUpdatedEvent -> stateLogic.upsertPoll(event.processPoll(stateLogic::getPoll))
            is PollDeletedEvent -> stateLogic.deletePoll(event.poll)
            is VoteCastedEvent -> stateLogic.upsertPoll(event.processPoll(getCurrentUserId(), stateLogic::getPoll))
            is VoteChangedEvent -> stateLogic.upsertPoll(event.processPoll(getCurrentUserId(), stateLogic::getPoll))
            is VoteRemovedEvent -> stateLogic.upsertPoll(event.processPoll(stateLogic::getPoll))
            is AnswerCastedEvent -> stateLogic.upsertPoll(event.processPoll(stateLogic::getPoll))
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
        stateLogic.upsertMessage(updatedMessage)
        stateLogic.delsertPinnedMessage(updatedMessage)
    }

    private fun updateMessage(message: Message) {
        val oldMessage = getMessage(message.id) ?: return
        val ownReactions = oldMessage.ownReactions
        val enrichedMessage = message.copy(ownReactions = ownReactions)
        stateLogic.updateMessage(enrichedMessage)
    }

    private fun getMessage(id: String): Message? {
        return mutableState.visibleMessages.value[id]?.copy()
    }

    private fun updateReminder(messageId: String, reminder: MessageReminder) {
        // Update reminder only if message exists
        val message = mutableState.getMessageById(messageId) ?: return
        updateMessage(message.copy(reminder = reminder.toMessageReminderInfo()))
    }

    private fun deleteReminder(messageId: String) {
        val message = mutableState.getMessageById(messageId) ?: return
        updateMessage(message.copy(reminder = null))
    }
}
