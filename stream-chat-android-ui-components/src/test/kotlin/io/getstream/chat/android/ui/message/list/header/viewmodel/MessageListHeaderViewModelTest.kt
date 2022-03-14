package io.getstream.chat.android.ui.message.list.header.viewmodel

import io.getstream.chat.android.ui.createUser
import org.mockito.kotlin.times

private const val CID = "CID:messaging"
private val CURRENT_USER = createUser(online = true)

/*
@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageListHeaderViewModelTest {

    private val chatDomain: ChatDomain = mock()
    private val channelControllerResult: Result<ChannelController> = mock()
    private val channelControllerCall = TestCall(channelControllerResult)
    private val channelController: ChannelController = mock()

    @BeforeEach
    fun setup() {
        whenever(chatDomain.user) doReturn MutableLiveData(CURRENT_USER)
        whenever(chatDomain.watchChannel(CID, 0)) doReturn channelControllerCall
        whenever(channelControllerResult.isSuccess) doReturn true
        whenever(channelControllerResult.data()) doReturn channelController
    }

    @Test
    fun `Should notify about a new channel`() {
        val channel: Channel = mock()
        whenever(channelController.offlineChannelData) doReturn MutableLiveData(mock())
        whenever(channelController.toChannel()) doReturn channel
        val channelHeaderViewModel = MessageListHeaderViewModel(CID, chatDomain = chatDomain)
        val mockObserver: Observer<Channel> = spy()

        channelHeaderViewModel.channelState.observeForever(mockObserver)
        verify(mockObserver, times(2)).onChanged(eq(channel))
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

    *//** [createAnyOtherUserOnlineInput] *//*
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
*/
