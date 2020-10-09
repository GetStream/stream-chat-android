package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.request.AnyChannelPaginationRequest
import io.getstream.chat.android.livedata.utils.calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class MessageRepositoryTest : BaseDomainTest() {
    val repo by lazy { chatDomainImpl.repos.messages }

    @Test
    fun testInsertAndRead() = runBlocking(Dispatchers.IO) {
        repo.insert(data.message1)
        val message = repo.select(data.message1.id, data.userMap)
        // ignore the channel field, we don't have that information at the message repository level
        Truth.assertThat(message).isEqualTo(data.message1)
    }

    @Test
    fun testMessageObject() = runBlocking(Dispatchers.IO) {
        val messagea = Message(text = "hi").apply { reactionCounts = mutableMapOf("like" to 10) }
        val messageb = Message(text = "hi")
        Truth.assertThat(messagea).isNotEqualTo(messageb)

        val message1 = data.createMessage()
        val message2 = message1.copy()
        Truth.assertThat(message1).isEqualTo(message2)
    }

    @Test
    fun testMessageObjectWithExtraData() = runBlocking(Dispatchers.IO) {
        val extra = mutableMapOf("int" to 10, "string" to "green", "list" to listOf("a", "b"))
        val messageIn = data.createMessage().apply { extraData = extra; id = "testMessageObjectWithExtraData" }
        repo.insert(messageIn, true)
        val messageOut = repo.select(messageIn.id, data.userMap)
        Truth.assertThat(messageOut!!.extraData).isEqualTo(extra)
    }

    @Test
    fun testUpdate() = runBlocking(Dispatchers.IO) {
        repo.insert(data.message1, true)
        repo.insert(data.message1Updated, true)

        val message = repo.select(data.message1Updated.id, data.userMap)

        Truth.assertThat(message).isEqualTo(data.message1Updated)
        Truth.assertThat(repo.messageCache.size()).isEqualTo(1)
    }

    @Test
    fun testSyncNeeded() = runBlocking(Dispatchers.IO) {
        val oldDate = calendar(2019, 11, 1)
        val message1 =
            data.createMessage().apply {
                id = "testSyncNeeded-1"; text = "yoyo"; syncStatus = SyncStatus.SYNC_NEEDED
                createdAt = oldDate
            }
        val message2 = data.createMessage()
            .apply { id = "testSyncNeeded-2"; text = "hi123"; syncStatus = SyncStatus.FAILED_PERMANENTLY }
        val message3 = data.createMessage()
            .apply { id = "testSyncNeeded-3"; text = "hi123"; syncStatus = SyncStatus.SYNC_NEEDED }
        repo.insert(listOf(message1, message2, message3), true)

        var messages = repo.selectSyncNeeded()

        Truth.assertThat(messages.size).isEqualTo(2)
        // verify that the old item is returned first
        Truth.assertThat(messages.first().createdAt).isEqualTo(oldDate)
        Truth.assertThat(messages.first().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)

        messages = repo.retryMessages()
        Truth.assertThat(messages.size).isEqualTo(2)
        Truth.assertThat(messages.first().syncStatus).isEqualTo(SyncStatus.COMPLETED)

        messages = repo.selectSyncNeeded()
        Truth.assertThat(messages.size).isEqualTo(0)
    }

    @Test
    fun testSelectMessagesForChannel() = runBlocking(Dispatchers.IO) {
        val message1 = data.createMessage().apply {
            id = "testSelectMessagesForChannel1"; text = "message1"; syncStatus = SyncStatus.SYNC_NEEDED; user = data.user1; createdAt =
                calendar(2019, 11, 1)
        }
        val message2 = data.createMessage().apply {
            id = "testSelectMessagesForChannel2"; text = "hi123"; syncStatus = SyncStatus.FAILED_PERMANENTLY; user =
                data.user1; createdAt = calendar(2019, 10, 1)
        }
        val message3 = data.createMessage().apply {
            id = "testSelectMessagesForChannel3"; text = "hi123123"; syncStatus = SyncStatus.FAILED_PERMANENTLY; user =
                data.user1; createdAt = calendar(2019, 9, 1)
        }
        repo.insert(listOf(message1, message2, message3), true)

        // this should select the first message
        val pagination = AnyChannelPaginationRequest(1)
        pagination.setFilter(Pagination.GREATER_THAN, message2.id)
        var messages = repo.selectMessagesForChannel(data.message1.cid, pagination)
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages.first().id).isEqualTo(message1.id)
        // this should select the third message
        pagination.setFilter(Pagination.LESS_THAN, message2.id)
        messages = repo.selectMessagesForChannel(data.message1.cid, pagination)
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages.first().id).isEqualTo(message3.id)

        // verify that LTE & GTE also work
        pagination.messageLimit = 2
        // filter on 2 and older
        pagination.setFilter(Pagination.LESS_THAN_OR_EQUAL, message2.id)
        // should return message 2 and message 3, with message 3 (the oldest message as the first element)
        messages = repo.selectMessagesForChannel(data.message1.cid, pagination)
        Truth.assertThat(messages.size).isEqualTo(2)
        Truth.assertThat(messages.first().id).isEqualTo(message3.id)
        // request 2 and newer, message 2 (the oldest) should be first
        pagination.setFilter(Pagination.GREATER_THAN_OR_EQUAL, message2.id)
        messages = repo.selectMessagesForChannel(data.message1.cid, pagination)
        Truth.assertThat(messages.size).isEqualTo(2)
        Truth.assertThat(messages.first().id).isEqualTo(message2.id)
    }
}
