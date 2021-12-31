package io.getstream.chat.android.offline.usecase

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.invoking
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class EditMessageTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var chatDomainImpl: ChatDomainImpl
    private lateinit var sut: EditMessage

    @BeforeEach
    fun before() {
        chatDomainImpl = mock {
            on { scope } doReturn testCoroutines.scope
        }
        sut = EditMessage(chatDomainImpl)
    }

    @Test
    fun `Given message with empty cid When invoke Should throw exception`() {
        invoking {
            sut.invoke(randomMessage(cid = ""))
        }.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun `Given message with inappropriate formatted cid When invoke Should throw exception`() {
        invoking {
            sut.invoke(randomMessage(cid = "cid"))
        }.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun `Given message with appropriate cid When invoke Should invoke edit message on channel controller`() =
        runBlockingTest {
            val channelController = mock<ChannelController>()
            val channel = randomChannel()
            val message = randomMessage(cid = channel.cid)
            whenever(chatDomainImpl.channel(message.cid)) doReturn channelController
            whenever(channelController.toChannel()) doReturn channel

            sut.invoke(message).execute()

            verify(channelController).editMessage(message)
        }

    @Test
    fun `Given message with appropriate cid When invoke Should populate mentions`() =
        runBlockingTest {
            val username = "Test User"
            val channelController = mock<ChannelController>()
            val channel = randomChannel(members = listOf(randomMember(user = randomUser().apply { name = username })))
            val message = randomMessage(cid = channel.cid, text = "Message text: @$username")
            whenever(chatDomainImpl.channel(message.cid)) doReturn channelController
            whenever(channelController.toChannel()) doReturn channel
            whenever(channelController.editMessage(message)) doReturn Result.success(message)

            val result = sut.invoke(message).execute()

            result.isSuccess.shouldBeTrue()
            result.data().mentionedUsersIds.size `should be equal to` 1
        }
}
