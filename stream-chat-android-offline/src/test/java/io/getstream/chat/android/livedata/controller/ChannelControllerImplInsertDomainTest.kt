package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
internal class ChannelControllerImplInsertDomainTest : BaseConnectedIntegrationTest() {

    @Test
    fun reactionStorage() = runBlocking {
        chatDomainImpl.repos.insertMessage(data.message1)
        val reaction = data.reaction1.copy()
        reaction.syncStatus = SyncStatus.SYNC_NEEDED
        chatDomainImpl.repos.insertReaction(reaction)
        val results = chatDomainImpl.retryReactions()
        Truth.assertThat(results.size).isEqualTo(1)
    }

    @Test
    fun sendReaction() = runBlocking {
        // ensure the message exists
        chatDomainImpl.createChannel(data.channel1)
        val message1 = data.createMessage()
        channelControllerImpl.sendMessage(message1)
        val reaction1 =
            Reaction(message1.id, "like", 1).apply { user = data.user1; userId = data.user1.id }
        chatDomainImpl.setOffline()
        chatDomainImpl.repos.insertChannel(data.channel1)
        channelControllerImpl.upsertMessage(message1)
        // send the reaction while offline
        channelControllerImpl.sendReaction(reaction1, enforceUnique = false)
        var reactionEntity =
            chatDomainImpl.repos.selectUserReactionsToMessageByType(message1.id, data.user1.id, data.reaction1.type)
        Truth.assertThat(reactionEntity!!.syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        chatDomainImpl.setOnline()
        val reactionEntities = chatDomainImpl.retryReactions()
        Truth.assertThat(reactionEntities.size).isEqualTo(1)
        reactionEntity = chatDomainImpl.repos.selectUserReactionsToMessageByType(message1.id, data.user1.id, "like")
        Truth.assertThat(reactionEntity!!.syncStatus).isEqualTo(SyncStatus.COMPLETED)
    }

    @Test
    @Ignore("This test is not stable due to usage of setOffline")
    fun deleteReaction() = runBlocking {
        chatDomainImpl.setOffline()

        channelControllerImpl.sendReaction(data.reaction1, enforceUnique = false)
        channelControllerImpl.deleteReaction(data.reaction1)

        val reaction =
            chatDomainImpl.repos.selectUserReactionsToMessageByType(data.message1.id, data.user1.id, data.reaction1.type)
        Truth.assertThat(reaction!!.syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        Truth.assertThat(reaction.deletedAt).isNotNull()

        val reactions = chatDomainImpl.retryReactions()
        Truth.assertThat(reactions.size).isEqualTo(1)
    }

    @Test
    @Ignore("Needs a mock, message id already exists")
    fun sendMessage() = runBlocking {
        // send the message while offline
        chatDomainImpl.createChannel(data.channel1)
        chatDomainImpl.setOffline()
        val message1 = data.createMessage()
        channelControllerImpl.sendMessage(message1)
        // get the message and channel state both live and offline versions
        val roomChannel = chatDomainImpl.repos.selectChannelWithoutMessages(message1.cid)
        val liveChannel = channelControllerImpl.toChannel()
        var roomMessages = chatDomainImpl.repos.selectMessagesForChannel(
            message1.cid,
            AnyChannelPaginationRequest().apply { messageLimit = 10 }
        )
        var liveMessages = channelControllerImpl.messages.getOrAwaitValue()

        Truth.assertThat(liveMessages.size).isEqualTo(1)
        Truth.assertThat(liveMessages[0].syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        Truth.assertThat(roomMessages.size).isEqualTo(1)
        Truth.assertThat(roomMessages[0].syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        // verify the message is stored in room, and set to retry
        // verify the channel is updated as well (lastMessage at and lastMessageAt)
        Truth.assertThat(liveChannel.lastMessageAt).isEqualTo(message1.createdAt)
        Truth.assertThat(roomChannel!!.lastMessageAt).isEqualTo(message1.createdAt)

        var messages = chatDomainImpl.retryMessages()
        Truth.assertThat(messages.size).isEqualTo(1)

        // now we go online and retry, after the retry all state should be updated
        chatDomainImpl.setOnline()
        messages = chatDomainImpl.retryMessages()
        Truth.assertThat(messages.size).isEqualTo(1)

        roomMessages = chatDomainImpl.repos.selectMessagesForChannel(
            message1.cid,
            AnyChannelPaginationRequest().apply { messageLimit = 10 }
        )
        liveMessages = channelControllerImpl.messages.getOrAwaitValue()
        Truth.assertThat(liveMessages[0].syncStatus).isEqualTo(SyncStatus.COMPLETED)
        Truth.assertThat(roomMessages[0].syncStatus).isEqualTo(SyncStatus.COMPLETED)
    }

    @Test
    fun clean() {
        // clean should remove old typing indicators
        channelControllerImpl.setTyping(data.user3.id, data.user3TypingStartedOld)
        channelControllerImpl.setTyping(data.user2.id, data.user2TypingStarted)

        Truth.assertThat(channelControllerImpl.typing.getOrAwaitValue().users.size).isEqualTo(2)
        channelControllerImpl.clean()
        Truth.assertThat(channelControllerImpl.typing.getOrAwaitValue().users.size).isEqualTo(1)
    }

    @Test
    fun insertQuery() = runBlocking {
        val filter = Filters.eq("type", "messaging")
        val query = QueryChannelsSpec(filter, QuerySort())
        query.cids = listOf("messaging:123", "messaging:234")
        chatDomainImpl.repos.queryInsert(query)
    }

    @Test
    fun insertReaction() = runBlocking {
        // check DAO layer and converters
        chatDomainImpl.repos.insertMessage(data.message1)
        val reaction = data.reaction1.copy()
        chatDomainImpl.repos.insertReaction(reaction)
        val reaction2 = chatDomainImpl.repos.selectUserReactionsToMessageByType(
            reaction.messageId,
            reaction.userId,
            reaction.type
        )
        Truth.assertThat(reaction2).isEqualTo(reaction)
        Truth.assertThat(reaction2!!.extraData).isNotNull()
    }

    @Test
    fun typing() = runBlocking {
        // second typing keystroke after each other should not resend the typing event
        Truth.assertThat(chatDomainImpl.useCases.keystroke(data.channel1.cid).execute().data()).isTrue()
        Truth.assertThat(chatDomainImpl.useCases.keystroke(data.channel1.cid).execute().data())
            .isFalse()

        sleep(3001)
        Truth.assertThat(chatDomainImpl.useCases.keystroke(data.channel1.cid).execute().data()).isTrue()
    }

    @Test
    fun markRead() = runBlocking {
        // ensure there is at least one message
        channelControllerImpl.upsertMessage(data.message1)
        Truth.assertThat(channelControllerImpl.markRead()).isTrue()
        Truth.assertThat(channelControllerImpl.markRead()).isFalse()
    }
}
