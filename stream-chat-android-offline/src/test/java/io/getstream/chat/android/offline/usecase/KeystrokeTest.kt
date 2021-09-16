package io.getstream.chat.android.offline.usecase

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
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
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldThrow
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
        invoking {
            sut.invoke(cid = "")
        }.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun `Given inappropriate formatted cid When invoke Should throw exception`() {
        invoking {
            sut.invoke(cid = "31dse1")
        }.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun `Given appropriate cid When invoke Should invoke keystroke to channel controller`() = runBlockingTest {
        val channelController = mock<ChannelController>()
        whenever(chatDomainImpl.channel(any<String>())) doReturn channelController
        whenever(chatDomainImpl.scope) doReturn TestCoroutineScope()

        sut.invoke(cid = randomCID(), parentId = null).execute()

        verify(channelController).keystroke(null)
    }

    @Test
    fun `Given appropriate cid When invoke Should invoke keystroke to chat domain`() = runBlockingTest {
        val channelController = mock<ChannelController>()
        whenever(channelController.keystroke(anyOrNull())) doReturn Result(true)
        whenever(chatDomainImpl.channel(any<String>())) doReturn channelController
        whenever(chatDomainImpl.scope) doReturn TestCoroutineScope()

        val result = sut.invoke(cid = randomCID(), parentId = null).execute()

        result.isSuccess.shouldBeTrue()
        result.data().shouldBeTrue()
    }
}
