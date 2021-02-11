package io.getstream.chat.android.ui.message.list.header.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.usecase.WatchChannel
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.ui.createMember
import io.getstream.chat.android.ui.createMembers
import io.getstream.chat.android.ui.createUser
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

private const val CID = "CID:messaging"
private val CURRENT_USER = createUser(online = true)

@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageListHeaderViewModelTest {

    private val chatDomain: ChatDomain = mock()
    private val useCases: UseCaseHelper = mock()
    private val watchChannel: WatchChannel = mock()
    private val channelControllerResult: Result<ChannelController> = mock()
    private val channelControllerCall = TestCall(channelControllerResult)
    private val channelController: ChannelController = mock()

    @BeforeEach
    fun setup() {
        whenever(chatDomain.currentUser) doReturn CURRENT_USER
        whenever(chatDomain.useCases) doReturn useCases
        whenever(useCases.watchChannel) doReturn watchChannel
        whenever(watchChannel.invoke(CID, 0)) doReturn channelControllerCall
        whenever(channelControllerResult.isSuccess) doReturn true
        whenever(channelControllerResult.data()) doReturn channelController
    }

    @Test
    fun `Should notify about a new channel`() {
        val channel: Channel = mock()
        whenever(channelController.channelData) doReturn MutableLiveData(mock())
        whenever(channelController.toChannel()) doReturn channel
        val channelHeaderViewModel = MessageListHeaderViewModel(CID, chatDomain = chatDomain)
        val mockObserver: Observer<Channel> = spy()

        channelHeaderViewModel.channelState.observeForever(mockObserver)
        verify(mockObserver).onChanged(eq(channel))
    }

    @Test
    fun `Should notify about new members`() {
        val members = createMembers()
        whenever(channelController.members) doReturn MutableLiveData(members)
        val channelHeaderViewModel = MessageListHeaderViewModel(CID, chatDomain = chatDomain)
        val mockObserver: Observer<List<Member>> = spy()

        channelHeaderViewModel.members.observeForever(mockObserver)
        verify(mockObserver).onChanged(eq(members))
    }

    /** [createAnyOtherUserOnlineInput] */
    @ParameterizedTest
    @MethodSource("io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModelTest#createAnyOtherUserOnlineInput")
    fun `Should notify about any other user online`(members: List<Member>, expectedValue: Boolean) {
        whenever(channelController.members) doReturn MutableLiveData(members)
        val channelHeaderViewModel = MessageListHeaderViewModel(CID, chatDomain = chatDomain)
        val mockObserver: Observer<Boolean> = spy()

        channelHeaderViewModel.anyOtherUsersOnline.observeForever(mockObserver)

        verify(mockObserver).onChanged(eq(expectedValue))
    }

    companion object {
        @JvmStatic
        fun createAnyOtherUserOnlineInput() = listOf(
            Arguments.of(createMembers { createMember(createUser(online = false)) }, false),
            Arguments.of(createMembers { createMember(createUser(online = true)) }, true),
            Arguments.of(createMembers() + createMember(createUser(online = true)), true),
            Arguments.of(
                createMembers { createMember(createUser(online = false)) } + createMember(
                    createUser(online = true)
                ),
                true
            ),
            Arguments.of(
                createMembers { createMember(createUser(online = false)) } + createMember(
                    CURRENT_USER
                ),
                false
            ),
            Arguments.of(
                createMembers { createMember(createUser(online = true)) } + createMember(
                    CURRENT_USER
                ),
                true
            ),
            Arguments.of(
                createMembers() + createMember(createUser(online = true)) + createMember(
                    CURRENT_USER
                ),
                true
            ),
            Arguments.of(
                createMembers { createMember(createUser(online = false)) } + createMember(
                    createUser(online = true)
                ) + createMember(CURRENT_USER),
                true
            )
        )
    }
}
