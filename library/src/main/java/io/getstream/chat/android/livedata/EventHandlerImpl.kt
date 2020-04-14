package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class EventHandlerImpl(var repo: io.getstream.chat.android.livedata.ChatRepo, var runAsync: Boolean=true) {
    fun handleEvents(events: List<ChatEvent>) {
        if (runAsync) {
            GlobalScope.launch(Dispatchers.IO) {
                handleEventsInternal(events)
            }
        } else {
            runBlocking(Dispatchers.IO) {  }
        }
    }

    internal suspend fun handleEventsInternal(events: List<ChatEvent>) {
        val users: MutableMap<String, User> = mutableMapOf()
        val channels: MutableMap<String, Channel> = mutableMapOf()
        val messages: MutableMap<String, Message> = mutableMapOf()
        val configs: MutableMap<String, Config> = mutableMapOf()

        val channelsToFetch = mutableSetOf<String>()
        val messagesToFetch = mutableSetOf<String>()

        // step 1. see which data we need to retrieve from offline storage
        for (event in events) {
            when (event) {
                is MessageReadEvent, is MemberAddedEvent, is MemberRemovedEvent, is MemberUpdatedEvent, is ChannelUpdatedEvent, is ChannelHiddenEvent, is ChannelDeletedEvent -> {
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
        val channelMap = repo.repos.channels.select(channelsToFetch.toList()).associateBy{it.cid}
        val messageMap = repo.repos.messages.select(messagesToFetch.toList()).associateBy{it.id}


        // step 2. second pass through the events, make a list of what we need to update
        for (event in events) {
            when (event) {
                // TODO: all of these events should also update user information
                is NewMessageEvent, is MessageDeletedEvent, is MessageUpdatedEvent -> {
                    messages[event.message.id] = event.message
                    channels[event.message.channel.id] = event.message.channel
                }
                is MessageReadEvent -> {
                    // get the channel, update reads, write the channel
                    val channel = channelMap.get(event.cid)
                    val read = ChannelUserRead()
                    read.user = event.user!!
                    read.lastRead = event.createdAt
                    channel?.let {
                        it.updateReads(read)
                        insertChannelStateEntity(it)
                    }
                }
                is ReactionNewEvent -> {
                    // get the message, update the reaction data, update the message
                    // note that we need to use event.reaction and not event.message
                    // event.message only has a subset of reactions
                    val message = selectMessageEntity(event.reaction!!.messageId)
                    message?.let {
                        val userId = event.reaction!!.user!!.id
                        it.addReaction(event.reaction!!, currentUser.id == userId)
                        insertMessageEntity(it)
                    }
                }
                is ReactionDeletedEvent -> {
                    // get the message, update the reaction data, update the message
                    val message = selectMessageEntity(event.reaction!!.messageId)
                    message?.let {
                        val userId = event.reaction!!.user!!.id
                        it.removeReaction(event.reaction!!, false)
                        it.reactionCounts = event.message.reactionCounts
                        insertMessageEntity(it)
                    }
                }
                is UserPresenceChanged, is UserUpdated -> {
                    insertUser(event.user!!)
                }
                is MemberAddedEvent, is MemberRemovedEvent, is MemberUpdatedEvent -> {
                    // get the channel, update members, write the channel
                    val channelEntity = selectChannelEntity(event.cid!!)
                    if (channelEntity != null) {
                        var member = event.member
                        val userId = event.member!!.user.id
                        if (event is MemberRemovedEvent) {
                            member = null
                        }
                        channelEntity.setMember(userId, member)
                        insertChannelStateEntity(channelEntity)
                    }


                }
                is ChannelUpdatedEvent, is ChannelHiddenEvent, is ChannelDeletedEvent -> {
                    // get the channel, update members, write the channel
                    event.channel?.let {
                        insertChannel(it)
                    }
                }

            }
        }
    }
        // TODO: we need a handle events endpoints, instead of handle event
    suspend fun handleEvent(event: ChatEvent) {
        // keep the data in Room updated based on the various events..
        // TODO 1.1: cache users, messages and channels to reduce number of Room queries


        // any event can have channel and unread count information
        event.unreadChannels?.let { setChannelUnreadCount(it) }
        event.totalUnreadCount?.let { setTotalUnreadCount(it) }

        // if this is a channel level event, let the channel repo handle it
        if (event.isChannelEvent()) {
            val cid = event.cid!!
            if (activeChannelMap.containsKey(cid)) {
                val channelRepo = activeChannelMap.get(cid)!!
                channelRepo.handleEvent(event)
            }
        }

        // connection events
        when (event) {
            is DisconnectedEvent -> {
                _online.postValue(false)
            }
            is ConnectedEvent -> {
                val recovered = _initialized.value ?: false
                _online.postValue(true)
                _initialized.postValue(true)
                if (recovered) {
                    connectionRecovered(true)
                } else {
                    connectionRecovered(false)
                }
            }
        }

        // queryRepo mainly monitors for the notification added to channel event
        for ((_, queryRepo) in activeQueryMap) {
            queryRepo.handleEvent(event)
        }

        if (offlineEnabled) {
            event.user?.let { insertUser(it) }

            // TODO: all of these events should insert related objects like users, messages, channels etc

            when (event) {
                // TODO: all of these events should also update user information
                is NewMessageEvent, is MessageDeletedEvent, is MessageUpdatedEvent -> {
                    insertMessage(event.message)
                }
                is MessageReadEvent -> {
                    // get the channel, update reads, write the channel
                    val channel = channelStateDao.select(event.cid)
                    val read = ChannelUserRead()
                    read.user = event.user!!
                    read.lastRead = event.createdAt
                    channel?.let {
                        it.updateReads(read)
                        insertChannelStateEntity(it)
                    }
                }
                is ReactionNewEvent -> {
                    // get the message, update the reaction data, update the message
                    // note that we need to use event.reaction and not event.message
                    // event.message only has a subset of reactions
                    val message = selectMessageEntity(event.reaction!!.messageId)
                    message?.let {
                        val userId = event.reaction!!.user!!.id
                        it.addReaction(event.reaction!!, currentUser.id == userId)
                        insertMessageEntity(it)
                    }
                }
                is ReactionDeletedEvent -> {
                    // get the message, update the reaction data, update the message
                    val message = selectMessageEntity(event.reaction!!.messageId)
                    message?.let {
                        val userId = event.reaction!!.user!!.id
                        it.removeReaction(event.reaction!!, false)
                        it.reactionCounts = event.message.reactionCounts
                        insertMessageEntity(it)
                    }
                }
                is UserPresenceChanged, is UserUpdated -> {
                    insertUser(event.user!!)
                }
                is MemberAddedEvent, is MemberRemovedEvent, is MemberUpdatedEvent -> {
                    // get the channel, update members, write the channel
                    val channelEntity = selectChannelEntity(event.cid!!)
                    if (channelEntity != null) {
                        var member = event.member
                        val userId = event.member!!.user.id
                        if (event is MemberRemovedEvent) {
                            member = null
                        }
                        channelEntity.setMember(userId, member)
                        insertChannelStateEntity(channelEntity)
                    }


                }
                is ChannelUpdatedEvent, is ChannelHiddenEvent, is ChannelDeletedEvent -> {
                    // get the channel, update members, write the channel
                    event.channel?.let {
                        insertChannel(it)
                    }
                }

            }
        }
    }
}