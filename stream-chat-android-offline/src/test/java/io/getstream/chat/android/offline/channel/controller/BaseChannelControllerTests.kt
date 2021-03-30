package io.getstream.chat.android.offline.channel.controller

import androidx.annotation.CallSuper
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.controller.helper.MessageHelper
import io.getstream.chat.android.offline.channel.ChannelController
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScope
import org.junit.jupiter.api.BeforeEach

internal open class BaseChannelControllerTests {
    protected val channelType = "channelType"
    protected val channelId = "channelId"
    protected lateinit var sut: ChannelController
    protected lateinit var chatClient: ChatClient
    protected lateinit var chatDomainImpl: ChatDomainImpl
    protected lateinit var messageHelper: MessageHelper

    @ExperimentalCoroutinesApi
    @BeforeEach
    @CallSuper
    open fun before() {
        chatClient = mock()
        chatDomainImpl = mock {
            on { scope } doReturn TestCoroutineScope()
        }
        messageHelper = mock()
        sut = ChannelController(channelType, channelId, chatClient, chatDomainImpl, messageHelper)
    }
}
