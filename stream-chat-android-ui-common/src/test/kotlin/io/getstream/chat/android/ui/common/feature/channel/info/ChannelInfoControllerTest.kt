/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.channel.info

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.toChannelData
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoViewState
import io.getstream.chat.android.ui.common.utils.ExpandableList
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

internal class ChannelInfoControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture().get(backgroundScope)

        assertEquals(ChannelInfoViewState.Content.Loading, sut.state.value.content)
    }

    @Test
    fun `direct channel content`() = runTest {
        val currentUser = User(id = "1")
        val otherUser = User(id = "2")
        val channel = Channel(
            id = "!members-1,2",
            createdBy = otherUser,
            members = listOf(
                Member(user = currentUser),
                Member(user = otherUser),
            ),
        )
        val sut = Fixture()
            .given(
                currentUser = currentUser,
                channel = channel,
            )
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = ExpandableList(
                        items = listOf(
                            ChannelInfoViewState.Member(
                                user = otherUser,
                                role = ChannelInfoViewState.Role.Owner,
                            ),
                        ),
                        minimumVisibleItems = 5,
                    ),
                ),
                actual.content,
            )
        }
    }

    @Test
    fun `group channel content`() = runTest {
        val owner = User(id = "1")
        val user2 = User(id = "2")
        val user3 = User(id = "3")
        val channel = Channel(
            createdBy = owner,
            members = listOf(
                Member(user = owner),
                Member(user = user2),
                Member(user = user3),
            ),
        )
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = ExpandableList(
                        items = listOf(
                            ChannelInfoViewState.Member(
                                user = owner,
                                role = ChannelInfoViewState.Role.Owner,
                            ),
                            ChannelInfoViewState.Member(
                                user = user2,
                                role = ChannelInfoViewState.Role.Other(""),
                            ),
                            ChannelInfoViewState.Member(
                                user = user3,
                                role = ChannelInfoViewState.Role.Other(""),
                            ),
                        ),
                        minimumVisibleItems = 5,
                    ),
                ),
                actual.content,
            )
        }
    }

    @Test
    fun `expandable group channel content`() = runTest {
        val channel = Channel(
            members = (1..10).map { i -> Member(user = User(id = "$i")) },
        )
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = ExpandableList(
                        items = channel.members
                            .map { member ->
                                ChannelInfoViewState.Member(
                                    user = member.user,
                                    role = ChannelInfoViewState.Role.Other(""),
                                )
                            },
                        minimumVisibleItems = 5,
                    ),
                ),
                actual.content,
            )
        }
    }

    @Test
    fun `expand group channel content`() = runTest {
        val channel = Channel(
            members = (1..10).map { i -> Member(user = User(id = "$i")) },
        )
        val sut = Fixture()
            .given(channel = channel)
            .get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = ExpandableList(
                        items = channel.members
                            .map { member ->
                                ChannelInfoViewState.Member(
                                    user = member.user,
                                    role = ChannelInfoViewState.Role.Other(""),
                                )
                            },
                        minimumVisibleItems = 5,
                    ),
                ),
                actual.content,
            )

            sut.expandMembers()

            val expandedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = ExpandableList(
                        items = channel.members
                            .map { member ->
                                ChannelInfoViewState.Member(
                                    user = member.user,
                                    role = ChannelInfoViewState.Role.Other(""),
                                )
                            },
                        minimumVisibleItems = 5,
                        isCollapsed = false,
                    ),
                ),
                expandedState.content,
            )
        }
    }

    @Test
    fun `update name`() = runTest {
        val channel = Channel(name = "name")
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    name = channel.name,
                ),
                actual.content,
            )

            val newName = "newName"
            fixture.givenUpdateChannelName(newName)

            sut.updateName(newName)

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    name = newName,
                ),
                updatedState.content,
            )
        }
    }

    @Test
    fun `update name error`() = runTest {
        val channel = Channel(name = "name")
        val fixture = Fixture().given(channel = channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial and loaded state

            val newName = "newName"
            val error = Error.GenericError("Error updating channel name")
            fixture.givenUpdateChannelName(newName, error)

            sut.updateName(newName)

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.UpdateNameError(message = error.message),
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `mute channel`() = runTest {
        val fixture = Fixture().given(isMuted = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isMuted = false,
                ),
                actual.content,
            )

            fixture.givenMuteChannel()

            sut.mute()

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isMuted = true,
                ),
                updatedState.content,
            )
        }
    }

    @Test
    fun `mute channel error`() = runTest {
        val fixture = Fixture().given(isMuted = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isMuted = false,
                ),
                actual.content,
            )

            val error = Error.GenericError("Error muting channel")
            fixture.givenMuteChannel(error)

            sut.mute()

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.MuteError(message = error.message),
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `unmute channel`() = runTest {
        val fixture = Fixture().given(isMuted = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isMuted = true,
                ),
                actual.content,
            )

            fixture.givenUnmuteChannel()

            sut.unmute()

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isMuted = false,
                ),
                updatedState.content,
            )
        }
    }

    @Test
    fun `unmute channel error`() = runTest {
        val fixture = Fixture().given(isMuted = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isMuted = true,
                ),
                actual.content,
            )

            val error = Error.GenericError("Error unmuting channel")
            fixture.givenUnmuteChannel(error)

            sut.unmute()

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.UnmuteError(message = error.message),
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `hide channel`() = runTest {
        val fixture = Fixture().given(isHidden = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isHidden = false,
                ),
                actual.content,
            )

            val clearHistory = true
            fixture.givenHideChannel(clearHistory)

            sut.hide(clearHistory)

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isHidden = true,
                ),
                updatedState.content,
            )
        }
    }

    @Test
    fun `hide channel error`() = runTest {
        val fixture = Fixture().given(isHidden = false)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isHidden = false,
                ),
                actual.content,
            )

            val clearHistory = true
            val error = Error.GenericError("Error hiding channel")
            fixture.givenHideChannel(clearHistory, error)

            sut.hide(clearHistory)

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.HideError(message = error.message),
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `unhide channel`() = runTest {
        val fixture = Fixture().given(isHidden = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isHidden = true,
                ),
                actual.content,
            )

            fixture.givenUnhideChannel()

            sut.unhide()

            val updatedState = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isHidden = false,
                ),
                updatedState.content,
            )
        }
    }

    @Test
    fun `unhide channel error`() = runTest {
        val fixture = Fixture().given(isHidden = true)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                    isHidden = true,
                ),
                actual.content,
            )

            val error = Error.GenericError("Error unhiding channel")
            fixture.givenUnhideChannel(error)

            sut.unhide()

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.UnhideError(message = error.message),
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `leave channel`() = runTest {
        val currentUser = User(id = "1")
        val fixture = Fixture()
            .given(
                currentUser = currentUser,
                channel = Channel(id = "!members-1,2"),
            )
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                ),
                actual.content,
            )

            val quitMessage = Message(text = "${currentUser.id} left")
            fixture.givenLeaveChannel(quitMessage)

            sut.leave(quitMessage)

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.LeaveSuccess,
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `leave channel error`() = runTest {
        val currentUser = User(id = "1")
        val fixture = Fixture()
            .given(
                currentUser = currentUser,
                channel = Channel(id = "!members-1,2"),
            )
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                ),
                actual.content,
            )

            val quitMessage = Message()
            val error = Error.GenericError("Error leaving channel")
            fixture.givenLeaveChannel(quitMessage, error)

            sut.leave(quitMessage)

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.LeaveError(message = error.message),
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `delete channel`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                ),
                actual.content,
            )

            fixture.givenDeleteChannel()

            sut.delete()

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.DeleteSuccess,
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `delete channel error`() = runTest {
        val fixture = Fixture()
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            val actual = awaitItem()
            assertEquals(
                ChannelInfoViewState.Content.Success(
                    members = emptyMembers(),
                ),
                actual.content,
            )

            val error = Error.GenericError("Error deleting channel")
            fixture.givenDeleteChannel(error)

            sut.delete()

            sut.events.test {
                assertEquals(
                    ChannelInfoEvent.DeleteError(message = error.message),
                    awaitItem(),
                )
            }
        }
    }
}

private const val CID = "messaging:1"

private class Fixture {
    private val channelData = MutableStateFlow(ChannelData(type = "", id = ""))
    private val channelMembers = MutableStateFlow(emptyList<Member>())
    private val channelMuted = MutableStateFlow(false)
    private val channelHidden = MutableStateFlow(false)
    private val channelState: ChannelState = mock {
        on { channelData } doReturn channelData
        on { members } doReturn channelMembers
        on { muted } doReturn channelMuted
        on { hidden } doReturn channelHidden
    }
    private val channelClient: ChannelClient = mock()
    private val chatClient: ChatClient = mock()

    fun given(
        currentUser: User? = null,
        channel: Channel? = null,
        isMuted: Boolean? = null,
        isHidden: Boolean? = null,
    ) = apply {
        if (currentUser != null) {
            whenever(chatClient.getCurrentOrStoredUserId()) doReturn currentUser.id
        }
        if (channel != null) {
            channelData.value = channel.toChannelData()
            channelMembers.value = channel.members
        }
        if (isMuted != null) {
            channelMuted.value = isMuted
        }
        if (isHidden != null) {
            channelHidden.value = isHidden
        }
    }

    fun givenUpdateChannelName(name: String, error: Error? = null) = apply {
        whenever(channelClient.updatePartial(mapOf("name" to name))) doAnswer {
            error?.asCall()
                ?: mock<Channel>().asCall().also {
                    channelData.update { channelData -> channelData.copy(name = name) }
                }
        }
    }

    fun givenMuteChannel(error: Error? = null) = apply {
        whenever(channelClient.mute()) doAnswer {
            error?.asCall()
                ?: Unit.asCall().also {
                    channelMuted.value = true
                }
        }
    }

    fun givenUnmuteChannel(error: Error? = null) = apply {
        whenever(channelClient.unmute()) doAnswer {
            error?.asCall()
                ?: Unit.asCall().also {
                    channelMuted.value = false
                }
        }
    }

    fun givenHideChannel(clearHistory: Boolean, error: Error? = null) = apply {
        whenever(channelClient.hide(clearHistory)) doAnswer {
            error?.asCall()
                ?: Unit.asCall().also {
                    channelHidden.value = true
                }
        }
    }

    fun givenUnhideChannel(error: Error? = null) = apply {
        whenever(channelClient.show()) doAnswer {
            error?.asCall()
                ?: Unit.asCall().also {
                    channelHidden.value = false
                }
        }
    }

    fun givenLeaveChannel(quitMessage: Message, error: Error? = null) = apply {
        whenever(
            channelClient.removeMembers(
                memberIds = listOf(chatClient.getCurrentOrStoredUserId()!!),
                systemMessage = quitMessage
            )
        ) doAnswer {
            error?.asCall()
                ?: mock<Channel>().asCall()
        }
    }

    fun givenDeleteChannel(error: Error? = null) = apply {
        whenever(channelClient.delete()) doAnswer {
            error?.asCall()
                ?: mock<Channel>().asCall()
        }
    }

    fun get(scope: CoroutineScope) = ChannelInfoController(
        cid = CID,
        scope = scope,
        chatClient = chatClient,
        channelState = MutableStateFlow(channelState),
        channelClient = channelClient,
    )
}

private fun <T> emptyMembers() = ExpandableList<T>(
    items = emptyList(),
    minimumVisibleItems = 5,
)
