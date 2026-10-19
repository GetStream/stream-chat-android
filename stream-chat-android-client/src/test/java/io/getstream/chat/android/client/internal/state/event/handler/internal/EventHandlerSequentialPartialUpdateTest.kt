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

import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.test.randomNotificationChannelPartialUpdatedEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/** `notification.channel_partial_updated` through the event handler: grouped unread counts and offline storage. */
internal class EventHandlerSequentialPartialUpdateTest {

    private val currentUser = randomUser()

    @Test
    fun `NotificationChannelPartialUpdatedEvent migrates grouped unread count when group field changes`() = runTest {
        val cid = "messaging:channel-id"
        val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
            setGroupedUnreadChannels(mapOf("a" to 1, "b" to 0))
        }
        val partialChannel = Channel(id = "channel-id", type = "messaging", extraData = mapOf("group" to "b"))
        val handler = EventHandlerSequentialTest.Fixture()
            .withCurrentUser(currentUser)
            .withMutableGlobalState(mutableGlobalState)
            .withActiveChannel(
                channelType = "messaging",
                channelId = "channel-id",
                extraData = mapOf("group" to "a"),
                unreadMessages = 1,
            )
            .get(this)

        handler.handleEvents(randomNotificationChannelPartialUpdatedEvent(cid = cid, channel = partialChannel))

        mutableGlobalState.groupedUnreadChannels.value `should be equal to` mapOf("a" to 0, "b" to 1)
    }

    @Test
    fun `NotificationChannelPartialUpdatedEvent migrates grouped unread count of a stored channel that is not active`() =
        runTest {
            val stored = randomChannel(
                id = "channel-id",
                type = "messaging",
                extraData = mapOf("group" to "a"),
                read = listOf(randomChannelUserRead(user = currentUser, unreadMessages = 1)),
            )
            val repos: RepositoryFacade = mock()
            whenever(repos.selectMessages(any())) doReturn emptyList()
            whenever(repos.selectChannels(any())) doReturn listOf(stored)
            whenever(repos.selectThreads(any())) doReturn emptyList()
            whenever(repos.selectChannel(stored.cid)) doReturn stored
            val mutableGlobalState = MutableGlobalState(currentUser.id).apply {
                setGroupedUnreadChannels(mapOf("a" to 1, "b" to 0))
            }
            val partialChannel = Channel(id = "channel-id", type = "messaging", extraData = mapOf("group" to "b"))
            val handler = EventHandlerSequentialTest.Fixture()
                .withCurrentUser(currentUser)
                .withRepositoryFacade(repos)
                .withMutableGlobalState(mutableGlobalState)
                .get(this)

            handler.handleEvents(randomNotificationChannelPartialUpdatedEvent(cid = stored.cid, channel = partialChannel))

            mutableGlobalState.groupedUnreadChannels.value `should be equal to` mapOf("a" to 0, "b" to 1)
        }

    @Test
    fun `When handling NotificationChannelPartialUpdatedEvent, The stored channel should get the partial update`() =
        runTest {
            val storedChannel = randomChannel(members = listOf(randomMember()), extraData = mapOf("group" to "a"))
            val partialChannel = Channel(
                id = storedChannel.id,
                type = storedChannel.type,
                extraData = mapOf("group" to "b"),
            )
            val repos: RepositoryFacade = mock()
            whenever(repos.selectMessages(any())) doReturn emptyList()
            whenever(repos.selectChannels(any())) doReturn listOf(storedChannel)
            whenever(repos.selectThreads(any())) doReturn emptyList()
            val handler = EventHandlerSequentialTest.Fixture()
                .withRepositoryFacade(repos)
                .get(this)

            handler.handleEvents(
                randomNotificationChannelPartialUpdatedEvent(cid = storedChannel.cid, channel = partialChannel),
            )

            val inserted = argumentCaptor<Collection<Channel>>()
            verify(repos).insertChannels(inserted.capture())
            val storedResult = inserted.firstValue.single()
            storedResult.members `should be equal to` storedChannel.members
            storedResult.extraData `should be equal to` mapOf("group" to "b")
        }

    @Test
    fun `When handling NotificationChannelPartialUpdatedEvent, A channel that is not stored should not be inserted`() =
        runTest {
            val event = randomNotificationChannelPartialUpdatedEvent()
            val repos: RepositoryFacade = mock()
            whenever(repos.selectMessages(any())) doReturn emptyList()
            whenever(repos.selectChannels(any())) doReturn emptyList()
            whenever(repos.selectThreads(any())) doReturn emptyList()
            val handler = EventHandlerSequentialTest.Fixture()
                .withRepositoryFacade(repos)
                .get(this)

            handler.handleEvents(event)

            verify(repos).insertChannels(emptyList())
        }
}
