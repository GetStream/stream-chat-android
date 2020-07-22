package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncated
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelVisible
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncated
import io.getstream.chat.android.client.events.NotificationInviteAccepted
import io.getstream.chat.android.client.events.NotificationInviteRejected
import io.getstream.chat.android.client.events.NotificationInvited
import io.getstream.chat.android.client.events.NotificationMessageNew
import io.getstream.chat.android.client.events.NotificationMutesUpdated
import io.getstream.chat.android.client.events.NotificationRemovedFromChannel
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.UserBanned
import io.getstream.chat.android.client.events.UserUnbanned
import io.getstream.chat.android.client.events.UserUpdated
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.UserEntity
import io.getstream.chat.android.livedata.extensions.getCid
import io.getstream.chat.android.livedata.extensions.isChannelEvent
import io.getstream.chat.android.livedata.extensions.users
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EventHandlerImpl(var domainImpl: ChatDomainImpl, var runAsync: Boolean = true) {
    private val logger = ChatLogger.get("ChatDomain EventHandler")

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
        events.sortedBy { it.createdAt }

        val users: MutableMap<String, UserEntity> = mutableMapOf()
        val channels: MutableMap<String, ChannelEntity> = mutableMapOf()
        var me: User? = null

        val messages: MutableMap<String, MessageEntity> = mutableMapOf()
        var unreadChannels: Int? = null
        var totalUnreadCount: Int? = null

        val channelsToFetch = mutableSetOf<String>()
        val messagesToFetch = mutableSetOf<String>()

        // step 1. see which data we need to retrieve from offline storage
        for (event in events) {
            val cid = event.cid ?: event.channel?.cid ?: ""
            logger.logI("Handling event of type ${event.type} for cid $cid, event batch size is ${events.size}")
            when (event) {
                is MessageReadEvent,
                is MemberAddedEvent,
                is MemberRemovedEvent,
                is NotificationRemovedFromChannel,
                is MemberUpdatedEvent,
                is ChannelUpdatedEvent,
                is ChannelDeletedEvent,
                is ChannelHiddenEvent,
                is ChannelVisible,
                is NotificationAddedToChannelEvent,
                is NotificationInvited,
                is NotificationInviteAccepted,
                is NotificationInviteRejected -> channelsToFetch.add(cid)
                is ReactionNewEvent,
                is ReactionDeletedEvent -> event.reaction?.messageId?.let(messagesToFetch::add)
                is UserBanned, is UserUnbanned -> { me = domainImpl.repos.users.selectMe() }
            }
        }
        // actually fetch the data
        val channelMap = domainImpl.repos.channels.select(channelsToFetch.toList()).associateBy { it.cid }
        val messageMap = domainImpl.repos.messages.select(messagesToFetch.toList()).associateBy { it.id }

        // step 2. second pass through the events, make a list of what we need to update
        loop@ for (event in events) {
            // any event can have channel and unread count information
            event.unreadChannels?.let { unreadChannels = it }
            event.totalUnreadCount?.let { totalUnreadCount = it }

            event.user?.let {
                users[it.id] = UserEntity(it)
            }

            when (event) {
                // keep the data in Room updated based on the various events..
                // note that many of these events should also update user information
                is NewMessageEvent,
                is MessageDeletedEvent,
                is MessageUpdatedEvent -> {
                    val message = event.message
                    event.cid?.let { cid ->
                        message.cid = cid
                        messages[message.id] = MessageEntity(message)
                        users.putAll(message.users().map { UserEntity(it) }.associateBy { it.id })
                    }
                }
                is NotificationMessageNew -> {
                    // add both the event and the channel
                    val message = event.message
                    event.cid?.let { cid ->
                        message.cid = cid
                        messages[message.id] = MessageEntity(message)
                        channels[cid] = ChannelEntity(event.channel ?: Channel(cid = cid))
                        users.putAll(message.users().map { UserEntity(it) }.associateBy { it.id })
                        event.channel?.let { channel ->
                            users.putAll(channel.users().map { UserEntity(it) }.associateBy { it.id })
                        }
                    }
                }
                is NotificationAddedToChannelEvent,
                is NotificationInvited,
                is NotificationInviteAccepted,
                is NotificationInviteRejected -> {
                    event.channel?.let { channel ->
                        channels[channel.cid] = ChannelEntity(channel)
                        users.putAll(channel.users().map { UserEntity(it) }.associateBy { it.id })
                    }
                }
                is ChannelHiddenEvent -> {
                    event.cid?.let { cid ->
                        val channelEntity = ChannelEntity(Channel(cid = cid))
                        channelEntity.hidden = true
                        if (event.clearHistory == true) {
                            channelEntity.hideMessagesBefore = event.createdAt
                        }
                        channels[cid] = channelEntity
                    }
                }
                is ChannelVisible -> {
                    event.cid?.let { cid ->
                        val channelEntity = ChannelEntity(Channel(cid = cid))
                        channelEntity.hidden = false
                        channels[cid] = channelEntity
                    }
                }
                is UserBanned,
                is UserUnbanned -> {
                    me?.let { it.banned = event is UserBanned }
                }
                is NotificationMutesUpdated -> {
                    me = event.me
                }
                is ConnectedEvent -> {
                    if (event.isValid()) {
                        me = event.me
                    }
                }
                is MessageReadEvent -> {
                    // get the channel, update reads, write the channel
                    channelMap[event.cid]?.let { channelEntity ->
                        event.user?.let { user ->
                            val read = ChannelUserRead(user)
                            read.lastRead = event.createdAt
                            channelEntity.updateReads(read)
                        }
                        channels[channelEntity.cid] = channelEntity
                    }
                }
                is UserUpdated -> event.user?.let { users[it.id] = UserEntity(it) }
                is ReactionNewEvent -> {
                    // get the message, update the reaction data, update the message
                    // note that we need to use event.reaction and not event.message
                    // event.message only has a subset of reactions
                    event.reaction?.let { reaction ->
                        messageMap[reaction.messageId]?.let { messageEntity ->
                            messageEntity.addReaction(reaction, domainImpl.currentUser.id == reaction.user?.id)
                            messages[messageEntity.id] = messageEntity
                        }
                    }
                }
                is ReactionDeletedEvent -> {
                    // get the message, update the reaction data, update the message
                    event.reaction?.let { reaction ->
                        messageMap[reaction.messageId]?.let { messageEntity ->
                            messageEntity.removeReaction(reaction, false)
                            messageEntity.reactionCounts = event.message.reactionCounts
                            messages[messageEntity.id] = messageEntity
                        }
                    }
                }
                is MemberAddedEvent, is MemberUpdatedEvent -> {
                    event.cid?.let { cid ->
                        channelMap[cid]?.let { channelEntity ->
                            event.channel?.members?.forEach { member ->
                                channelEntity.setMember(member.user.id, member)
                            }
                            channels[channelEntity.cid] = channelEntity
                        }
                        event.channel?.let { c ->
                            users.putAll(c.users().map { UserEntity(it) }.associateBy { it.id })
                        }
                    }
                }
                is MemberRemovedEvent, is NotificationRemovedFromChannel -> {
                    // get the channel, update members, write the channel
                    var user = event.user
                    // quite confusing but NotificationRemovedFromChannel is only fired for the current user
                    if (event is NotificationRemovedFromChannel) {
                        user = domainImpl.currentUser
                    }
                    event.getCid()?.let { cid ->
                        channelMap[cid]?.let { channelEntity ->
                            user?.let { channelEntity.setMember(it.id, null) }
                            channels[cid] = channelEntity
                        }
                    }
                    event.channel?.let { c ->
                        users.putAll(c.users().map { UserEntity(it) }.associateBy { it.id })
                    }
                }
                is ChannelUpdatedEvent, is ChannelDeletedEvent -> {
                    // get the channel, update members, write the channel
                    event.channel?.let {
                        channels[it.cid] = ChannelEntity(it)
                    }
                    event.channel?.let { c ->
                        users.putAll(c.users().map { UserEntity(it) }.associateBy { it.id })
                    }
                }
            }
        }
        // actually insert the data
        domainImpl.repos.users.insert(users.values.toList())
        domainImpl.repos.channels.insert(channels.values.toList())
        // we only cache messages for which we're receiving events
        domainImpl.repos.messages.insert(messages.values.toList(), true)

        // handle delete and truncate events
        for (event in events) {
            when (event) {
                is NotificationChannelTruncated,
                is ChannelTruncated -> {
                    event.getCid()?.let { cid ->
                        event.createdAt?.let { createdAt ->
                            domainImpl.repos.messages.deleteChannelMessagesBefore(cid, createdAt)
                        }
                    }
                }
                is ChannelDeletedEvent -> {
                    event.getCid()?.let { cid ->
                        event.createdAt?.let { createdAt ->
                            domainImpl.repos.messages.deleteChannelMessagesBefore(cid, createdAt)
                            domainImpl.repos.channels.select(cid)?.let {
                                it.deletedAt = createdAt
                                domainImpl.repos.channels.insert(it)
                            }
                        }
                    }
                }
            }
        }

        me?.let {
            domainImpl.updateCurrentUser(it)
        }

        unreadChannels?.let { domainImpl.setChannelUnreadCount(it) }
        totalUnreadCount?.let { domainImpl.setTotalUnreadCount(it) }
    }

    private suspend fun handleEventsInternal(events: List<ChatEvent>) {
        events.sortedBy { it.createdAt }
        updateOfflineStorageFromEvents(events)

        // step 3 - forward the events to the active chanenls

        events.filter { it.isChannelEvent() }
            .groupBy { it.cid ?: "" }
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
