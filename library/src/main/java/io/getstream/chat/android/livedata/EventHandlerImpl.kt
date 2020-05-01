package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.controller.isChannelEvent
import io.getstream.chat.android.livedata.controller.users
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.UserEntity
import java.util.InputMismatchException
import kotlinx.coroutines.*

class EventHandlerImpl(var domainImpl: io.getstream.chat.android.livedata.ChatDomainImpl, var runAsync: Boolean = true) {
    private val logger = ChatLogger.get("ChatDomain EventHandler")

    fun handleEvents(events: List<ChatEvent>) {
        if (runAsync) {
            domainImpl.scope.launch(Dispatchers.IO) {
                handleEventsInternal(events)
            }
        } else {
            runBlocking(domainImpl.scope.coroutineContext) { handleEventsInternal(events) }
        }
    }

    internal suspend fun handleEvent(event: ChatEvent) {
        handleEventsInternal(listOf(event))
    }

    internal suspend fun handleEventsInternal(events: List<ChatEvent>) {
        events.sortedBy { it.createdAt }
        val users: MutableMap<String, UserEntity> = mutableMapOf()
        val channels: MutableMap<String, ChannelEntity> = mutableMapOf()
        var me: User? = null

        val messages: MutableMap<String, MessageEntity> = mutableMapOf()
        var unreadChannels: Int? = null
        var totalUnreadCount: Int? = null
        var channelEvents: MutableMap<String, MutableList<ChatEvent>> = mutableMapOf()

        val channelsToFetch = mutableSetOf<String>()
        val messagesToFetch = mutableSetOf<String>()

        // step 1. see which data we need to retrieve from offline storage
        for (event in events) {
            when (event) {
                is MessageReadEvent, is MemberAddedEvent, is MemberRemovedEvent, is MemberUpdatedEvent, is ChannelUpdatedEvent, is ChannelDeletedEvent, is ChannelHiddenEvent, is ChannelVisible -> {
                    // get the channel, update reads, write the channel
                    channelsToFetch.add(event.cid!!)
                }
                is ReactionNewEvent -> {
                    // get the message, update the reaction data, update the message
                    // note that we need to use event.reaction and not event.message
                    // event.message only has a subset of reactions
                    messagesToFetch.add(event.reaction!!.messageId)
                }
                is ReactionDeletedEvent -> {
                    // get the message, update the reaction data, update the message
                    messagesToFetch.add(event.reaction!!.messageId)
                }
            }
        }
        // actually fetch the data
        val channelMap = domainImpl.repos.channels.select(channelsToFetch.toList()).associateBy { it.cid }
        val messageMap = domainImpl.repos.messages.select(messagesToFetch.toList()).associateBy { it.id }

        // step 2. second pass through the events, make a list of what we need to update
        for (event in events) {
            // any event can have channel and unread count information
            event.unreadChannels?.let { unreadChannels = it }
            event.totalUnreadCount?.let { totalUnreadCount = it }

            event.user?.let {
                users[it.id] = UserEntity(it)
            }
            if (event.isChannelEvent()) {
                if (!channelEvents.containsKey(event.cid!!)) {
                    channelEvents[event.cid!!] = mutableListOf()
                }
                channelEvents[event.cid!!]!!.add(event)
            }

            when (event) {
                // keep the data in Room updated based on the various events..
                // note that many of these events should also update user information
                is NewMessageEvent, is MessageDeletedEvent, is MessageUpdatedEvent -> {
                    val message = event.message
                    message.cid = event.cid!!
                    messages[message.id] = MessageEntity(message)
                    users.putAll(message.users().map { UserEntity(it) }.associateBy { it.id })
                }
                is NotificationMessageNew -> {
                    messages[event.message.id] = MessageEntity(event.message)
                    channels[event.channel!!.id] = ChannelEntity(event.channel!!)
                    users.putAll(event.message.users().map { UserEntity(it) }.associateBy { it.id })
                    users.putAll(event.channel!!.users().map { UserEntity(it) }.associateBy { it.id })
                }
                is ChannelHiddenEvent -> {
                    val channelEntity = ChannelEntity(event.channel!!)
                    channelEntity.hidden = true
                    if (event.clearHistory != null && event.clearHistory!!) {
                        channelEntity.hideMessagesBefore = event.createdAt
                    }
                    channels[event.channel!!.id] = channelEntity
                }
                is ChannelVisible -> {
                    val channelEntity = ChannelEntity(event.channel!!)
                    // TODO: this approach is not so nice since it's overwritten by other data
                    channelEntity.hidden = false
                    channels[event.channel!!.id] = channelEntity
                }
                is NotificationMutesUpdated -> {
                    val muteEvent: NotificationMutesUpdated = event
                    me = muteEvent.me
                }
                is ConnectedEvent -> {
                    val connectedEvent: ConnectedEvent = event
                    me = connectedEvent.me
                }
                is MessageReadEvent -> {
                    // get the channel, update reads, write the channel
                    val channel = channelMap[event.cid]
                    val read = ChannelUserRead(event.user!!)
                    read.lastRead = event.createdAt
                    channel?.let {
                        it.updateReads(read)
                        channels[it.cid] = it
                    }
                }
                is ReactionNewEvent -> {
                    // get the message, update the reaction data, update the message
                    // note that we need to use event.reaction and not event.message
                    // event.message only has a subset of reactions
                    val message = messageMap[event.reaction!!.messageId]
                    message?.let {
                        val userId = event.reaction!!.user!!.id
                        it.addReaction(event.reaction!!, domainImpl.currentUser.id == userId)
                        messages[it.id] = it
                    }
                }
                is ReactionDeletedEvent -> {
                    // get the message, update the reaction data, update the message
                    val message = messageMap[event.reaction!!.messageId]
                    message?.let {
                        it.removeReaction(event.reaction!!, false)
                        it.reactionCounts = event.message.reactionCounts
                        messages[it.id] = it
                    }
                }
                is MemberAddedEvent, is MemberRemovedEvent, is MemberUpdatedEvent -> {
                    // get the channel, update members, write the channel
                    val channelEntity = channelMap.get(event.cid!!)
                    if (channelEntity != null) {
                        var member = event.member
                        val userId = event.member!!.user.id
                        if (event is MemberRemovedEvent) {
                            member = null
                        }
                        channelEntity.setMember(userId, member)
                        channels[channelEntity.cid] = channelEntity
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
            // actually insert the data
            domainImpl.repos.users.insert(users.values.toList())
            domainImpl.repos.channels.insert(channels.values.toList())
            // we only cache messages for which we're receiving events
            domainImpl.repos.messages.insert(messages.values.toList(), true)
            if (me != null) {
                domainImpl.updateCurrentUser(me)
            }

            unreadChannels?.let { domainImpl.setChannelUnreadCount(it) }
            totalUnreadCount?.let { domainImpl.setTotalUnreadCount(it) }

            // step 3 - forward the events to the active queries and channels
            for ((cid, cEvents) in channelEvents) {
                if (domainImpl.isActiveChannel(cid)) {
                    domainImpl.channel(cid).handleEvents(cEvents)
                }
            }

            for (chatEvent in events) {

                // connection events are never send on the recovery endpoint, so handle them 1 by 1
                when (chatEvent) {
                    is DisconnectedEvent -> {
                        domainImpl.postOffline()
                    }
                    is ConnectedEvent -> {
                        val connectedEvent: ConnectedEvent = chatEvent
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
            // queryRepo mainly monitors for the notification added to channel event
            for (queryRepo in domainImpl.getActiveQueries()) {
                queryRepo.handleEvents(events)
            }


        }
    }
}
