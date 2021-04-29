package io.getstream.chat.android.offline.usecase

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class KeystrokeTest {

    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var sut: Keystroke

    @BeforeEach
    fun before() {
        chatDomainImpl = mock()
        sut = Keystroke(chatDomainImpl)
    }

    @Test
    fun `Given empty cid When invoke Should throw exception`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            sut.invoke(cid = "")
        }
    }

    @Test
    fun `Given inappropriate formatted cid When invoke Should throw exception`() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            sut.invoke(cid = "31dse1")
        }
    }

    @Test
    fun `Given appropriate cid When invoke Should invoke keystroke to channel controller`() {
        val channelController = mock<ChannelController>()
        whenever(chatDomainImpl.channel(any<String>())) doReturn channelController
        whenever(chatDomainImpl.scope) doReturn TestCoroutineScope()

        sut.invoke(cid = randomCID(), parentId = null).execute()

        verify(channelController).keystroke(null)
    }

    @Test
    fun `Given appropriate cid When invoke Should invoke keystroke to chat domain`() {
        val channelController = mock<ChannelController> {
            on { keystroke(null) } doReturn Result(true)
        }
        whenever(chatDomainImpl.channel(any<String>())) doReturn channelController
        whenever(chatDomainImpl.scope) doReturn TestCoroutineScope()

        val result = sut.invoke(cid = randomCID(), parentId = null).execute()

        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.data()).isTrue()
    }
}
