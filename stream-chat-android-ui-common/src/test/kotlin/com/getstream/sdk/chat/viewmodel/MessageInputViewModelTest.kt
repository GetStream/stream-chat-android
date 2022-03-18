@file:Suppress("DEPRECATION_ERROR")

package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.Observer
import com.getstream.sdk.chat.createCommands
import com.getstream.sdk.chat.createMembers
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageInputViewModelTest {

    private val CID = randomCID()
    private val channelConfig: Config = mock {
        whenever(it.commands) doReturn createCommands()
    }

    private val channelState: ChannelState = spy {
        whenever(it.repliedMessage) doReturn MutableStateFlow(Message())
        whenever(it.channelConfig) doReturn MutableStateFlow(channelConfig)
        whenever(it.members) doReturn MutableStateFlow(createMembers())
    }

    private val stateRegistry: StateRegistry = mock {
        whenever(it.channel(any(), any())) doReturn channelState
    }
    private val chatClient: ChatClient = mock {
        whenever(it.queryChannel(any(), any(), any())) doReturn Channel().asCall()
    }

    @BeforeEach
    fun setup() {
        StateRegistry.instance = stateRegistry
    }

    @Test
    fun `Should show commands`() {
        val messageInputViewModel = MessageInputViewModel(CID, chatClient)
        val mockObserver: Observer<List<Command>> = spy()
        messageInputViewModel.commands.observeForever(mockObserver)

        verify(mockObserver).onChanged(eq(channelConfig.commands))
    }
}
