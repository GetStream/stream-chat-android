package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.extensions.keystroke
import io.getstream.chat.android.offline.integration.BaseConnectedMockedTest
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldThrow
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class KeystrokeTest : BaseConnectedMockedTest() {

    @Test
    fun `Given empty cid When invoke Should throw exception`() {
        invoking {
            client.keystroke(cid = "")
        }.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun `Given inappropriate formatted cid When invoke Should throw exception`() {
        invoking {
            client.keystroke(cid = "31dse1")
        }.shouldThrow(IllegalArgumentException::class)
    }

    @Test
    fun `Given appropriate cid When invoke Should invoke keystroke to chat domain`() = runBlockingTest {
        val channelController = mock<ChannelController> {
            onBlocking { keystroke(anyOrNull()) } doReturn Result(true)
        }

        // Mock ChatDomain instance to use mocked ChannelController
        ChatDomain.instance = mock<ChatDomainImpl>().also {
            whenever(it.channel(any<String>())) doReturn channelController
            whenever(it.scope) doReturn TestCoroutineScope()
        }
        val result = client.keystroke(cid = randomCID()).execute()

        result.isSuccess.shouldBeTrue()
        result.data().shouldBeTrue()
    }
}
