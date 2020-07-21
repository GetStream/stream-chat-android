package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.entity.ChannelEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.UserEntity
import io.getstream.chat.android.livedata.extensions.getCid
import io.getstream.chat.android.livedata.extensions.isChannelEvent
import io.getstream.chat.android.livedata.extensions.users
import kotlinx.coroutines.*

class EventHandlerImpl(var domainImpl: io.getstream.chat.android.livedata.ChatDomainImpl, var runAsync: Boolean = true) {
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
                is MessageReadEvent, is MemberAddedEvent, is MemberRemovedEvent, is NotificationRemovedFromChannel,
                is MemberUpdatedEvent, is ChannelUpdatedEvent, is ChannelDeletedEvent, is ChannelHiddenEvent, is ChannelVisible,
                is NotificationAddedToChannelEvent, is NotificationInvited, is NotificationInviteAccepted, is NotificationInviteRejected
                -> {
                    // get the channel, update reads, write the channel
                    channelsToFetch.add(cid)
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
                is UserBanned, is UserUnbanned -> {
                    me = domainImpl.repos.users.selectMe()
                }
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
                is NewMessageEvent, is MessageDeletedEvent, is MessageUpdatedEvent -> {
                    val message = event.message
                    message.cid = event.cid!!
                    messages[message.id] = MessageEntity(message)
                    users.putAll(message.users().map { UserEntity(it) }.associateBy { it.id })
                }
                is NotificationMessageNew -> {
                    // add both the event and the channel
                    val message = event.message
                    message.cid = event.cid!!
                    messages[message.id] = MessageEntity(message)
                    channels[event.channel!!.cid] = ChannelEntity(event.channel!!)
                    users.putAll(message.users().map { UserEntity(it) }.associateBy { it.id })
                    users.putAll(event.channel!!.users().map { UserEntity(it) }.associateBy { it.id })
                }
                is NotificationAddedToChannelEvent, is NotificationInvited, is NotificationInviteAccepted, is NotificationInviteRejected -> {
                    channels[event.channel!!.cid] = ChannelEntity(event.channel!!)
                    users.putAll(event.channel!!.users().map { UserEntity(it) }.associateBy { it.id })
                }
                is ChannelHiddenEvent -> {
                    val channelEntity = ChannelEntity(event.channel!!)
                    channelEntity.hidden = true
                    if (event.clearHistory != null && event.clearHistory!!) {
                        channelEntity.hideMessagesBefore = event.createdAt
                    }
                    channels[event.channel!!.cid] = channelEntity
                }
                is ChannelVisible -> {
                    val channelEntity = ChannelEntity(event.channel!!)
                    // TODO: this approach is not so nice since it's overwritten by other data
                    channelEntity.hidden = false
                    channels[event.channel!!.cid] = channelEntity
                }
                is UserBanned, is UserUnbanned -> {
                    me?.let { it.banned = event is UserBanned }
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
                is UserUpdated -> {
                    event.user?.let { users[it.id] = UserEntity(it) }
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
                is MemberAddedEvent, is MemberUpdatedEvent -> {
                    val channelEntity = channelMap[event.cid!!]
                    channelEntity?.let { channelEntity ->
                        event.channel?.members?.forEach { member ->
                            channelEntity.setMember(member.user.id, member)
                        }
                        channels[channelEntity.cid] = channelEntity
                    }
                    event.channel?.let { c ->
                        users.putAll(c.users().map { UserEntity(it) }.associateBy { it.id })
                    }
                }
                is MemberRemovedEvent, is NotificationRemovedFromChannel -> {
                    // get the channel, update members, write the channel
                    val channelEntity = channelMap[event.getCid()!!]
                    var user = event.user
                    // quite confusing but NotificationRemovedFromChannel is only fired for the current user
                    if (event is NotificationRemovedFromChannel) {
                        user = domainImpl.currentUser
                    }
                    channelEntity?.let {
                        it.setMember(user!!.id, null)
                        channels[it.cid] = it
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
                is NotificationChannelTruncated, is ChannelTruncated -> {
                    domainImpl.repos.messages.deleteChannelMessagesBefore(event.getCid()!!, event.createdAt!!)
                }
                is ChannelDeletedEvent -> {
                    domainImpl.repos.messages.deleteChannelMessagesBefore(event.getCid()!!, event.createdAt!!)
                    val channel = domainImpl.repos.channels.select(event.getCid()!!)
                    channel?.let {
                        it.deletedAt = event.createdAt!!
                        domainImpl.repos.channels.insert(it)
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

    internal suspend fun handleEventsInternal(events: List<ChatEvent>) {
        events.sortedBy { it.createdAt }
        updateOfflineStorageFromEvents(events)

        // step 3 - forward the events to the active chanenls

        val channelEvents: MutableMap<String, MutableList<ChatEvent>> = mutableMapOf()
        for (event in events) {
            if (event.isChannelEvent()) {
                if (!channelEvents.containsKey(event.cid!!)) {
                    channelEvents[event.cid!!] = mutableListOf()
                }
                channelEvents[event.cid!!]!!.add(event)
            }
        }
        for ((cid, cEvents) in channelEvents) {
            if (domainImpl.isActiveChannel(cid)) {
                domainImpl.channel(cid).handleEvents(cEvents)
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
