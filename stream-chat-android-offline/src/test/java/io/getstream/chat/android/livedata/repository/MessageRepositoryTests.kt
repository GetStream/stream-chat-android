package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomMessageEntity
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.repository.domain.message.MessageDao
import io.getstream.chat.android.livedata.repository.domain.message.MessageRepository
import io.getstream.chat.android.livedata.repository.domain.message.MessageRepositoryImpl
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class MessageRepositoryTests {

    private lateinit var messageDao: MessageDao

    private lateinit var sut: MessageRepository

    @BeforeEach
    fun setup() {
        messageDao = mock()
        sut = MessageRepositoryImpl(messageDao, ::randomUser)
    }

    @Test
    fun `Given 2 messages in cache When select message entities Should return message from dao and cache`() = runBlockingTest {
        val cachedMessage1 = randomMessage(id = "id1")
        val cachedMessage2 = randomMessage(id = "id2")
        sut.insertMessages(listOf(cachedMessage1, cachedMessage2), true)
        val messageEntityFromDb1 = randomMessageEntity(id = "id3")
        val messageEntityFromDb2 = randomMessageEntity(id = "id4")
        When calling messageDao.select(listOf("id3", "id4")) doReturn listOf(messageEntityFromDb1, messageEntityFromDb2)

        val result = sut.selectMessages(listOf("id1", "id2", "id3", "id4"))

        result.size shouldBeEqualTo 4
        result.any { it.id == "id1" } shouldBeEqualTo true
        result.any { it.id == "id2" } shouldBeEqualTo true
        result.any { it.id == "id3" } shouldBeEqualTo true
        result.any { it.id == "id4" } shouldBeEqualTo true
    }
}
