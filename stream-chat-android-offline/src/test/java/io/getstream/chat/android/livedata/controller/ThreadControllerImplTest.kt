package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.livedata.randomString
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ThreadControllerImplTest : BaseDomainTest2() {

    val threadId = randomString()
    val channelControllerImpl2: ChannelControllerImpl = mock()
    val chatClient: ChatClient = mock()

    @Test
    fun `the correct messages on the channelController should be shown on the thread`() = testCoroutines.scope.runBlockingTest {
    }

    @Test
    fun `new messages on the channel controller should show up on the thread`() = testCoroutines.scope.runBlockingTest {
    }

    @Test
    fun `removing messages on the channel controller should remove them from the thread`() = testCoroutines.scope.runBlockingTest {
    }

    @Test
    fun `loading more should set loading and endReached variables`() = testCoroutines.scope.runBlockingTest {
    }
}
