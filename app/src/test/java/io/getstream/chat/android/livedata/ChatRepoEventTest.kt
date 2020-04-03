package io.getstream.chat.android.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import io.getstream.chat.android.livedata.utils.waitForSetUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Verify that all events correctly update state in room
 */
@RunWith(AndroidJUnit4::class)
class ChatRepoEventTest: BaseTest() {

    @Before
    fun setup() {
        client = createClient()
        setupRepo(client, false)
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }


    @Test
    fun newMessageEvent() {
        // new messages should be stored in room
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.newMessageEvent)}
        val message = runBlocking(Dispatchers.IO) {
            repo.selectMessageEntity(data.newMessageEvent.message.id)
        }
        Truth.assertThat(message).isNotNull()
    }

    @Test
    fun initializeAndConnect() {
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.connectedEvent)}
        Truth.assertThat(repo.initialized.getOrAwaitValue()).isTrue()
        Truth.assertThat(repo.online.getOrAwaitValue()).isTrue()
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.disconnectedEvent)}
        Truth.assertThat(repo.initialized.getOrAwaitValue()).isTrue()
        Truth.assertThat(repo.online.getOrAwaitValue()).isFalse()
    }

    @Test
    fun unreadCounts() {
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.connectedEvent2)}
        Truth.assertThat(repo.channelUnreadCount.getOrAwaitValue()).isEqualTo(2)
        Truth.assertThat(repo.totalUnreadCount.getOrAwaitValue()).isEqualTo(3)
    }

    @Test
    fun messageRead() {
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.readEvent)}
        // check channel level read info
        val cid = data.readEvent.cid!!
        val channel = runBlocking(Dispatchers.IO) { repo.selectChannelEntity(cid) }
        Truth.assertThat(channel!!.reads.size).isEqualTo(1)
        val read = channel!!.reads.values.first()
        Truth.assertThat(read.userId).isEqualTo(data.readEvent.user!!.id)
    }

    @Test
    fun reactionEvent() {
        // add the message
        val messageId = data.newMessageEvent.message.id
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.newMessageEvent)}
        // add the reaction
        val secondId = data.reactionEvent.reaction!!.messageId
        Truth.assertThat(secondId).isEqualTo(messageId)
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.reactionEvent)}
        // fetch the message
        var message = runBlocking(Dispatchers.IO) {
            repo.selectMessageEntity(messageId)!!
        }
        // reaction from yourself (so it goes into ownReactions)
        Truth.assertThat(message.reactionCounts.get("like")).isEqualTo(1)
        Truth.assertThat(message.latestReactions.first().userId).isEqualTo(data.reaction1.user!!.id)
        Truth.assertThat(message.ownReactions.first().userId).isEqualTo(data.reaction1.user!!.id)

        // add a reaction from a different user, it should not go into own reaction
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.reactionEvent2)}
        message = runBlocking(Dispatchers.IO) {
            repo.selectMessageEntity(messageId)!!
        }
        Truth.assertThat(message.reactionCounts.get("like")).isEqualTo(2)
        Truth.assertThat(message.latestReactions.size).isEqualTo(2)
        Truth.assertThat(message.ownReactions.size).isEqualTo(1)
    }

    @Test
    fun channelUpdatedEvent() {
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.channelUpdatedEvent)}
        // check channel level read info
        val cid = data.channelUpdatedEvent.cid!!
        val channel = runBlocking(Dispatchers.IO) { repo.selectChannelEntity(cid)!! }
        Truth.assertThat(channel.extraData.get("color")).isEqualTo("green")
    }

    @Test
    fun memberEvent() {
        // add the member to the channel
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.memberAddedToChannelEvent)}
        val cid = data.memberAddedToChannelEvent.cid!!
        // verify that user 2 is now part of the members
        val channel = runBlocking(Dispatchers.IO) { repo.selectChannelEntity(cid)!! }
        Truth.assertThat(channel.members.size).isEqualTo(2)
    }

}
