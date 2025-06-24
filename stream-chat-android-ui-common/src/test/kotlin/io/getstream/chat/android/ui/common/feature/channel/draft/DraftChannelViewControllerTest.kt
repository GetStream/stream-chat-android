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

@file:OptIn(ExperimentalStreamChatApi::class)

package io.getstream.chat.android.ui.common.feature.channel.draft

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.User
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomGenericError
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channel.draft.DraftChannelViewState
import io.getstream.result.Error
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class DraftChannelViewControllerTest {

    @Test
    fun `initial state`() = runTest {
        val sut = Fixture().get(backgroundScope)

        assertEquals(DraftChannelViewState.Loading, sut.state.value)
    }

    @Test
    fun `create draft channel with no connected user`() = runTest {
        val memberIds = randomMemberIds()
        val fixture = Fixture().givenCreateDraftChannel(memberIds)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.events.test {
                assertEquals(
                    DraftChannelViewEvent.DraftChannelError,
                    awaitItem(),
                )
            }
        }
    }

    @Test
    fun `create draft channel success`() = runTest {
        val memberIds = randomMemberIds()
        val currentUser = randomUser()
        val channel = randomChannel()
        val fixture = Fixture().givenCreateDraftChannel(memberIds, currentUser, channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            assertEquals(
                DraftChannelViewState.Content(channel),
                awaitItem(),
            )
        }
        launch { fixture.verifyDraftChannelCreated(memberIds, currentUser) }
    }

    @Test
    fun `create draft channel error`() = runTest {
        val memberIds = randomMemberIds()
        val currentUser = randomUser()
        val channel = randomChannel()
        val fixture = Fixture().givenCreateDraftChannel(memberIds, currentUser, channel, error = randomGenericError())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(1) // Skip initial state

            sut.events.test {
                assertEquals(
                    DraftChannelViewEvent.DraftChannelError,
                    awaitItem(),
                )
            }
        }

        launch { fixture.verifyDraftChannelCreated(memberIds, currentUser) }
    }

    @Test
    fun `message sent success`() = runTest {
        val channel = randomChannel()
        val currentUser = randomUser()
        val fixture = Fixture()
            .givenCreateDraftChannel(memberIds = randomMemberIds(), currentUser, channel)
            .givenUpdateChannel(channel)
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(DraftChannelViewAction.MessageSent)

                assertEquals(
                    DraftChannelViewEvent.NavigateToChannel(channel.cid),
                    awaitItem(),
                )
            }
        }

        launch { fixture.verifyChannelUpdated(channel) }
    }

    @Test
    fun `message sent error`() = runTest {
        val channel = randomChannel()
        val currentUser = randomUser()
        val fixture = Fixture()
            .givenCreateDraftChannel(memberIds = randomMemberIds(), currentUser, channel)
            .givenUpdateChannel(channel, error = randomGenericError())
        val sut = fixture.get(backgroundScope)

        sut.state.test {
            skipItems(2) // Skip initial states

            sut.events.test {
                sut.onViewAction(DraftChannelViewAction.MessageSent)

                assertEquals(
                    DraftChannelViewEvent.DraftChannelError,
                    awaitItem(),
                )
            }
        }
    }
}

private class Fixture {
    private val chatClient: ChatClient = mock()
    private val channelClient: ChannelClient = mock()
    private var memberIds: List<String> = randomMemberIds()

    fun givenCreateDraftChannel(
        memberIds: List<String>,
        currentUser: User? = null,
        channel: Channel? = null,
        error: Error? = null,
    ) = apply {
        this.memberIds = memberIds
        if (currentUser != null) {
            whenever(chatClient.getCurrentUser()) doReturn currentUser
        }
        whenever(
            chatClient.createChannel(
                channelType = "messaging",
                channelId = "",
                params = CreateChannelParams(
                    members = (memberIds + listOfNotNull(currentUser?.id)).map(::MemberData),
                    extraData = mapOf("draft" to true),
                ),
            ),
        ) doAnswer {
            error?.asCall() ?: channel?.asCall()
        }
    }

    fun givenUpdateChannel(channel: Channel, error: Error? = null) = apply {
        whenever(chatClient.channel(channel.cid)) doReturn channelClient
        whenever(channelClient.update(message = null, extraData = mapOf("draft" to false))) doAnswer {
            error?.asCall() ?: channel.asCall()
        }
    }

    fun verifyDraftChannelCreated(memberIds: List<String>, currentUser: User) = apply {
        verify(chatClient).createChannel(
            channelType = "messaging",
            channelId = "",
            params = CreateChannelParams(
                members = (memberIds + currentUser.id).map(::MemberData),
                extraData = mapOf("draft" to true),
            ),
        )
    }

    fun verifyChannelUpdated(channel: Channel) = apply {
        verify(chatClient).channel(channel.cid)
        verify(channelClient).update(message = null, extraData = mapOf("draft" to false))
    }

    fun get(scope: CoroutineScope) = DraftChannelViewController(
        memberIds = memberIds,
        scope = scope,
        chatClient = chatClient,
    )
}

private fun randomMemberIds() = List(size = positiveRandomInt(10), init = ::randomString)
