package com.getstream.sdk.chat.viewmodel

import androidx.arch.core.executor.testing.InstantExecutorExtension
import com.getstream.sdk.chat.createChannel
import com.getstream.sdk.chat.createUser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.usecase.WatchChannel
import io.getstream.chat.android.livedata.utils.Call2
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val CID = "CID:messaging"
private val CURRENT_USER = createUser(online = true)
private val CHANNEL = createChannel(CID)

@ExtendWith(InstantExecutorExtension::class)
class MessageListViewModelTest {
    private lateinit var viewModel: MessageListViewModel

    private val chatDomain: ChatDomain = mock()
    private val useCases: UseCaseHelper = mock()
    private val watchChannel: WatchChannel = mock()
    private val watchChannelCall: Call2<ChannelController> = mock()
    private val channelControllerResult: Result<ChannelController> = mock()
    private val channelController: ChannelController = mock()

    @BeforeEach
    fun setup() {
        whenever(chatDomain.useCases) doReturn useCases
        whenever(useCases.watchChannel) doReturn watchChannel
        whenever(watchChannel.invoke(any(), any())) doReturn watchChannelCall
        whenever(watchChannelCall.execute()) doReturn channelControllerResult
    }

    @Test
    fun `Should display messages`() {

    }
}