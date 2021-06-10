package io.getstream.chat.android.offline.message

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

internal class MessageSendingServiceTests {

    @Test
    fun `Given message without attachments And offline When send new message`() = runBlockingTest {
        val message = randomMessage(attachments = mutableListOf())
        val sut = Fixture().givenOffline().get()

        sut.sendNewMessage(message)
    }

    private class Fixture {
        private val repositoryFacade = mock<RepositoryFacade>()
        private val chatDomainImpl = mock<ChatDomainImpl> {
            on(it.user) doReturn MutableStateFlow(randomUser())
            on(it.repos) doReturn repositoryFacade
        }
        private val channelController = mock<ChannelController>()

        fun givenOffline() = apply {
            whenever(chatDomainImpl.online) doReturn MutableStateFlow(false)
        }

        fun get(): MessageSendingService = MessageSendingService(chatDomainImpl, channelController, mock())
    }
}
