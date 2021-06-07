package io.getstream.chat.android.offline.usecase

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.stub
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class MarkReadTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var chatDomain: ChatDomainImpl
    private lateinit var client: ChatClient
    private lateinit var channelController: ChannelController
    private lateinit var cid: String
    private lateinit var channelType: String
    private lateinit var channelId: String

    private lateinit var sut: MarkRead

    @BeforeEach
    fun init() {
        client = mock()
        chatDomain = mock {
            on { scope } doReturn testCoroutines.scope
        }
        sut = MarkRead(chatDomain)
        channelController = mock()
        setupChannelController(channelController)
    }

    private fun setupChannelController(channelController: ChannelController) {
        channelType = "messaging"
        channelId = randomString()
        cid = randomCID()
        whenever(chatDomain.channel(cid)) doReturn channelController
        whenever(chatDomain.client) doReturn client
        whenever(channelController.channelType) doReturn channelType
        whenever(channelController.channelId) doReturn channelId
        val markReadCall = TestCall(Result(Unit))
        whenever(client.markRead(any(), any())) doReturn markReadCall
    }

    @Test
    fun `Given empty cid Should throw exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            sut.invoke("")
        }
    }

    @Test
    fun `Given invalid cid Should throw exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            sut.invoke("no-colon")
        }
    }

    @Test
    fun `Given valid cid Should invoke markRead() on ChannelController`() = runBlockingTest {
        val channelController = mock<ChannelController>()
        val cid = randomCID()
        whenever(chatDomain.channel(cid)) doReturn channelController

        sut.invoke(cid).execute()

        verify(channelController).markRead()
    }

    @Test
    fun `Given valid cid and successful ChannelController result Should call markRead() on client`() =
        runBlockingTest {
            channelController.stub { onBlocking { markRead() } doReturn true }

            sut.invoke(cid).execute()

            verify(channelController).markRead()
            verify(client).markRead(channelType, channelId)
        }

    @Test
    fun `Given valid cid and successful ChannelController result Should return return successful Result`() =
        runBlockingTest {
            channelController.stub { onBlocking { markRead() } doReturn true }

            val result = sut.invoke(cid).execute()

            Truth.assertThat(result.isSuccess).isTrue()
            Truth.assertThat(result.data()).isTrue()
        }

    @Test
    fun `Given valid cid and failed ChannelController result Should not call client`() =
        runBlockingTest {
            channelController.stub { onBlocking { markRead() } doReturn false }

            sut.invoke(cid).execute()

            verify(channelController).markRead()
            verifyZeroInteractions(client)
        }
}
