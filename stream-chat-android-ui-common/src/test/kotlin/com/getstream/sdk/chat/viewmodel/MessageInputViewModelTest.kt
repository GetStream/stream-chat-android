@file:Suppress("DEPRECATION_ERROR")

package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.Observer
import com.getstream.sdk.chat.buildChannelState
import com.getstream.sdk.chat.createCommands
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomCID
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.validateMockitoUsage
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageInputViewModelTest {

    private val CID = randomCID()

    private val channelConfig: Config = mock()

    private val channelState: ChannelState = buildChannelState(channelConfig = MutableStateFlow(channelConfig))

    private val globalState: GlobalMutableState = mock()

    private val stateRegistry: StateRegistry = mock()

    private val chatClient: ChatClient = mock()

    init {
        GlobalMutableState.instance = globalState
        StateRegistry.instance = stateRegistry
    }

    @BeforeEach
    fun setup(){
        whenever(globalState.user) doReturn MutableStateFlow(User(id = "id"))
        whenever(stateRegistry.channel(any(), any())).doAnswer { channelState }
        whenever(chatClient.queryChannel(any(), any(), any())) doReturn Channel().asCall()
    }

    @AfterEach
    fun validate() {
        validateMockitoUsage()
    }

    @Test
    fun `Should show commands`() {
        // given
        whenever(channelConfig.commands) doReturn createCommands()
        val messageInputViewModel = MessageInputViewModel(CID, chatClient)
        val mockObserver: Observer<List<Command>> = spy()

        // when
        messageInputViewModel.commands.observeForever(mockObserver)

        // then
        verify(mockObserver).onChanged(eq(channelConfig.commands))
    }
}
