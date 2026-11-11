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

import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateImplTestBase
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.MarkReadResult
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.util.Date

internal class ChannelLogicImplMarkReadTest : ChannelStateImplTestBase() {

    private val repository: RepositoryFacade = mock()
    private val other = User(id = "other")
    private lateinit var sut: ChannelLogicImpl

    @BeforeEach
    fun setUpLogic() {
        sut = ChannelLogicImpl(
            cid = CID,
            messagesUpdateLogic = mock(),
            repository = repository,
            state = channelState,
            mutableGlobalState = MutableGlobalState(currentUser.id),
            userPresence = false,
            coroutineScope = testCoroutines.scope,
            getCurrentUserId = { currentUser.id },
            now = { System.currentTimeMillis() },
        )
        channelState.setMessages(
            (1..3).map {
                randomMessage(
                    id = "message_$it",
                    cid = CID,
                    createdAt = Date(it * 1_000L),
                    createdLocallyAt = null,
                    user = other,
                    parentId = null,
                    type = MessageType.REGULAR,
                    syncStatus = SyncStatus.COMPLETED,
                    deletedAt = null,
                    silent = false,
                )
            },
        )
        channelState.replaceRead(read(unreadMessages = 3, lastRead = Date(500), lastReadMessageId = null))
    }

    @Test
    fun `a failed mark read restores the cleared unread count`() {
        assertEquals(MarkReadResult.RemoteRequired, sut.markRead(restoreOnFailure = true))
        assertEquals(0, unreadMessages())

        assertEquals(true, sut.onMarkReadFailed())

        assertEquals(3, unreadMessages())
    }

    @Test
    fun `a failed mark read keeps the messages counted after it`() {
        sut.markRead(restoreOnFailure = true)
        // A message.new counted after the count was cleared
        val newMessage = randomMessage(
            id = "message_4",
            cid = CID,
            createdAt = Date(4_000),
            createdLocallyAt = null,
            user = other,
            parentId = null,
            type = MessageType.REGULAR,
            syncStatus = SyncStatus.COMPLETED,
            deletedAt = null,
            silent = false,
            shadowed = false,
        )
        channelState.updateCurrentUserRead(Date(4_000), newMessage)
        assertEquals(1, unreadMessages())

        sut.onMarkReadFailed()

        assertEquals(4, unreadMessages())
    }

    @Test
    fun `a mark read that is not tracked restores nothing on failure`() {
        sut.markRead()

        assertEquals(false, sut.onMarkReadFailed())

        assertEquals(0, unreadMessages())
    }

    @Test
    fun `a failed mark read adds nothing when no message was loaded to clear the count`() {
        channelState.setMessages(emptyList())

        sut.markRead(restoreOnFailure = true)
        sut.onMarkReadFailed()

        assertEquals(3, unreadMessages())
    }

    @Test
    fun `a failed mark read adds nothing once a read from the server was applied`() {
        sut.markRead(restoreOnFailure = true)
        // A channel refresh while the request is in flight brings the server read at the same position
        channelState.updateReads(listOf(read(unreadMessages = 3, lastRead = Date(500), lastReadMessageId = null)))

        sut.onMarkReadFailed()

        assertEquals(3, unreadMessages())
    }

    private fun unreadMessages(): Int? = channelState.read.value?.unreadMessages

    private fun read(unreadMessages: Int, lastRead: Date, lastReadMessageId: String?) = ChannelUserRead(
        user = currentUser,
        lastReceivedEventDate = lastRead,
        unreadMessages = unreadMessages,
        lastRead = lastRead,
        lastReadMessageId = lastReadMessageId,
    )
}
