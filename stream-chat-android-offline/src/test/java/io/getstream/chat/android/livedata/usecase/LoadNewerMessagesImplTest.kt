package io.getstream.chat.android.livedata.usecase

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.usecase.LoadNewerMessagesImpl
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomInt
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class LoadNewerMessagesImplTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var sut: LoadNewerMessagesImpl

    @BeforeEach
    fun before() {
        chatDomainImpl = mock {
            on { scope } doReturn testCoroutines.scope
        }
        sut = LoadNewerMessagesImpl(chatDomainImpl)
    }

    @Test
    fun `Given empty cid When invoke Should throw exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            sut.invoke(cid = "", messageLimit = randomInt())
        }
    }

    @Test
    fun `Given inappropriate formatted cid When invoke Should throw exception`() {
        assertThrows(IllegalArgumentException::class.java) {
            sut.invoke(cid = "cid", messageLimit = randomInt())
        }
    }

    @Test
    fun `Given appropriate cid When invoke Should invoke load newer messages on channel controller`() =
        runBlockingTest {
            val channelController = mock<ChannelController>()
            val messageLimit = randomInt()
            val cid = randomCID()
            whenever(chatDomainImpl.channel(cid)) doReturn channelController

            sut.invoke(cid = cid, messageLimit = messageLimit).execute()

            verify(channelController).loadNewerMessages(limit = messageLimit)
        }

    @Test
    fun `Given appropriate cid And successful response When invoke Should return channel`() = runBlockingTest {
        val channelMock = mock<Channel>()
        val cid = randomCID()
        val messageLimit = randomInt()
        val channelController = mock<ChannelController> {
            onBlocking { loadNewerMessages(limit = messageLimit) } doReturn Result(channelMock)
        }
        whenever(chatDomainImpl.channel(cid)) doReturn channelController

        val result = sut.invoke(cid = cid, messageLimit = messageLimit).execute()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isEqualTo(channelMock)
    }

    @Test
    fun `Given appropriate cid And failed response When invoke Should return error`() = runBlockingTest {
        val errorMock = mock<ChatError>()
        val cid = randomCID()
        val messageLimit = randomInt()
        val channelController = mock<ChannelController> {
            onBlocking { loadNewerMessages(limit = messageLimit) } doReturn Result(errorMock)
        }
        whenever(chatDomainImpl.channel(cid)) doReturn channelController

        val result = sut.invoke(cid = cid, messageLimit = messageLimit).execute()

        Truth.assertThat(result.isSuccess).isFalse()
        Truth.assertThat(result.error()).isEqualTo(errorMock)
    }
}
