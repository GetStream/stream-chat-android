package io.getstream.chat.android.livedata

import exhaustive
import io.getstream.chat.android.client.events.ChannelCreatedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelMuteEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUnmuteEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChannelsMuteEvent
import io.getstream.chat.android.client.events.ChannelsUnmuteEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserEvent
import io.getstream.chat.android.client.events.UserMutedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUnmutedEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.UsersMutedEvent
import io.getstream.chat.android.client.events.UsersUnmutedEvent
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.extensions.addReaction
import io.getstream.chat.android.livedata.extensions.removeReaction
import io.getstream.chat.android.livedata.extensions.setMember
import io.getstream.chat.android.livedata.extensions.updateReads
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

internal class EventHandlerImpl(
    private val domainImpl: ChatDomainImpl,
    private val runAsync: Boolean = true
) {

    fun handleEvents(events: List<ChatEvent>) {
        if (runAsync) {
            domainImpl.scope.launch(domainImpl.scope.coroutineContext) {
                handleEventsInternal(events)
            }
        } else {
            runBlocking(domainImpl.scope.coroutineContext) { handleEventsInternal(events) }
        }
    }

    internal suspend fun handleEvent(event: ChatEvent) {
        handleEventsInternal(listOf(event))
    }

    internal suspend fun updateOfflineStorageFromEvents(events: List<ChatEvent>) {
        events.sortedBy(ChatEvent::createdAt)

        val batchBuilder = EventBatchUpdate.Builder()
        batchBuilder.addToFetchChannels(events.filterIsInstance<CidEvent>().map { it.cid })

        // For some reason backend is not sending us the user instance into some events that they should
        // and we are not able to identify which event type is. Gson, because it is using reflection,
        // inject a null instance into property `user` that doesn't allow null values.
        // This is a workaround, while we identify which event type is, that omit null values without
        // break our public API
        @Suppress("USELESS_CAST")
        batchBuilder.addUsers(events.filterIsInstance<UserEvent>().mapNotNull { it.user as User? })

        // step 1. see which data we need to retrieve from offline storage
        for (event in events) {
            when (event) {
                is MessageReadEvent,
                is MemberAddedEvent,
                is MemberRemovedEvent,
                is NotificationRemovedFromChannelEvent,
                is MemberUpdatedEvent,
                is ChannelUpdatedEvent,
                is ChannelUpdatedByUserEvent,
                is ChannelDeletedEvent,
                is ChannelHiddenEvent,
                is ChannelVisibleEvent,
                is NotificationAddedToChannelEvent,
                is NotificationInvitedEvent,
                is NotificationInviteAcceptedEvent,
                is ChannelTruncatedEvent,
                is ChannelCreatedEvent,
                is HealthEvent,
                is NotificationMutesUpdatedEvent,
                is GlobalUserBannedEvent,
                is UserDeletedEvent,
                is UserMutedEvent,
                is UsersMutedEvent,
                is UserPresenceChangedEvent,
                is GlobalUserUnbannedEvent,
                is UserUnmutedEvent,
                is UsersUnmutedEvent,
                is UserUpdatedEvent,
                is NotificationChannelMutesUpdatedEvent,
                is ConnectedEvent,
                is ConnectingEvent,
                is DisconnectedEvent,
                is ErrorEvent,
                is UnknownEvent,
                is NotificationChannelDeletedEvent,
                is NotificationChannelTruncatedEvent,
                is NotificationMarkReadEvent,
                is TypingStartEvent,
                is TypingStopEvent,
                is ChannelUserBannedEvent,
                is UserStartWatchingEvent,
                is UserStopWatchingEvent,
                is ChannelUserUnbannedEvent -> Unit
                is ReactionNewEvent -> batchBuilder.addToFetchMessages(event.reaction.messageId)
                is ReactionDeletedEvent -> batchBuilder.addToFetchMessages(event.reaction.messageId)
                is ChannelMuteEvent -> batchBuilder.addToFetchChannels(event.channelMute.channel.cid)
                is ChannelsMuteEvent -> {
                    event.channelsMute.forEach { batchBuilder.addToFetchChannels(it.channel.cid) }
                }
                is ChannelUnmuteEvent -> batchBuilder.addToFetchChannels(event.channelMute.channel.cid)
                is ChannelsUnmuteEvent -> {
                    event.channelsMute.forEach { batchBuilder.addToFetchChannels(it.channel.cid) }
                }
                is MessageDeletedEvent -> batchBuilder.addToFetchMessages(event.message.id)
                is MessageUpdatedEvent -> batchBuilder.addToFetchMessages(event.message.id)
                is NewMessageEvent -> batchBuilder.addToFetchMessages(event.message.id)
                is NotificationMessageNewEvent -> batchBuilder.addToFetchMessages(event.message.id)
                is ReactionUpdateEvent -> batchBuilder.addToFetchMessages(event.message.id)
            }.exhaustive
        }
        // actually fetch the data
        val batch = batchBuilder.build(domainImpl)

        // step 2. second pass through the events, make a list of what we need to update
        loop@ for (event in events) {
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when (event) {
                // keep the data in Room updated based on the various events..
                // note that many of these events should also update user information
                is NewMessageEvent -> {
                    event.message.cid = event.cid
                    event.totalUnreadCount?.let { domainImpl.setTotalUnreadCount(it) }
                    batch.addMessageData(event.cid, event.message)
                }
                is MessageDeletedEvent -> {
                    event.message.cid = event.cid
                    batch.addMessageData(event.cid, event.message)
                }
                is MessageUpdatedEvent -> {
                    event.message.cid = event.cid
                    batch.addMessageData(event.cid, event.message)
                }
                is NotificationMessageNewEvent -> {
                    event.message.cid = event.cid
                    event.totalUnreadCount?.let { domainImpl.setTotalUnreadCount(it) }
                    batch.addMessageData(event.cid, event.message)
                }
                is NotificationAddedToChannelEvent -> {
                    batch.addChannel(event.channel)
                }
                is NotificationInvitedEvent -> {
                    batch.addUser(event.user)
                }
                is NotificationInviteAcceptedEvent -> {
                    batch.addUser(event.user)
                }
                is ChannelHiddenEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        val updatedChannel = it.apply {
                            hidden = true
                            hiddenMessagesBefore = event.createdAt.takeIf { event.clearHistory }
                        }
                        batch.addChannel(updatedChannel)
                    }
                }
                is ChannelVisibleEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { hidden = false })
                    }
                }
                is NotificationMutesUpdatedEvent -> {
                    domainImpl.updateCurrentUser(event.me)
                }
                is ConnectedEvent -> {
                    domainImpl.updateCurrentUser(event.me)
                }

                is ReactionNewEvent -> {
                    // get the message, update the reaction data, update the message
                    // note that we need to use event.reaction and not event.message
                    // event.message only has a subset of reactions
                    batch.getCurrentMessage(event.reaction.messageId)?.let {
                        val updatedMessage = it.apply {
                            addReaction(event.reaction, domainImpl.currentUser.id == event.user.id)
                        }
                        batch.addMessage(updatedMessage)
                    }
                }
                is ReactionDeletedEvent -> {
                    // get the message, update the reaction data, update the message
                    batch.getCurrentMessage(event.reaction.messageId)?.also {
                        val updatedMessage = it.copy(reactionCounts = event.message.reactionCounts)
                            .apply { removeReaction(event.reaction, false) }
                        batch.addMessage(updatedMessage)
                    }
                }
                is ReactionUpdateEvent -> {
                    batch.getCurrentMessage(event.reaction.messageId)?.let {
                        val updatedMessage = it.apply {
                            addReaction(event.reaction, domainImpl.currentUser.id == event.user.id)
                        }
                        batch.addMessage(updatedMessage)
                    }
                }
                is MemberAddedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { setMember(event.member.user.id, event.member) })
                    }
                }
                is MemberUpdatedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { setMember(event.member.user.id, event.member) })
                    }
                }
                is MemberRemovedEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { setMember(event.user.id, null) })
                    }
                }
                is NotificationRemovedFromChannelEvent -> {
                    batch.getCurrentChannel(event.cid)?.let {
                        batch.addChannel(it.apply { setMember(event.user.id, null) })
                    }
                }
                is ChannelUpdatedEvent -> {
                    batch.addChannel(event.channel)
                }
                is ChannelUpdatedByUserEvent -> {
                    batch.addChannel(event.channel)
                }
                is ChannelDeletedEvent -> {
                    batch.addChannel(event.channel)
                }
                is ChannelCreatedEvent -> {
                    batch.addChannel(event.channel)
                }
                is ChannelMuteEvent -> {
                    batch.addChannel(event.channelMute.channel)
                }
                is ChannelsMuteEvent -> {
                    event.channelsMute.forEach {
                        batch.addChannel(it.channel)
                    }
                }
                is ChannelUnmuteEvent -> {
                    batch.addChannel(event.channelMute.channel)
                }
                is ChannelsUnmuteEvent -> {
                    event.channelsMute.forEach {
                        batch.addChannel(it.channel)
                    }
                }
                is ChannelTruncatedEvent -> {
                    batch.addChannel(event.channel)
                }
                is NotificationChannelDeletedEvent -> {
                    batch.addChannel(event.channel)
                    // note that NotificationChannelDeletedEvent doesn't implement UserEvent
                    event.user?.let {
                        batch.addUser(it)
                    }
                }
                is NotificationChannelMutesUpdatedEvent -> {
                    domainImpl.updateCurrentUser(event.me)
                }
                is NotificationChannelTruncatedEvent -> {
                    batch.addChannel(event.channel)
                }

                is MessageReadEvent -> {
                    // get the channel, update reads, write the channel
                    batch.getCurrentChannel(event.cid)?.let {
                        val updatedChannel = it.apply {
                            updateReads(ChannelUserRead(user = event.user, lastRead = event.createdAt))
                        }
                        batch.addChannel(updatedChannel)
                    }
                }
                is NotificationMarkReadEvent -> {
                    event.totalUnreadCount?.let { domainImpl.setTotalUnreadCount(it) }

                    batch.getCurrentChannel(event.cid)?.let {
                        val updatedChannel = it.apply {
                            updateReads(ChannelUserRead(user = event.user, lastRead = event.createdAt))
                        }
                        batch.addChannel(updatedChannel)
                    }
                }
                is UserMutedEvent -> {
                    batch.addUser(event.targetUser)
                }
                is UsersMutedEvent -> {
                    batch.addUsers(event.targetUsers)
                }
                is UserUnmutedEvent -> {
                    batch.addUser(event.targetUser)
                }
                is UsersUnmutedEvent -> {
                    batch.addUsers(event.targetUsers)
                }
                is GlobalUserBannedEvent -> {
                    batch.addUser(event.user.apply { banned = true })
                }
                is GlobalUserUnbannedEvent -> {
                    batch.addUser(event.user.apply { banned = false })
                }
                is TypingStartEvent,
                is TypingStopEvent,
                is HealthEvent,
                is ConnectingEvent,
                is DisconnectedEvent,
                is ErrorEvent,
                is UnknownEvent,
                is ChannelUserBannedEvent,
                is ChannelUserUnbannedEvent,
                is UserUpdatedEvent,
                is UserDeletedEvent,
                is UserPresenceChangedEvent,
                is UserStartWatchingEvent,
                is UserStopWatchingEvent -> Unit
            }.exhaustive
        }

        // execute the batch
        batch.execute()

        // handle delete and truncate events
        for (event in events) {
            when (event) {
                is NotificationChannelTruncatedEvent -> {
                    domainImpl.repos.messages.deleteChannelMessagesBefore(event.cid, event.createdAt)
                }
                is ChannelTruncatedEvent -> {
                    domainImpl.repos.messages.deleteChannelMessagesBefore(event.cid, event.createdAt)
                }
                is ChannelDeletedEvent -> {
                    domainImpl.repos.messages.deleteChannelMessagesBefore(event.cid, event.createdAt)
                    domainImpl.repos.channels.select(event.cid)?.let {
                        domainImpl.repos.channels.insert(it.apply { deletedAt = event.createdAt })
                    }
                }
            }
        }
    }

    private suspend fun handleEventsInternal(events: List<ChatEvent>) {
        events.sortedBy { it.createdAt }
        updateOfflineStorageFromEvents(events)

        // step 3 - forward the events to the active channels

        events.filterIsInstance<CidEvent>()
            .groupBy { it.cid }
            .filterNot { it.key.isBlank() }
            .forEach {
                val (cid, eventList) = it
                if (domainImpl.isActiveChannel(cid)) {
                    domainImpl.channel(cid).handleEvents(eventList)
                }
            }

        // only afterwards forward to the queryRepo since it borrows some data from the channel
        // queryRepo mainly monitors for the notification added to channel event
        for (queryRepo in domainImpl.getActiveQueries()) {
            queryRepo.handleEvents(events)
        }

        // send out the connect events
        for (event in events) {

            // connection events are never send on the recovery endpoint, so handle them 1 by 1
            when (event) {
                is DisconnectedEvent -> {
                    domainImpl.postOffline()
                }
                is ConnectedEvent -> {
                    val recovered = domainImpl.isInitialized()

                    domainImpl.postOnline()
                    domainImpl.postInitialized()
                    if (recovered && domainImpl.recoveryEnabled) {
                        domainImpl.connectionRecovered(true)
                    } else {
                        domainImpl.connectionRecovered(false)
                    }
                }
            }
        }
    }
}
