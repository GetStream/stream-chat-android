package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.test.randomMessage
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
internal class ThreadQueryListenerDatabaseTest {

    private val messageRepository: MessageRepository = mock()
    private val userRepository: UserRepository = mock()

    private val threadQueryListenerDatabase: ThreadQueryListenerDatabase = ThreadQueryListenerDatabase(
        messageRepository, userRepository
    )

    @Test
    fun `given the response is successful, database should be updated`() = runTest {
        val message = randomMessage()
        val messageList = listOf(message)

        threadQueryListenerDatabase.onGetRepliesResult(Result.success(messageList), randomString(), randomInt())

        verify(userRepository).insertUsers(any())
        verify(messageRepository).insertMessages(messageList, false)
    }

    @Test
    fun `given the response is failure, database should NOT be updated`() = runTest {
        val message = randomMessage()
        val messageList = listOf(message)

        threadQueryListenerDatabase.onGetRepliesResult(Result.error(ChatError()), randomString(), randomInt())

        verify(userRepository, never()).insertUsers(any())
        verify(messageRepository, never()).insertMessages(messageList, false)
    }
}
