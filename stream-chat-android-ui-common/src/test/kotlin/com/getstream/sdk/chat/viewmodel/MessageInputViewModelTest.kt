/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("DEPRECATION_ERROR")

package com.getstream.sdk.chat.viewmodel

/*
@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageInputViewModelTest {

    private val CID = randomCID()
    private val chatDomain: ChatDomain = mock()
    private val chatClient: ChatClient = mock()

    @Suppress("DEPRECATION_ERROR")
    private val channelControllerResult: Result<ChannelController> = mock()
    private val channelControllerCall = TestCall(channelControllerResult)
    private val channelController: ChannelController = mock()
    private val commands: List<Command> = createCommands()
    private val channel: Channel = createChannel(cid = CID, config = Config(commands = commands))

    @BeforeEach
    fun setup() {
        whenever(chatDomain.watchChannel(eq(CID), eq(0))) doReturn channelControllerCall
        whenever(chatDomain.user) doReturn MutableLiveData(randomUser())
        whenever(channelController.toChannel()) doReturn channel
        whenever(channelController.offlineChannelData) doReturn MutableLiveData(mock())
        whenever(channelControllerResult.isSuccess) doReturn true
        whenever(channelControllerResult.data()) doReturn channelController
    }

    @Test
    fun `Should show members`() {
        val members = createMembers()
        whenever(channelController.members) doReturn MutableLiveData(members)
        val messageInputViewModel = MessageInputViewModel(CID, chatDomain, chatClient)
        val mockObserver: Observer<List<Member>> = spy()
        messageInputViewModel.members.observeForever(mockObserver)

        verify(mockObserver).onChanged(eq(members))
    }

    @Test
    fun `Should show commands`() {
        val messageInputViewModel = MessageInputViewModel(CID, chatDomain, chatClient)
        val mockObserver: Observer<List<Command>> = spy()
        messageInputViewModel.commands.observeForever(mockObserver)

        verify(mockObserver).onChanged(eq(commands))
    }
}*/
