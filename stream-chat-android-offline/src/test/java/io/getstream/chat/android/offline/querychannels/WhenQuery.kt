package io.getstream.chat.android.offline.querychannels

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@ExperimentalCoroutinesApi
internal class WhenQuery {
    private lateinit var sut: QueryChannelsController
    private lateinit var chatClient: ChatClient

    @BeforeEach
    fun before() {
        val chatDomainImpl = mock<ChatDomainImpl> {
            on { scope } doReturn TestCoroutineScope()
            on { repos } doReturn mock()
        }
        chatClient = mock()
        sut = QueryChannelsController(mock(), mock(), chatClient, chatDomainImpl)
    }

    @Test
    fun `123`() = runBlockingTest {
        whenever(chatClient.queryChannels(any())) doReturn TestCall(Result(emptyList()))
        sut.query()
    }
}
