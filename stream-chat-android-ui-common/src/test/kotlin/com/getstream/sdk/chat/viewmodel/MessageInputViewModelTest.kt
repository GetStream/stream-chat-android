@file:Suppress("DEPRECATION_ERROR")

package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.createChannel
import com.getstream.sdk.chat.createCommands
import com.getstream.sdk.chat.createMembers
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.randomUser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.randomCID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageInputViewModelTest {

    private val CID = randomCID()
    private val chatDomain: ChatDomain = mock()

    @Suppress("DEPRECATION_ERROR")
    private val channelControllerResult: Result<ChannelController> = mock()
    private val channelControllerCall = TestCall(channelControllerResult)
    private val channelController: ChannelController = mock()
    private val commands: List<Command> = createCommands()
    private val channel: Channel = createChannel(cid = CID, config = Config(commands = commands))

    @BeforeEach
    fun setup() {
        whenever(chatDomain.watchChannel(eq(CID), eq(0))) doReturn channelControllerCall
        whenever(channelControllerResult.isSuccess) doReturn true
        whenever(channelControllerResult.data()) doReturn channelController
        whenever(channelController.toChannel()) doReturn channel
        whenever(chatDomain.editMessage(any())) doReturn mock()
        whenever(chatDomain.keystroke(eq(CID), anyOrNull())) doReturn mock()
        whenever(chatDomain.stopTyping(eq(CID), anyOrNull())) doReturn mock()
        whenever(chatDomain.user) doReturn MutableLiveData(randomUser())
        whenever(channelController.offlineChannelData) doReturn MutableLiveData(mock())
    }

    @Test
    fun `Should show members`() {
        val members = createMembers()
        whenever(channelController.members) doReturn MutableLiveData(members)
        val messageInputViewModel = MessageInputViewModel(CID, chatDomain)
        val mockObserver: Observer<List<Member>> = spy()
        messageInputViewModel.members.observeForever(mockObserver)

        verify(mockObserver).onChanged(eq(members))
    }

    @Test
    fun `Should show commands`() {
        val messageInputViewModel = MessageInputViewModel(CID, chatDomain)
        val mockObserver: Observer<List<Command>> = spy()
        messageInputViewModel.commands.observeForever(mockObserver)

        verify(mockObserver).onChanged(eq(commands))
    }

    @Test
    fun `Should stop typing if a message is edited`() {
        val message = createMessage()
        val messageInputViewModel = MessageInputViewModel(CID, chatDomain)

        messageInputViewModel.editMessage(message)

        verify(chatDomain).stopTyping(eq(CID), anyOrNull())
        verify(chatDomain).editMessage(eq(message))
    }
}
