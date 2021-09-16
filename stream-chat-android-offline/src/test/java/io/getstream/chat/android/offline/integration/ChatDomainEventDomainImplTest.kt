package io.getstream.chat.android.offline.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verify that all events correctly update state in room.
 */
@RunWith(AndroidJUnit4::class)
internal class ChatDomainEventDomainImplTest : BaseDomainTest2() {

    @Before
    override fun setup() {
        super.setup()
        runBlocking {
            chatDomainImpl.repos.insertUsers(data.userMap.values)
        }
    }

    @Test
    fun `verify that a missing channel config returns the default`(): Unit = runBlocking {
        val config = chatDomainImpl.getChannelConfig("missing")
        config shouldBeEqualTo (chatDomainImpl.defaultConfig)
    }

    @Test
    fun `verify that a new message event is stored in room`(): Unit = runBlocking {
        // new messages should be stored in room
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent)
        val message = chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
        message.shouldNotBeNull()
    }

    @Test
    fun `channel controller edit message event`(): Unit = runBlocking {
        // setup the queryControllerImpl
        queryControllerImpl.query(10)

        // update the last message
        chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)
        // channelControllerImpl.handleEvent(data.messageUpdatedEvent)
        // queryControllerImpl.handleEvent(data.messageUpdatedEvent)

        // verify that the last message is now updated
        val channelMap = queryControllerImpl.channels.value.associateBy { it.cid }
        val channel1 = channelMap[data.channel1.cid]
        channel1!!.messages.last().text shouldBeEqualTo data.messageUpdatedEvent.message.text
    }

    @Test
    fun `new notification message event should be stored in room`(): Unit = runBlocking {
        // new messages should be stored in room
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        val message =
            chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
        message.shouldNotBeNull()
    }

    @Test
    fun `when you are added to a channel it should be stored in room`(): Unit = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.notificationAddedToChannel2Event)
        val channel =
            chatDomainImpl.repos.selectChannelWithoutMessages(data.notificationAddedToChannel2Event.channel.cid)
        channel.shouldNotBeNull()
    }

    @Test
    fun `truncating a channel should remove all messages`(): Unit = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        chatDomainImpl.eventHandler.handleEvent(data.channelTruncatedEvent)
        val message =
            chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
        message.shouldBeNull()
        val channelController = chatDomainImpl.channel(data.channel1)
        val messages = channelController.messages.value
        messages.shouldBeEmpty()
    }

    @Test
    fun `verify that a truncate notification event also works`(): Unit = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        chatDomainImpl.eventHandler.handleEvent(data.notificationChannelTruncated)
        val message =
            chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
        message.shouldBeNull()
        val channelController = chatDomainImpl.channel(data.channel1)
        val messages = channelController.messages.value
        messages.shouldBeEmpty()
    }

    @Test
    fun `verify that a channel is correctly deleted when channel deleted event is received`(): Unit =
        runBlocking {
            chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
            chatDomainImpl.eventHandler.handleEvent(data.channelDeletedEvent)
            val message =
                chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
            val channel = chatDomainImpl.repos.selectChannelWithoutMessages(data.channel1.cid)
            message.shouldBeNull()
            channel!!.deletedAt shouldBeEqualTo data.channelDeletedEvent.createdAt
            val channelController = chatDomainImpl.channel(data.channel1)
            val messages = channelController.messages.value
            val channelData = channelController.channelData.value
            messages.shouldBeEmpty()
            channelData.deletedAt shouldBeEqualTo data.channelDeletedEvent.createdAt
        }

    @Test
    fun `the current user information should be stored using users insertMe`(): Unit = runBlocking {
        data.user1.extraData = mutableMapOf("snack" to "icecream")
        chatDomainImpl.repos.insertCurrentUser(data.user1)
        val me = chatDomainImpl.repos.selectCurrentUser()
        me.shouldNotBeNull()
        me?.id shouldBeEqualTo "broad-lake-3"
    }

    @Ignore
    @Test
    fun `handle unread counts on the connect event`(): Unit = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.connectedEvent2)
        chatDomainImpl.channelUnreadCount.value shouldBeEqualTo 2
        chatDomainImpl.totalUnreadCount.value shouldBeEqualTo 3
    }

    @Test
    fun `the mute user event should update the list of mutes users`(): Unit = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.notificationMutesUpdated)
        chatDomainImpl.muted.value shouldBeEqualTo data.notificationMutesUpdated.me.mutes
    }

    @Test
    fun `a message read event should be stored on the channel`(): Unit = runBlocking {
        chatDomainImpl.repos.insertChannel(data.channel1)
        chatDomainImpl.eventHandler.handleEvent(data.readEvent)
        // check channel level read info
        val cid = data.readEvent.cid
        val channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)
        channel!!.read.size shouldBeEqualTo 1
        val read = channel.read.first()
        read.user.id shouldBeEqualTo data.readEvent.user.id
    }

    @Test
    fun `a reaction event should update the denormalized message fields`(): Unit = runBlocking {
        // add the message
        val messageId = data.newMessageEvent.message.id
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent)
        // add the reaction
        val secondId = data.reactionEvent.reaction.messageId
        secondId shouldBeEqualTo messageId
        chatDomainImpl.eventHandler.handleEvent(data.reactionEvent)
        // fetch the message
        var message = chatDomainImpl.repos.selectMessage(messageId)!!

        // reaction from yourself (so it goes into ownReactions)
        message.reactionCounts["like"] shouldBeEqualTo 1
        message.reactionScores["like"] shouldBeEqualTo 10

        message.latestReactions.first().userId shouldBeEqualTo data.reaction1.user!!.id
        message.ownReactions.first().userId shouldBeEqualTo data.reaction1.user!!.id

        // add a reaction from a different user, it should not go into own reaction
        chatDomainImpl.eventHandler.handleEvent(data.reactionEvent2)
        message = chatDomainImpl.repos.selectMessage(messageId)!!
        message.reactionCounts["like"] shouldBeEqualTo 2
        message.latestReactions.size shouldBeEqualTo 2
        message.ownReactions.size shouldBeEqualTo 1
    }

    @Test
    fun `verify that a channel update event works correctly`(): Unit = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.channelUpdatedEvent)
        // check channel level read info
        val cid = data.channelUpdatedEvent.cid
        val channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        channel.extraData["color"] shouldBeEqualTo "green"
    }

    @Test
    fun `add and remove member should update the room storage`(): Unit = runBlocking {
        // add the member to the channel
        chatDomainImpl.repos.insertChannel(data.channel1)
        chatDomainImpl.eventHandler.handleEvent(data.memberAddedToChannelEvent)
        val cid = data.memberAddedToChannelEvent.cid
        // verify that user 2 is now part of the members
        var channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        channel.members.size shouldBeEqualTo 2
        chatDomainImpl.eventHandler.handleEvent(data.memberRemovedFromChannel)
        channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        channel.members.size shouldBeEqualTo 1
    }

    @Test
    fun `member notification events should update room`(): Unit = runBlocking {
        // add the member to the channel
        chatDomainImpl.repos.insertChannel(data.channel1)
        chatDomainImpl.eventHandler.handleEvent(data.memberAddedToChannelEvent)
        val cid = data.memberAddedToChannelEvent.cid
        // verify that user 2 is now part of the members
        var channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        channel.members.size shouldBeEqualTo 2
        // remove user 1
        chatDomainImpl.eventHandler.handleEvent(data.notificationRemovedFromChannel)
        channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        channel.members.size shouldBeEqualTo 1
    }
}
