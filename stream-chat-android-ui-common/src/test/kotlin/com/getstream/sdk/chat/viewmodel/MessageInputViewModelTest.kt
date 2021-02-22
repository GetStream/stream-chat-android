@file:Suppress("DEPRECATION_ERROR")

package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.createChannel
import com.getstream.sdk.chat.createCommands
import com.getstream.sdk.chat.createMembers
import com.getstream.sdk.chat.createMessage
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
import io.getstream.chat.android.livedata.usecase.EditMessage
import io.getstream.chat.android.livedata.usecase.Keystroke
import io.getstream.chat.android.livedata.usecase.SendMessage
import io.getstream.chat.android.livedata.usecase.SendMessageWithAttachments
import io.getstream.chat.android.livedata.usecase.StopTyping
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.usecase.WatchChannel
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
    private val useCases: UseCaseHelper = mock()
    private val watchChannel: WatchChannel = mock()
    private val sendMessage: SendMessage = mock()
    @Suppress("DEPRECATION_ERROR")
    private val sendMessageWithAttachments: SendMessageWithAttachments = mock()
    private val editMessage: EditMessage = mock()
    private val keystroke: Keystroke = mock()
    private val stopTyping: StopTyping = mock()
    private val channelControllerResult: Result<ChannelController> = mock()
    private val channelControllerCall = TestCall(channelControllerResult)
    private val channelController: ChannelController = mock()
    private val commands: List<Command> = createCommands()
    private val channel: Channel = createChannel(cid = CID, config = Config(commands = commands))

    @BeforeEach
    fun setup() {
        whenever(chatDomain.useCases) doReturn useCases
        whenever(useCases.watchChannel) doReturn watchChannel
        whenever(useCases.sendMessage) doReturn sendMessage
        whenever(useCases.sendMessageWithAttachments) doReturn sendMessageWithAttachments
        whenever(useCases.editMessage) doReturn editMessage
        whenever(useCases.keystroke) doReturn keystroke
        whenever(useCases.stopTyping) doReturn stopTyping
        whenever(watchChannel(eq(CID), eq(0))) doReturn channelControllerCall
        whenever(channelControllerResult.isSuccess) doReturn true
        whenever(channelControllerResult.data()) doReturn channelController
        whenever(channelController.toChannel()) doReturn channel
        whenever(editMessage(any())) doReturn mock()
        whenever(keystroke(eq(CID), anyOrNull())) doReturn mock()
        whenever(stopTyping(eq(CID), anyOrNull())) doReturn mock()
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

        verify(stopTyping).invoke(eq(CID), anyOrNull())
        verify(editMessage).invoke(eq(message))
    }
}
