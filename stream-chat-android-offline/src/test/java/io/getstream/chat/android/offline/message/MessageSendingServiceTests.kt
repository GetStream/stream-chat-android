package io.getstream.chat.android.offline.message

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Test

internal class MessageSendingServiceTests {

    @Test
    fun `Given message without attachments And offline When send new message Should return message as result with right data`() =
        runBlockingTest {
            val message = randomMessage(
                cid = "",
                id = "",
                attachments = mutableListOf(),
                type = "1232423432",
                text = "text123",
                createdLocallyAt = null,
                syncStatus = SyncStatus.COMPLETED
            )
            val sut = Fixture().givenOffline().givenCid("cid1").get()

            val result = sut.sendNewMessage(message)

            Truth.assertThat(result.isSuccess).isTrue()
            Truth.assertThat(result.data().cid).isEqualTo("cid1")
            Truth.assertThat(result.data().id).isNotEmpty()
            Truth.assertThat(result.data().type).isEqualTo("regular")
            Truth.assertThat(result.data().text).isEqualTo("text123")
            Truth.assertThat(result.data().createdLocallyAt).isNotNull()
            Truth.assertThat(result.data().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        }

    private class Fixture {
        private val repositoryFacade = mock<RepositoryFacade>()
        private val chatDomainImpl = mock<ChatDomainImpl> {
            on(it.user) doReturn MutableStateFlow(randomUser())
            on(it.repos) doReturn repositoryFacade
            on { generateMessageId() } doReturn randomString()
        }
        private val channelController = mock<ChannelController>()

        fun givenOffline() = apply {
            whenever(chatDomainImpl.online) doReturn MutableStateFlow(false)
        }

        fun get(): MessageSendingService = MessageSendingService(chatDomainImpl, channelController, mock())

        fun givenCid(cid: String) = apply {
            whenever(channelController.cid) doReturn cid
        }
    }
}
