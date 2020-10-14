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
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.extensions.users
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EventHandlerImpl(
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

        val users: MutableMap<String, User> = mutableMapOf()
        val channels: MutableMap<String, ChannelEntity> = mutableMapOf()

        val messages: MutableMap<String, MessageEntity> = mutableMapOf()
        val channelsToFetch = mutableSetOf<String>()
        val messagesToFetch = mutableSetOf<String>()

        events.filterIsInstance<CidEvent>().onEach { channelsToFetch += it.cid }
        // For some reason backend is not sending us the user instance into some events that they should
        // and we are not able to identify which event type is. Gson, because it is using reflection,
        // inject a null instance into property `user` that doesn't allow null values.
        // This is a workaround, while we identify which event type is, that omit null values without
        // break our public API
        @Suppress("USELESS_CAST")
        users += events.filterIsInstance<UserEvent>().mapNotNull { it.user as User? }.associateBy(User::id)

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
                is ReactionNewEvent -> messagesToFetch += event.reaction.messageId
                is ReactionDeletedEvent -> messagesToFetch += event.reaction.messageId
                is ChannelMuteEvent -> channelsToFetch += event.channelMute.channel.cid
                is ChannelsMuteEvent -> {
                    event.channelsMute.forEach { channelsToFetch.add(it.channel.cid) }
                }
                is ChannelUnmuteEvent -> channelsToFetch += event.channelMute.channel.cid
                is ChannelsUnmuteEvent -> {
                    event.channelsMute.forEach { channelsToFetch.add(it.channel.cid) }
                }
                is MessageDeletedEvent -> messagesToFetch += event.message.id
                is MessageUpdatedEvent -> messagesToFetch += event.message.id
                is NewMessageEvent -> messagesToFetch += event.message.id
                is NotificationMessageNewEvent -> messagesToFetch += event.message.id
                is ReactionUpdateEvent -> messagesToFetch += event.message.id
            }.exhaustive
        }
        // actually fetch the data
        val channelMap = domainImpl.repos.channels.select(channelsToFetch.toList()).associateBy { it.cid }
        val messageMap = domainImpl.repos.messages.select(messagesToFetch.toList()).associateBy { it.id }

        fun addMessageData(cid: String, message: Message) {
            users.putAll(message.users().associateBy(User::id))
            messages[message.id] = MessageEntity(message)
            channelMap[cid]?.let {
                it.addMessage(MessageEntity(message))
                channels[it.cid] = it
            }
        }

        // step 2. second pass through the events, make a list of what we need to update
        loop@ for (event in events) {
            @Suppress("IMPLICIT_CAST_TO_ANY")
            when (event) {
                // keep the data in Room updated based on the various events..
                // note that many of these events should also update user information
                is NewMessageEvent -> {
                    event.message.cid = event.cid
                    event.totalUnreadCount?.let { domainImpl.setTotalUnreadCount(it) }
                    addMessageData(event.cid, event.message)
                }
                is MessageDeletedEvent -> {
                    event.message.cid = event.cid
                    addMessageData(event.cid, event.message)
                }
                is MessageUpdatedEvent -> {
                    event.message.cid = event.cid
                    addMessageData(event.cid, event.message)
                }
                is NotificationMessageNewEvent -> {
                    event.message.cid = event.cid
                    event.totalUnreadCount?.let { domainImpl.setTotalUnreadCount(it) }
                    addMessageData(event.cid, event.message)
                }
                is NotificationAddedToChannelEvent -> {
                    users.putAll(event.channel.users().associateBy(User::id))
                    channels[event.cid] = ChannelEntity(event.channel)
                }
                is NotificationInvitedEvent -> {
                    users[event.user.id] = event.user
                }
                is NotificationInviteAcceptedEvent -> {
                    users[event.user.id] = event.user
                }
                is ChannelHiddenEvent -> {
                    channels[event.cid] = ChannelEntity(Channel(cid = event.cid)).apply {
                        hidden = true
                        hideMessagesBefore = event.createdAt.takeIf { event.clearHistory }
                    }
                }
                is ChannelVisibleEvent -> {
                    channels[event.cid] = ChannelEntity(Channel(cid = event.cid)).apply {
                        hidden = false
                    }
                }
                is NotificationMutesUpdatedEvent -> {
                    domainImpl.updateCurrentUser(event.me)
                }
                is ConnectedEvent -> {
                    domainImpl.updateCurrentUser(event.me)
                }
                is MessageReadEvent -> {
                    // get the channel, update reads, write the channel
                    channelMap[event.cid]?.let {
                        channels[it.cid] = it.apply {
                            updateReads(ChannelUserRead(user = event.user, lastRead = event.createdAt))
                        }
                    }
                }
                is ReactionNewEvent -> {
                    // get the message, update the reaction data, update the message
                    // note that we need to use event.reaction and not event.message
                    // event.message only has a subset of reactions
                    messageMap[event.reaction.messageId]?.let {
                        messages[it.id] = it.apply {
                            addReaction(event.reaction, domainImpl.currentUser.id == event.user.id)
                        }
                    } ?: Unit
                }
                is ReactionDeletedEvent -> {
                    // get the message, update the reaction data, update the message
                    messageMap[event.reaction.messageId]?.let {
                        messages[it.id] = it.apply {
                            removeReaction(event.reaction, false)
                            reactionCounts = event.message.reactionCounts
                        }
                    } ?: Unit
                }
                is ReactionUpdateEvent -> {
                    messageMap[event.reaction.messageId]?.let {
                        messages[it.id] = it.apply {
                            addReaction(event.reaction, domainImpl.currentUser.id == event.user.id)
                        }
                    }
                }
                is MemberAddedEvent -> {
                    channelMap[event.cid]?.let {
                        it.setMember(event.member.user.id, event.member)
                        channels[it.cid] = it
                        users.put(event.member.user.id, event.member.user)
                    }
                }
                is MemberUpdatedEvent -> {
                    channelMap[event.cid]?.let {
                        it.setMember(event.member.user.id, event.member)
                        channels[it.cid] = it
                        users.put(event.member.user.id, event.member.user)
                    }
                }
                is MemberRemovedEvent -> {
                    channelMap[event.cid]?.let {
                        it.setMember(event.user.id, null)
                        channels[it.cid] = it
                    }
                }
                is NotificationRemovedFromChannelEvent -> {
                    channelMap[event.cid]?.let {
                        it.setMember(event.user.id, null)
                        channels[it.cid] = it
                    }
                }
                is ChannelUpdatedEvent -> {
                    channels[event.cid] = ChannelEntity(event.channel)
                    users.putAll(event.channel.users().associateBy(User::id))
                }
                is ChannelUpdatedByUserEvent -> {
                    channels[event.cid] = ChannelEntity(event.channel)
                    users.putAll(event.channel.users().associateBy(User::id))
                }
                is ChannelDeletedEvent -> {
                    channels[event.cid] = ChannelEntity(event.channel)
                    users.putAll(event.channel.users().associateBy(User::id))
                }
                is ChannelCreatedEvent -> {
                    channels[event.cid] = ChannelEntity(event.channel)
                    users.putAll(event.channel.users().associateBy(User::id))
                }
                is ChannelMuteEvent -> {
                    channels[event.channelMute.channel.cid] = ChannelEntity(event.channelMute.channel)
                    users.putAll(event.channelMute.channel.users().associateBy(User::id))
                }
                is ChannelsMuteEvent -> {
                    event.channelsMute.forEach {
                        channels[it.channel.cid] = ChannelEntity(it.channel)
                        users.putAll(it.channel.users().associateBy(User::id))
                    }
                }
                is ChannelUnmuteEvent -> {
                    channels[event.channelMute.channel.cid] = ChannelEntity(event.channelMute.channel)
                    users.putAll(event.channelMute.channel.users().associateBy(User::id))
                }
                is ChannelsUnmuteEvent -> {
                    event.channelsMute.forEach {
                        channels[it.channel.cid] = ChannelEntity(it.channel)
                        users.putAll(it.channel.users().associateBy(User::id))
                    }
                }
                is ChannelTruncatedEvent -> {
                    channels[event.cid] = ChannelEntity(event.channel)
                    users.putAll(event.channel.users().associateBy(User::id))
                }
                is NotificationChannelDeletedEvent -> {
                    channels[event.cid] = ChannelEntity(event.channel)
                    users.putAll(event.channel.users().associateBy(User::id))
                }
                is NotificationChannelMutesUpdatedEvent -> {
                    domainImpl.updateCurrentUser(event.me)
                }
                is NotificationChannelTruncatedEvent -> {
                    channels[event.cid] = ChannelEntity(event.channel)
                    users.putAll(event.channel.users().associateBy(User::id))
                }
                is NotificationMarkReadEvent -> {
                    event.totalUnreadCount?.let { domainImpl.setTotalUnreadCount(it) }
                    channelMap[event.cid]?.let {
                        channels[it.cid] = it.apply {
                            updateReads(ChannelUserRead(user = event.user, lastRead = event.createdAt))
                        }
                    }
                }
                is UserMutedEvent -> {
                    users[event.targetUser.id] = event.targetUser
                }
                is UsersMutedEvent -> {
                    event.targetUsers.forEach { users[it.id] = it }
                }
                is UserUnmutedEvent -> {
                    users[event.targetUser.id] = event.targetUser
                }
                is UsersUnmutedEvent -> {
                    event.targetUsers.forEach { users[it.id] = it }
                }
                is GlobalUserBannedEvent -> {
                    users[event.user.id] = event.user.apply { banned = true }
                }
                is GlobalUserUnbannedEvent -> {
                    users[event.user.id] = event.user.apply { banned = false }
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
        // actually insert the data
        users.remove(domainImpl.currentUser.id)?.let { domainImpl.updateCurrentUser(it) }
        domainImpl.repos.users.insert(users.values.toList())
        domainImpl.repos.channels.insert(channels.values.toList())
        // we only cache messages for which we're receiving events
        domainImpl.repos.messages.insert(messages.values.toList(), true)

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

        // step 3 - forward the events to the active chanenls

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
