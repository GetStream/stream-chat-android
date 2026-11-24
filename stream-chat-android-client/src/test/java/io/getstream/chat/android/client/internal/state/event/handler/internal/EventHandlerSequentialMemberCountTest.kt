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

package io.getstream.chat.android.client.internal.state.event.handler.internal

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.client.test.randomMemberRemovedEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Covers the member count that [EventHandlerSequential] stores for a channel on member events.
 */
internal class EventHandlerSequentialMemberCountTest {

    private val storedMember = randomMember()

    // Only one of the ten members is stored, as for a channel with a partial member list.
    private val storedChannel = randomChannel(members = listOf(storedMember), memberCount = 10)

    @Test
    fun `When a member outside the stored list is added with a count, should store the count`() = runTest {
        val event = randomMemberAddedEvent(cid = storedChannel.cid, channelMemberCount = 12)

        handle(event).memberCount `should be equal to` 12
    }

    @Test
    fun `When a member is added without a count, should increment the stored count`() = runTest {
        val event = randomMemberAddedEvent(cid = storedChannel.cid)

        handle(event).memberCount `should be equal to` 11
    }

    @Test
    fun `When a member outside the stored list is removed with a count, should store the count`() = runTest {
        val event = randomMemberRemovedEvent(cid = storedChannel.cid, channelMemberCount = 8)

        handle(event).memberCount `should be equal to` 8
    }

    @Test
    fun `When a stored member is removed with a count, should store the count`() = runTest {
        val event = randomMemberRemovedEvent(
            cid = storedChannel.cid,
            user = storedMember.user,
            member = storedMember,
            channelMemberCount = 7,
        )

        handle(event).memberCount `should be equal to` 7
    }

    @Test
    fun `When a member outside the stored list is removed without a count, should keep the stored count`() = runTest {
        val event = randomMemberRemovedEvent(cid = storedChannel.cid)

        handle(event).memberCount `should be equal to` 10
    }

    private suspend fun TestScope.handle(event: ChatEvent): Channel {
        val repos: RepositoryFacade = mock()
        whenever(repos.selectMessages(any())) doReturn emptyList()
        whenever(repos.selectChannels(any())) doReturn listOf(storedChannel)
        whenever(repos.selectThreads(any())) doReturn emptyList()
        val handler = EventHandlerSequentialTest.Fixture()
            .withCurrentUser(randomUser())
            .withRepositoryFacade(repos)
            .get(this)

        handler.handleEvents(event)

        val inserted = argumentCaptor<Collection<Channel>>()
        verify(repos).insertChannels(inserted.capture())
        return inserted.firstValue.single()
    }
}
