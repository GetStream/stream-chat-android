package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.utils.`should be equal to result`
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.thread.ThreadController
import io.getstream.chat.android.offline.usecase.GetThread
import io.getstream.chat.android.test.randomCID
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.amshove.kluent.invoking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GetThreadImplTest {

    val cid = randomCID()
    val parentId = randomString()
    val chatDomain: ChatDomainImpl = mock()
    val channelController: ChannelController = mock()
    val threadControllerImpl: ThreadController = mock()
    val getThreadImpl = GetThread(chatDomain)

    @Before
    fun setup() {
        whenever(chatDomain.channel(cid)) doReturn channelController
        whenever(channelController.getThread(parentId)) doReturn threadControllerImpl
    }

    @Test
    fun `Should throw an exception if the channel cid is empty`() {
        invoking {
            getThreadImpl("", randomString())
        } `should throw` IllegalArgumentException::class `with message` "cid can not be empty"
    }

    @Test
    fun `Should throw an exception if the channel cid doesn't contain a colon`() {
        invoking {
            getThreadImpl(randomString().replace(":", ""), randomString())
        } `should throw` IllegalArgumentException::class `with message` "cid needs to be in the format channelType:channelId. For example, messaging:123"
    }

    @Test
    fun `Should return a ThreadController`() {
        runBlocking {
            whenever(chatDomain.scope) doReturn this

            val result = getThreadImpl(cid, parentId).execute()

            result `should be equal to result` Result(threadControllerImpl as ThreadController)
        }
    }
}
