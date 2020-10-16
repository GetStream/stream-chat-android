package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import assertions.`should be equal to result`
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.ChannelControllerImpl
import io.getstream.chat.android.livedata.controller.ThreadController
import io.getstream.chat.android.livedata.controller.ThreadControllerImpl
import io.getstream.chat.android.livedata.randomCID
import io.getstream.chat.android.livedata.randomString
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.When
import org.amshove.kluent.`should throw`
import org.amshove.kluent.`with message`
import org.amshove.kluent.calling
import org.amshove.kluent.invoking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class GetThreadImplTest {

    val cid = randomCID()
    val parentId = randomString()
    val chatDomain: ChatDomainImpl = mock()
    val channelController: ChannelControllerImpl = mock()
    val threadControllerImpl: ThreadControllerImpl = mock()
    val getThreadImpl = GetThreadImpl(chatDomain)

    @Before
    fun setup() {
        When calling chatDomain.channel(cid) doReturn channelController
        When calling channelController.getThread(parentId) doReturn threadControllerImpl
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
        } `should throw` IllegalArgumentException::class`with message` "cid needs to be in the format channelType:channelId. For example, messaging:123"
    }
    @Test
    fun `Should return a ThreadController`() {
        runBlocking {
            When calling channelController.scope doReturn this

            val result = getThreadImpl(cid, parentId).execute()

            result `should be equal to result` Result(threadControllerImpl as ThreadController)
        }
    }
}
