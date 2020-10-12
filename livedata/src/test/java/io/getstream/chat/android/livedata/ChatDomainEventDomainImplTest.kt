package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verify that all events correctly update state in room
 */
@RunWith(AndroidJUnit4::class)
internal class ChatDomainEventDomainImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun defaultConfig() = runBlocking(Dispatchers.IO) {
        val config = chatDomainImpl.getChannelConfig("missing")
        Truth.assertThat(config).isEqualTo((chatDomainImpl.defaultConfig))
    }

    @Test
    fun newMessageEvent() = runBlocking(Dispatchers.IO) {
        // new messages should be stored in room
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent)
        val message = chatDomainImpl.repos.messages.select(data.newMessageEvent.message.id)
        Truth.assertThat(message).isNotNull()
    }

    @Test
    fun newMessageNotificationEvent() = runBlocking(Dispatchers.IO) {
        // new messages should be stored in room
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        val message = chatDomainImpl.repos.messages.select(data.newMessageEvent.message.id)
        Truth.assertThat(message).isNotNull()
    }

    @Test
    fun addedToChannel() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.eventHandler.handleEvent(data.notificationAddedToChannel2Event)
        val channel = chatDomainImpl.repos.channels.select(data.notificationAddedToChannel2Event.channel.cid)
        Truth.assertThat(channel).isNotNull()
    }

    @Test
    fun truncate() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        chatDomainImpl.eventHandler.handleEvent(data.channelTruncatedEvent)
        val message = chatDomainImpl.repos.messages.select(data.newMessageEvent.message.id)
        Truth.assertThat(message).isNull()
        val channelController = chatDomainImpl.channel(data.channel1)
        val messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages).isEmpty()
    }

    @Test
    fun notificationTruncate() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        chatDomainImpl.eventHandler.handleEvent(data.notificationChannelTruncated)
        val message = chatDomainImpl.repos.messages.select(data.newMessageEvent.message.id)
        Truth.assertThat(message).isNull()
        val channelController = chatDomainImpl.channel(data.channel1)
        val messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages).isEmpty()
    }

    @Test
    fun channelDelete() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.eventHandler.handleEvent(data.newMessageEventNotification)
        chatDomainImpl.eventHandler.handleEvent(data.channelDeletedEvent)
        val message = chatDomainImpl.repos.messages.select(data.newMessageEvent.message.id)
        val channel = chatDomainImpl.repos.channels.select(data.channel1.cid)
        Truth.assertThat(message).isNull()
        Truth.assertThat(channel!!.deletedAt).isEqualTo(data.channelDeletedEvent.createdAt)
        val channelController = chatDomainImpl.channel(data.channel1)
        val messages = channelController.messages.getOrAwaitValue()
        val channelData = channelController.channelData.getOrAwaitValue()
        Truth.assertThat(messages).isEmpty()
        Truth.assertThat(channelData.deletedAt).isEqualTo(data.channelDeletedEvent.createdAt)
    }

    @Test
    fun initializeAndConnect() {
        runBlocking(Dispatchers.IO) { chatDomainImpl.eventHandler.handleEvent(data.connectedEvent) }
        Truth.assertThat(chatDomainImpl.initialized.getOrAwaitValue()).isTrue()
        Truth.assertThat(chatDomainImpl.online.getOrAwaitValue()).isTrue()
        runBlocking(Dispatchers.IO) { chatDomainImpl.eventHandler.handleEvent(data.disconnectedEvent) }
        Truth.assertThat(chatDomainImpl.initialized.getOrAwaitValue()).isTrue()
        Truth.assertThat(chatDomainImpl.online.getOrAwaitValue()).isFalse()
    }

    @Test
    fun loadAndState() = runBlocking(Dispatchers.IO) {
        data.user1.extraData = mutableMapOf("snack" to "icecream")
        chatDomainImpl.repos.users.insertMe(data.user1)
        val me = chatDomainImpl.repos.users.selectMe()
        Truth.assertThat(me).isNotNull()
        Truth.assertThat(me?.id).isEqualTo("broad-lake-3")
    }

    @Ignore("Failing for unknown reasons")
    @Test
    fun unreadCounts() {
        runBlocking(Dispatchers.IO) { chatDomainImpl.eventHandler.handleEvent(data.connectedEvent2) }
        Truth.assertThat(chatDomainImpl.channelUnreadCount.getOrAwaitValue()).isEqualTo(2)
        Truth.assertThat(chatDomainImpl.totalUnreadCount.getOrAwaitValue()).isEqualTo(3)
    }

    @Test
    fun mutUsers() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.eventHandler.handleEvent(data.notificationMutesUpdated)
        Truth.assertThat(chatDomainImpl.muted.getOrAwaitValue()).isEqualTo(data.notificationMutesUpdated.me.mutes)
    }

    @Test
    fun messageRead() {
        runBlocking(Dispatchers.IO) { chatDomainImpl.repos.channels.insertChannel(data.channel1) }
        runBlocking(Dispatchers.IO) { chatDomainImpl.eventHandler.handleEvent(data.readEvent) }
        // check channel level read info
        val cid = data.readEvent.cid
        val channel = runBlocking(Dispatchers.IO) { chatDomainImpl.repos.channels.select(cid) }
        Truth.assertThat(channel!!.reads.size).isEqualTo(1)
        val read = channel.reads.values.first()
        Truth.assertThat(read.userId).isEqualTo(data.readEvent.user.id)
    }

    @Test
    fun reactionEvent() {
        // add the message
        val messageId = data.newMessageEvent.message.id
        runBlocking(Dispatchers.IO) { chatDomainImpl.eventHandler.handleEvent(data.newMessageEvent) }
        // add the reaction
        val secondId = data.reactionEvent.reaction.messageId
        Truth.assertThat(secondId).isEqualTo(messageId)
        runBlocking(Dispatchers.IO) { chatDomainImpl.eventHandler.handleEvent(data.reactionEvent) }
        // fetch the message
        var message = runBlocking(Dispatchers.IO) {
            chatDomainImpl.repos.messages.select(messageId)!!
        }
        // reaction from yourself (so it goes into ownReactions)
        Truth.assertThat(message.reactionCounts["like"]).isEqualTo(1)
        Truth.assertThat(message.reactionScores["like"]).isEqualTo(10)

        Truth.assertThat(message.latestReactions.first().userId).isEqualTo(data.reaction1.user!!.id)
        Truth.assertThat(message.ownReactions.first().userId).isEqualTo(data.reaction1.user!!.id)

        // add a reaction from a different user, it should not go into own reaction
        runBlocking(Dispatchers.IO) { chatDomainImpl.eventHandler.handleEvent(data.reactionEvent2) }
        message = runBlocking(Dispatchers.IO) {
            chatDomainImpl.repos.messages.select(messageId)!!
        }
        Truth.assertThat(message.reactionCounts["like"]).isEqualTo(2)
        Truth.assertThat(message.latestReactions.size).isEqualTo(2)
        Truth.assertThat(message.ownReactions.size).isEqualTo(1)
    }

    @Test
    fun channelUpdatedEvent() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.eventHandler.handleEvent(data.channelUpdatedEvent)
        // check channel level read info
        val cid = data.channelUpdatedEvent.cid
        val channel = chatDomainImpl.repos.channels.select(cid)!!
        Truth.assertThat(channel.extraData["color"]).isEqualTo("green")
    }

    @Test
    fun memberAddAndRemoveEvent() = runBlocking(Dispatchers.IO) {
        // add the member to the channel
        chatDomainImpl.repos.channels.insertChannel(data.channel1)
        chatDomainImpl.eventHandler.handleEvent(data.memberAddedToChannelEvent)
        val cid = data.memberAddedToChannelEvent.cid
        // verify that user 2 is now part of the members
        var channel = chatDomainImpl.repos.channels.select(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(2)
        chatDomainImpl.eventHandler.handleEvent(data.memberRemovedFromChannel)
        channel = chatDomainImpl.repos.channels.select(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(1)
    }

    @Test
    fun memberAddAndNotificationRemoveEvent() = runBlocking(Dispatchers.IO) {
        // add the member to the channel
        chatDomainImpl.repos.channels.insertChannel(data.channel1)
        chatDomainImpl.eventHandler.handleEvent(data.memberAddedToChannelEvent)
        val cid = data.memberAddedToChannelEvent.cid
        // verify that user 2 is now part of the members
        var channel = chatDomainImpl.repos.channels.select(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(2)
        // remove user 1
        chatDomainImpl.eventHandler.handleEvent(data.notificationRemovedFromChannel)
        channel = chatDomainImpl.repos.channels.select(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(1)
    }
}
