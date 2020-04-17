package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Verify that all events correctly update state in room
 */
@RunWith(AndroidJUnit4::class)
class ChatDomainEventDomainTest: BaseDomainTest() {

    @Before
    fun setup() {
        client = createClient()
        setupChatDomain(client, false)
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }


    @Test
    fun newMessageEvent() {
        // new messages should be stored in room
        runBlocking(Dispatchers.IO) {chatDomain.eventHandler.handleEvent(data.newMessageEvent)}
        val message = runBlocking(Dispatchers.IO) {
            chatDomain.repos.messages.selectMessageEntity(data.newMessageEvent.message.id)
        }
        Truth.assertThat(message).isNotNull()
    }

    @Test
    fun initializeAndConnect() {
        runBlocking(Dispatchers.IO) {chatDomain.eventHandler.handleEvent(data.connectedEvent)}
        Truth.assertThat(chatDomain.initialized.getOrAwaitValue()).isTrue()
        Truth.assertThat(chatDomain.online.getOrAwaitValue()).isTrue()
        runBlocking(Dispatchers.IO) {chatDomain.eventHandler.handleEvent(data.disconnectedEvent)}
        Truth.assertThat(chatDomain.initialized.getOrAwaitValue()).isTrue()
        Truth.assertThat(chatDomain.online.getOrAwaitValue()).isFalse()
    }

    @Test
    fun unreadCounts() {
        runBlocking(Dispatchers.IO) {chatDomain.eventHandler.handleEvent(data.connectedEvent2)}
        Truth.assertThat(chatDomain.channelUnreadCount.getOrAwaitValue()).isEqualTo(2)
        Truth.assertThat(chatDomain.totalUnreadCount.getOrAwaitValue()).isEqualTo(3)
    }

    @Test
    fun messageRead() {
        runBlocking(Dispatchers.IO) {chatDomain.repos.channels.insert(data.channel1)}
        runBlocking(Dispatchers.IO) {chatDomain.eventHandler.handleEvent(data.readEvent)}
        // check channel level read info
        val cid = data.readEvent.cid!!
        val channel = runBlocking(Dispatchers.IO) { chatDomain.repos.channels.select(cid) }
        Truth.assertThat(channel!!.reads.size).isEqualTo(1)
        val read = channel!!.reads.values.first()
        Truth.assertThat(read.userId).isEqualTo(data.readEvent.user!!.id)
    }

    @Test
    fun reactionEvent() {
        // add the message
        val messageId = data.newMessageEvent.message.id
        runBlocking(Dispatchers.IO) {chatDomain.eventHandler.handleEvent(data.newMessageEvent)}
        // add the reaction
        val secondId = data.reactionEvent.reaction!!.messageId
        Truth.assertThat(secondId).isEqualTo(messageId)
        runBlocking(Dispatchers.IO) {chatDomain.eventHandler.handleEvent(data.reactionEvent)}
        // fetch the message
        var message = runBlocking(Dispatchers.IO) {
            chatDomain.repos.messages.selectMessageEntity(messageId)!!
        }
        // reaction from yourself (so it goes into ownReactions)
        Truth.assertThat(message.reactionCounts.get("like")).isEqualTo(1)
        Truth.assertThat(message.latestReactions.first().userId).isEqualTo(data.reaction1.user!!.id)
        Truth.assertThat(message.ownReactions.first().userId).isEqualTo(data.reaction1.user!!.id)

        // add a reaction from a different user, it should not go into own reaction
        runBlocking(Dispatchers.IO) {chatDomain.eventHandler.handleEvent(data.reactionEvent2)}
        message = runBlocking(Dispatchers.IO) {
            chatDomain.repos.messages.selectMessageEntity(messageId)!!
        }
        Truth.assertThat(message.reactionCounts.get("like")).isEqualTo(2)
        Truth.assertThat(message.latestReactions.size).isEqualTo(2)
        Truth.assertThat(message.ownReactions.size).isEqualTo(1)
    }

    @Test
    fun channelUpdatedEvent() = runBlocking(Dispatchers.IO) {
        chatDomain.eventHandler.handleEvent(data.channelUpdatedEvent)
        // check channel level read info
        val cid = data.channelUpdatedEvent.cid!!
        val channel = chatDomain.repos.channels.select(cid)!!
        Truth.assertThat(channel.extraData.get("color")).isEqualTo("green")
    }

    @Test
    fun memberEvent() = runBlocking(Dispatchers.IO) {
        // add the member to the channel
        chatDomain.repos.channels.insert(data.channel1)
        chatDomain.eventHandler.handleEvent(data.memberAddedToChannelEvent)
        val cid = data.memberAddedToChannelEvent.cid!!
        // verify that user 2 is now part of the members
        val channel = chatDomain.repos.channels.select(cid)!!
        Truth.assertThat(channel.members.size).isEqualTo(2)
    }

}
