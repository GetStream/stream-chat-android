package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verify that all events correctly update state in room
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
    fun `verify that a missing channel config returns the default`() = runBlocking {
        val config = chatDomainImpl.getChannelConfig("missing")
        Truth.assertThat(config).isEqualTo((chatDomainImpl.defaultConfig))
    }

    @Test
    fun `verify that a new message event is stored in room`() = runBlocking {
        // new messages should be stored in room
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent)
        val message = chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
        Truth.assertThat(message).isNotNull()
    }

    @Test
    fun `channel controller edit message event`() = runBlocking {
        // setup the queryControllerImpl
        queryControllerImpl.query(10)

        // update the last message
        chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)
        // channelControllerImpl.handleEvent(data.messageUpdatedEvent)
        // queryControllerImpl.handleEvent(data.messageUpdatedEvent)

        // verify that the last message is now updated
        val channelMap = queryControllerImpl.channels.getOrAwaitValue().associateBy { it.cid }
        val channel1 = channelMap[data.channel1.cid]
        Truth.assertThat(channel1!!.messages.last().text).isEqualTo(data.messageUpdatedEvent.message.text)
    }

    @Test
    fun `new notification message event should be stored in room`() = runBlocking {
        // new messages should be stored in room
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        val message =
            chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
        Truth.assertThat(message).isNotNull()
    }

    @Test
    fun `when you are added to a channel it should be stored in room`() = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.notificationAddedToChannel2Event)
        val channel = chatDomainImpl.repos.selectChannelWithoutMessages(data.notificationAddedToChannel2Event.channel.cid)
        Truth.assertThat(channel).isNotNull()
    }

    @Test
    fun `truncating a channel should remove all messages`() = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        chatDomainImpl.eventHandler.handleEvent(data.channelTruncatedEvent)
        val message =
            chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
        Truth.assertThat(message).isNull()
        val channelController = chatDomainImpl.channel(data.channel1)
        val messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages).isEmpty()
    }

    @Test
    fun `verify that a truncate notification event also works`() = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        chatDomainImpl.eventHandler.handleEvent(data.notificationChannelTruncated)
        val message =
            chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
        Truth.assertThat(message).isNull()
        val channelController = chatDomainImpl.channel(data.channel1)
        val messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages).isEmpty()
    }

    @Test
    fun `verify that a channel is correctly deleted when channel deleted event is received`() =
        runBlocking {
            chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
            chatDomainImpl.eventHandler.handleEvent(data.channelDeletedEvent)
            val message =
                chatDomainImpl.repos.selectMessage(data.newMessageEvent.message.id)
            val channel = chatDomainImpl.repos.selectChannelWithoutMessages(data.channel1.cid)
            Truth.assertThat(message).isNull()
            Truth.assertThat(channel!!.deletedAt).isEqualTo(data.channelDeletedEvent.createdAt)
            val channelController = chatDomainImpl.channel(data.channel1)
            val messages = channelController.messages.getOrAwaitValue()
            val channelData = channelController.channelData.getOrAwaitValue()
            Truth.assertThat(messages).isEmpty()
            Truth.assertThat(channelData.deletedAt).isEqualTo(data.channelDeletedEvent.createdAt)
        }

    @Test
    fun `the current user information should be stored using users insertMe`() = runBlocking {
        data.user1.extraData = mutableMapOf("snack" to "icecream")
        chatDomainImpl.repos.insertCurrentUser(data.user1)
        val me = chatDomainImpl.repos.selectCurrentUser()
        Truth.assertThat(me).isNotNull()
        Truth.assertThat(me?.id).isEqualTo("broad-lake-3")
    }

    @Ignore
    @Test
    fun `handle unread counts on the connect event`() = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.connectedEvent2)
        Truth.assertThat(chatDomainImpl.channelUnreadCount.getOrAwaitValue()).isEqualTo(2)
        Truth.assertThat(chatDomainImpl.totalUnreadCount.getOrAwaitValue()).isEqualTo(3)
    }

    @Test
    fun `the mute user event should update the list of mutes users`() = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.notificationMutesUpdated)
        Truth.assertThat(chatDomainImpl.muted.getOrAwaitValue())
            .isEqualTo(data.notificationMutesUpdated.me.mutes)
    }

    @Test
    fun `a message read event should be stored on the channel`() = runBlocking {
        chatDomainImpl.repos.insertChannel(data.channel1)
        chatDomainImpl.eventHandler.handleEvent(data.readEvent)
        // check channel level read info
        val cid = data.readEvent.cid
        val channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)
        Truth.assertThat(channel!!.read.size).isEqualTo(1)
        val read = channel.read.first()
        Truth.assertThat(read.user.id).isEqualTo(data.readEvent.user.id)
    }

    @Test
    fun `a reaction event should update the denormalized message fields`() = runBlocking {
        // add the message
        val messageId = data.newMessageEvent.message.id
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent)
        // add the reaction
        val secondId = data.reactionEvent.reaction.messageId
        Truth.assertThat(secondId).isEqualTo(messageId)
        chatDomainImpl.eventHandler.handleEvent(data.reactionEvent)
        // fetch the message
        var message = chatDomainImpl.repos.selectMessage(messageId)!!

        // reaction from yourself (so it goes into ownReactions)
        Truth.assertThat(message.reactionCounts["like"]).isEqualTo(1)
        Truth.assertThat(message.reactionScores["like"]).isEqualTo(10)

        Truth.assertThat(message.latestReactions.first().userId)
            .isEqualTo(data.reaction1.user!!.id)
        Truth.assertThat(message.ownReactions.first().userId)
            .isEqualTo(data.reaction1.user!!.id)

        // add a reaction from a different user, it should not go into own reaction
        chatDomainImpl.eventHandler.handleEvent(data.reactionEvent2)
        message = chatDomainImpl.repos.selectMessage(messageId)!!
        Truth.assertThat(message.reactionCounts["like"]).isEqualTo(2)
        Truth.assertThat(message.latestReactions.size).isEqualTo(2)
        Truth.assertThat(message.ownReactions.size).isEqualTo(1)
    }

    @Test
    fun `verify that a channel update event works correctly`() = runBlocking {
        chatDomainImpl.eventHandler.handleEvent(data.channelUpdatedEvent)
        // check channel level read info
        val cid = data.channelUpdatedEvent.cid
        val channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        Truth.assertThat(channel.extraData["color"]).isEqualTo("green")
    }

    @Test
    fun `add and remove member should update the room storage`() = runBlocking {
        // add the member to the channel
        chatDomainImpl.repos.insertChannel(data.channel1)
        chatDomainImpl.eventHandler.handleEvent(data.memberAddedToChannelEvent)
        val cid = data.memberAddedToChannelEvent.cid
        // verify that user 2 is now part of the members
        var channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(2)
        chatDomainImpl.eventHandler.handleEvent(data.memberRemovedFromChannel)
        channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(1)
    }

    @Test
    fun `member notification events should update room`() = runBlocking {
        // add the member to the channel
        chatDomainImpl.repos.insertChannel(data.channel1)
        chatDomainImpl.eventHandler.handleEvent(data.memberAddedToChannelEvent)
        val cid = data.memberAddedToChannelEvent.cid
        // verify that user 2 is now part of the members
        var channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(2)
        // remove user 1
        chatDomainImpl.eventHandler.handleEvent(data.notificationRemovedFromChannel)
        channel = chatDomainImpl.repos.selectChannelWithoutMessages(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(1)
    }
}
