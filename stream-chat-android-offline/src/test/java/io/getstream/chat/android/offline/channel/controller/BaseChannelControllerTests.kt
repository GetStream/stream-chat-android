package io.getstream.chat.android.offline.channel.controller

import androidx.annotation.CallSuper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach

internal open class BaseChannelControllerTests {
    protected val channelType = "channelType"
    protected val channelId = "channelId"
    protected val cid: String
        get() = "$channelType:$channelId"
    protected lateinit var sut: ChannelController
    protected lateinit var chatClient: ChatClient
    protected lateinit var chatDomainImpl: ChatDomainImpl
    protected lateinit var channelClient: ChannelClient
    protected lateinit var repos: RepositoryFacade

    @ExperimentalCoroutinesApi
    @BeforeEach
    @CallSuper
    open fun before() {
        repos = mock()
        channelClient = mock()
        chatClient = mock {
            on { channel(channelType, channelId) } doReturn channelClient
        }
        chatDomainImpl = mock {
            on { scope } doReturn TestCoroutineScope()
            on { repos } doReturn repos
        }
        sut = ChannelController(channelType, channelId, chatClient, chatDomainImpl)
    }
}
