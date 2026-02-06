/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal

import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMemberRemovedEvent
import io.getstream.chat.android.client.test.randomUserMessagesDeletedEvent
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChannelLogicTest {

    private val currentUserId = randomString()
    private lateinit var channelStateLogic: ChannelStateLogic
    private lateinit var sut: ChannelLogic

    @BeforeEach
    fun setUp() {
        // Channel mutable state
        val cid = randomCID()
        val mutableState = mock<ChannelMutableState>()
        whenever(mutableState.cid).doReturn(cid)
        // Channel state logic
        channelStateLogic = mock()
        whenever(channelStateLogic.writeChannelState()).doReturn(mutableState)
        // Channel logic
        sut = ChannelLogicImpl(
            repos = mock(),
            userPresence = false,
            channelStateLogic = channelStateLogic,
            coroutineScope = TestScope(),
            getCurrentUserId = { currentUserId },
        )
    }

    @Test
    fun `When handling MemberAddedEvent for current user, Then channel members and membership are updated`() {
        // Given
        val user = randomUser(id = currentUserId)
        val member = randomMember(user = user)
        val event = randomMemberAddedEvent(member = member)
        // When
        sut.handleEvent(event)
        // Then
        verify(channelStateLogic).addMember(member)
        verify(channelStateLogic).addMembership(member)
    }

    @Test
    fun `When handling MemberAdded for other user, Then only channel members are updated`() {
        // Given
        val member = randomMember()
        val event = randomMemberAddedEvent(member = member)
        // When
        sut.handleEvent(event)
        // Then
        verify(channelStateLogic).addMember(member)
        verify(channelStateLogic, never()).addMembership(member)
    }

    @Test
    fun `When handling MemberRemovedEvent for current user, Then channel members and membership are updated`() {
        // Given
        val user = randomUser(id = currentUserId)
        val member = randomMember(user = user)
        val event = randomMemberRemovedEvent(member = member)
        // When
        sut.handleEvent(event)
        // Then
        verify(channelStateLogic).deleteMember(member)
        verify(channelStateLogic).removeMembership()
    }

    @Test
    fun `When handling MemberRemovedEvent for other user, Then only channel members are updated`() {
        // Given
        val member = randomMember()
        val event = randomMemberRemovedEvent(member = member)
        // When
        sut.handleEvent(event)
        // Then
        verify(channelStateLogic).deleteMember(member)
        verify(channelStateLogic, never()).removeMembership()
    }

    @Test
    fun `When handling UserMessagesDeletedEvent, Then operation is handled by ChannelStateLogic`() {
        // Given
        val user = randomUser(id = "userId")
        val createdAt = randomDate()
        val hardDelete = randomBoolean()
        val event = randomUserMessagesDeletedEvent(
            user = user,
            createdAt = createdAt,
            hardDelete = hardDelete,
        )
        // When
        sut.handleEvent(event)
        // Then
        verify(channelStateLogic).deleteMessagesFromUser(user.id, hardDelete, createdAt)
    }
}
