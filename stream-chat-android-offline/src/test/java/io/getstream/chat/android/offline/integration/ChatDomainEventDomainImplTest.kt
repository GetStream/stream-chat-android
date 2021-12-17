package io.getstream.chat.android.offline.integration

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.querychannels.ChatEventHandler
import io.getstream.chat.android.offline.querychannels.EventHandlingResult
import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.randomUserStartWatchingEvent
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBe
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
@OptIn(ExperimentalStreamChatApi::class)
@RunWith(AndroidJUnit4::class)
internal class ChatDomainEventDomainImplTest : BaseDomainTest2() {

    @Before
    override fun setup() {
        super.setup()
        runBlocking {
            chatDomainImpl.repos.insertUsers(data.userMap.values)
            queryControllerImpl.chatEventHandler = ChatEventHandler { _, _ -> EventHandlingResult.Skip }
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
    fun `channel controller edit message event`(): Unit = coroutineTest {
        // setup the queryControllerImpl
        queryControllerImpl.query(10)

        // update the last message
        chatDomainImpl.eventHandler.handleEvent(data.messageUpdatedEvent)

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
        coroutineTest {
            queryControllerImpl.chatEventHandler = ChatEventHandler { _, _ -> EventHandlingResult.Skip }
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

    @Test
    fun `Given channel stored in DB and in controller When handle UserStartWatchingEvent Should update user value for channel in controller`() =
        coroutineTest {
            val userMember = randomUser(id = "user99")
            val updateUserMember = userMember.copy(extraData = mutableMapOf()).apply { name = "updatedName" }
            val message = randomMessage(user = userMember)
            val channel = data.channel1.copy(
                createdBy = userMember,
                messages = listOf(message),
                members = listOf(randomMember(chatDomainImpl.user.value!!), randomMember(userMember))
            )
            chatDomainImpl.repos.insertChannel(channel)
            queryControllerImpl.query()

            queryControllerImpl.channels.value.first() shouldBeEqualTo channel

            chatDomainImpl.eventHandler.handleEvent(
                randomUserStartWatchingEvent(
                    user = updateUserMember,
                    cid = channel.cid
                )
            )

            val updatedChannel = queryControllerImpl.channels.value.first()
            updatedChannel.createdBy.name shouldBeEqualTo "user99"
            updatedChannel.messages.first().user.name shouldBeEqualTo "user99"
            updatedChannel.members.any { it.user.name == "user99" } shouldBe true
        }

    @Test
    fun `Given a message was sent to a channel When the message is soft deleted Should contain deleted date`(): Unit =
        coroutineTest {
            chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent)
            chatDomainImpl.eventHandler.handleEvent(data.messageDeletedEvent)

            val messageId = data.newMessageEvent.message.id
            val message = chatDomainImpl.repos.selectMessage(messageId)
            message.shouldNotBeNull()
            message.deletedAt.shouldNotBeNull()

            val channelController = chatDomainImpl.channel(data.channel1)
            val messages = channelController.messages.value
            messages.size shouldBeEqualTo 1
        }

    @Test
    fun `Given a message was sent to a channel When the message is hard deleted Should be completely deleted`(): Unit =
        coroutineTest {
            chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent)
            chatDomainImpl.eventHandler.handleEvent(data.messageHardDeletedEvent)

            val messageId = data.newMessageEvent.message.id
            val message = chatDomainImpl.repos.selectMessage(messageId)
            message.shouldBeNull()

            val channelController = chatDomainImpl.channel(data.channel1)
            val messages = channelController.messages.value
            messages.shouldBeEmpty()
        }
}
