package io.getstream.chat.android.livedata.repository

import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.livedata.dao.MessageDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.BeforeEach

@ExperimentalCoroutinesApi
internal class MessageRepositoryTests {

    private lateinit var messageDao: MessageDao

    private lateinit var sut: MessageRepository

    @BeforeEach
    fun setup() {
        messageDao = mock()
        sut = MessageRepository(messageDao)
    }
}
