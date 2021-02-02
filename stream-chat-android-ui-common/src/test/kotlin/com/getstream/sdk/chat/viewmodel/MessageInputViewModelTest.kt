package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.createChannel
import com.getstream.sdk.chat.createCommands
import com.getstream.sdk.chat.createMembers
import com.getstream.sdk.chat.createMessage
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
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
import org.amshove.kluent.Verify
import org.amshove.kluent.When
import org.amshove.kluent.any
import org.amshove.kluent.called
import org.amshove.kluent.calling
import org.amshove.kluent.on
import org.amshove.kluent.that
import org.amshove.kluent.was
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
        When calling chatDomain.useCases doReturn useCases
        When calling useCases.watchChannel doReturn watchChannel
        When calling useCases.sendMessage doReturn sendMessage
        When calling useCases.sendMessageWithAttachments doReturn sendMessageWithAttachments
        When calling useCases.editMessage doReturn editMessage
        When calling useCases.keystroke doReturn keystroke
        When calling useCases.stopTyping doReturn stopTyping
        When calling watchChannel(eq(CID), eq(0)) doReturn channelControllerCall
        When calling channelControllerResult.isSuccess doReturn true
        When calling channelControllerResult.data() doReturn channelController
        When calling channelController.toChannel() doReturn channel
        When calling editMessage(any()) doReturn mock()
        When calling keystroke(eq(CID), anyOrNull()) doReturn mock()
        When calling stopTyping(eq(CID), anyOrNull()) doReturn mock()
    }

    @Test
    fun `Should show members`() {
        val members = createMembers()
        When calling channelController.members doReturn MutableLiveData(members)
        val messageInputViewModel = MessageInputViewModel(CID, chatDomain)
        val mockObserver: Observer<List<Member>> = spy()
        messageInputViewModel.members.observeForever(mockObserver)

        Verify on mockObserver that mockObserver.onChanged(eq(members)) was called
    }

    @Test
    fun `Should show commands`() {
        val messageInputViewModel = MessageInputViewModel(CID, chatDomain)
        val mockObserver: Observer<List<Command>> = spy()
        messageInputViewModel.commands.observeForever(mockObserver)

        Verify on mockObserver that mockObserver.onChanged(eq(commands)) was called
    }

    @Test
    fun `Should stop typing if a message is edited`() {
        val message = createMessage()
        val messageInputViewModel = MessageInputViewModel(CID, chatDomain)

        messageInputViewModel.editMessage(message)

        Verify on stopTyping that stopTyping(eq(CID), anyOrNull()) was called
        Verify on editMessage that editMessage(eq(message)) was called
    }
}
