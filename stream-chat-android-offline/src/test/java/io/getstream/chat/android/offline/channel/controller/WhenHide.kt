package io.getstream.chat.android.offline.channel.controller

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomUser
import io.getstream.chat.android.livedata.repository.RepositoryFacade
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomDateBefore
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

@ExperimentalCoroutinesApi
internal class WhenHide {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var channelId: String
    private lateinit var channelType: String
    private val cid: String
        get() = "$channelType:$channelId"
    private lateinit var channelController: ChannelController
    private lateinit var chatClient: ChatClient
    private lateinit var channelClient: ChannelClient
    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var repos: RepositoryFacade

    @BeforeEach
    fun setUp() {
        channelId = randomString()
        channelType = randomString()
        channelClient = mock()
        repos = mock()
        chatClient = mock {
            whenever(mock.channel(channelType, channelId)) doReturn channelClient
        }
        chatDomainImpl = mock {
            whenever(mock.scope) doReturn testCoroutines.scope
            whenever(mock.currentUser) doReturn randomUser()
            whenever(mock.repos) doReturn repos
        }
        channelController = ChannelController(
            channelType = channelType,
            channelId = channelId,
            client = chatClient,
            domainImpl = chatDomainImpl,
        )
    }

    @Test
    fun `Should mark channel as hidden`() = runBlockingTest {
        val response = Result(Unit)
        whenever(channelClient.hide(any())).thenReturn(TestCall(response))

        val result = channelController.hide(clearHistory = false)

        Truth.assertThat(response).isEqualTo(result)
        Truth.assertThat(channelController.hidden.value).isTrue()
    }

    @Test
    fun `Given failed response Should return server error`() =
        runBlockingTest {
            val response = Result<Unit>(error = ChatError())
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = channelController.hide(clearHistory = false)

            Truth.assertThat(response).isEqualTo(result)
        }

    @Test
    fun `Given successful response And channel without clearing history Should update channel in database`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = channelController.hide(clearHistory = false)

            Truth.assertThat(response).isEqualTo(result)
            verify(channelClient).hide(clearHistory = false)
            verify(repos).setHiddenForChannel(cid = cid, hidden = true)
        }

    @Test
    fun `Given successful response And channel with clearing history Should hide messages sent before`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = channelController.hide(clearHistory = true)

            Truth.assertThat(response).isEqualTo(result)
            Truth.assertThat(channelController.hideMessagesBefore).isNotNull()
        }

    @Test
    fun `Given successful response And channel with clearing history Should update channel in database`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = channelController.hide(clearHistory = true)

            Truth.assertThat(response).isEqualTo(result)
            verify(channelClient).hide(clearHistory = true)
            Truth.assertThat(channelController.hideMessagesBefore).isNotNull()
            verify(repos).setHiddenForChannel(
                cid = cid,
                hidden = true,
                hideMessagesBefore = channelController.hideMessagesBefore!!,
            )
        }

    @Test
    fun `Given successful response And channel with clearing history Should delete hidden messages from database`() =
        runBlockingTest {
            val response = Result(Unit)
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))

            val result = channelController.hide(clearHistory = true)

            Truth.assertThat(response).isEqualTo(result)
            verify(channelClient).hide(clearHistory = true)
            Truth.assertThat(channelController.hideMessagesBefore).isNotNull()
            verify(repos).deleteChannelMessagesBefore(
                cid = cid,
                hideMessagesBefore = channelController.hideMessagesBefore!!,
            )
        }

    @Test
    fun `Given successful response And channel with clearing history Should clear messages sent before channel was hidden`() =
        runBlockingTest {
            val now = Date().time
            val response = Result(Unit)
            val messageSentAfterChannelIsCleared = randomMessage(createdAt = Date(now + 1000))
            whenever(channelClient.hide(any())).thenReturn(TestCall(response))
            channelController.apply {
                upsertMessage(randomMessage(createdAt = randomDateBefore(Date().time)))
                upsertMessage(randomMessage(createdAt = randomDateBefore(Date().time)))
                upsertMessage(randomMessage(createdAt = randomDateBefore(Date().time)))
                upsertMessage(randomMessage(createdAt = randomDateBefore(Date().time)))
                upsertMessage(messageSentAfterChannelIsCleared)
            }

            val result = channelController.hide(clearHistory = true)

            Truth.assertThat(response).isEqualTo(result)
            val messages = channelController.unfilteredMessages.first()
            Truth.assertThat(messages.size).isEqualTo(1)
            Truth.assertThat(messages.any { it == messageSentAfterChannelIsCleared }).isTrue()
        }
}
