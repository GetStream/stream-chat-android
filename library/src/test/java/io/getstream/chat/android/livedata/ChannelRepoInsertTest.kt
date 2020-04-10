package io.getstream.chat.android.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import io.getstream.chat.android.livedata.utils.waitForSetUser
import kotlinx.coroutines.*
import kotlinx.coroutines.test.runBlockingTest
import org.junit.*
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLooper
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class ChannelRepoInsertTest: BaseTest() {

    @Before
    fun setup() {
        client = createClient()
        setupRepo(client, true)
        val handler = CoroutineExceptionHandler { _, exception ->
            println("Caught $exception")
        }
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }

    @Test
    fun reactionStorage() = runBlocking(Dispatchers.IO) {
        val reactionEntity = ReactionEntity(data.message1.id, data.user1.id, data.reaction1.type)
        reactionEntity.syncStatus = SyncStatus.SYNC_NEEDED
        repo.insertReactionEntity(reactionEntity)
        val results = repo.retryReactions()
        Truth.assertThat(results.size).isEqualTo(1)
    }

    // TODO: converter/repo test suite

    @Test
    fun sendReaction() = runBlocking(Dispatchers.IO) {

        // TODO: Mock socket and mock client
        // ensure the message exists
        repo._createChannel(data.channel1)
        channelRepo._sendMessage(data.message1)
        repo.setOffline()
        repo.insertChannel(data.channel1)
        channelRepo.upsertMessage(data.message1)
        // send the reaction while offline
        channelRepo._sendReaction(data.reaction1)
        var reactionEntity = repo.selectReactionEntity(data.message1.id, data.user1.id, data.reaction1.type)
        Truth.assertThat(reactionEntity!!.syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        repo.setOnline()
        val reactionEntities = repo.retryReactions()
        Truth.assertThat(reactionEntities.size).isEqualTo(1)
        reactionEntity = repo.selectReactionEntity(data.message1.id, data.user1.id, "like")
        Truth.assertThat(reactionEntity!!.syncStatus).isEqualTo(SyncStatus.SYNCED)

    }

    @Test
    fun deleteReaction() = runBlocking(Dispatchers.IO) {
        repo.setOffline()

        channelRepo._sendReaction(data.reaction1)
        channelRepo._deleteReaction(data.reaction1)

        val reaction = repo.selectReactionEntity(data.message1.id, data.user1.id, data.reaction1.type)
        Truth.assertThat(reaction!!.syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        Truth.assertThat(reaction!!.deletedAt).isNotNull()

        val reactions = repo.retryReactions()
        Truth.assertThat(reactions.size).isEqualTo(1)
    }

    @Test
    @Ignore("Needs a mock, message id already exists")
    fun sendMessage() = runBlocking(Dispatchers.IO){
        // send the message while offline
        repo._createChannel(data.channel1)
        repo.setOffline()
        channelRepo._sendMessage(data.message1)
        // get the message and channel state both live and offline versions
        var roomChannel = repo.selectChannelEntity(data.message1.channel.cid)
        var liveChannel = channelRepo.channel.getOrAwaitValue()
        var roomMessages = repo.selectMessagesForChannel(data.message1.channel.cid)
        var liveMessages = channelRepo.messages.getOrAwaitValue()

        Truth.assertThat(liveMessages.size).isEqualTo(1)
        Truth.assertThat(liveMessages[0].syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        Truth.assertThat(roomMessages.size).isEqualTo(1)
        Truth.assertThat(roomMessages[0].syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        // verify the message is stored in room, and set to retry
        // verify the channel is updated as well (lastMessage at and lastMessageAt)
        Truth.assertThat(liveChannel.lastMessageAt).isEqualTo(data.message1.createdAt)
        Truth.assertThat(roomChannel!!.lastMessageAt).isEqualTo(data.message1.createdAt)

        var messageEntities = repo.retryMessages()
        Truth.assertThat(messageEntities.size).isEqualTo(1)

        // now we go online and retry, after the retry all state should be updated
        repo.setOnline()
        messageEntities = repo.retryMessages()
        Truth.assertThat(messageEntities.size).isEqualTo(1)

        roomMessages = repo.selectMessagesForChannel(data.message1.channel.cid)
        liveMessages = channelRepo.messages.getOrAwaitValue()
        Truth.assertThat(liveMessages[0].syncStatus).isEqualTo(SyncStatus.SYNCED)
        Truth.assertThat(roomMessages[0].syncStatus).isEqualTo(SyncStatus.SYNCED)

    }

    @Test
    fun clean() {
        // clean should remove old typing indicators
        channelRepo.setTyping(data.user1.id, data.user1TypingStartedOld)
        channelRepo.setTyping(data.user2.id, data.user2TypingStarted)

        Truth.assertThat(channelRepo.typing.getOrAwaitValue().size).isEqualTo(2)
        channelRepo.clean()
        Truth.assertThat(channelRepo.typing.getOrAwaitValue().size).isEqualTo(1)
    }

    @Test
    fun insertQuery() = runBlocking(Dispatchers.IO){
        val filter = Filters.eq("type", "messaging")
        val sort = null
        val query = QueryChannelsEntity(filter, sort)
        query.channelCIDs = listOf("messaging:123", "messaging:234").toMutableList()
        repo.insertQuery(query)
    }

    @Test
    fun insertReaction() = runBlocking(Dispatchers.IO) {
        // check DAO layer and converters
        val reactionEntity = ReactionEntity(data.reaction1)
        repo.insertReactionEntity(reactionEntity)
        val reactionEntity2 = repo.selectReactionEntity(reactionEntity.messageId, reactionEntity.userId, reactionEntity.type)
        Truth.assertThat(reactionEntity2).isEqualTo(reactionEntity)
        Truth.assertThat(reactionEntity2!!.extraData).isNotNull()
        // verify conversion logic is ok
        val userMap = mutableMapOf(data.user1.id to data.user1)
        val reactionConverted = reactionEntity2!!.toReaction(userMap)
        Truth.assertThat(reactionConverted).isEqualTo(data.reaction1)
    }


    @Test
    fun typing() = runBlocking(Dispatchers.IO){
        // second typing keystroke after each other should not resend the typing event
        Truth.assertThat(channelRepo.keystroke()).isTrue()
        Truth.assertThat(channelRepo.keystroke()).isFalse()
        sleep(3001)
        Truth.assertThat(channelRepo.keystroke()).isTrue()
    }

    @Test
    fun markRead() = runBlocking(Dispatchers.IO){
        // ensure there is at least one message
        channelRepo.upsertMessage(data.message1)
        Truth.assertThat(channelRepo.markRead()).isTrue()
        Truth.assertThat(channelRepo.markRead()).isFalse()

    }


}