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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.legacy

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.SearchLogic
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.Date

internal class ChannelLogicLegacyImplMarkReadTest {

    private val currentUser = User(id = CURRENT_USER_ID)
    private val repository: RepositoryFacade = mock()
    private lateinit var state: ChannelStateLegacyImpl
    private lateinit var stateLogic: ChannelStateLogic
    private lateinit var sut: ChannelLogicLegacyImpl

    @BeforeEach
    fun setUp() {
        val userFlow = MutableStateFlow<User?>(currentUser)
        state = ChannelStateLegacyImpl(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = null,
            now = { System.currentTimeMillis() },
        )
        val clientState = mock<ClientState> { on(it.user) doReturn userFlow }
        stateLogic = ChannelStateLogic(
            clientState = clientState,
            mutableState = state,
            globalMutableState = MutableGlobalState(CURRENT_USER_ID),
            searchLogic = SearchLogic(state),
            coroutineScope = testCoroutines.scope,
        )
        sut = ChannelLogicLegacyImpl(
            repos = repository,
            userPresence = false,
            stateLogic = stateLogic,
            coroutineScope = testCoroutines.scope,
            getCurrentUserId = { CURRENT_USER_ID },
        )
        state.setMessages(
            (1..3).map {
                randomMessage(
                    id = "message_$it",
                    cid = CID,
                    createdAt = Date(it * 1_000L),
                    createdLocallyAt = null,
                    user = User(id = "other"),
                    parentId = null,
                    type = MessageType.REGULAR,
                    syncStatus = SyncStatus.COMPLETED,
                    deletedAt = null,
                    silent = false,
                )
            },
        )
        state.upsertReads(listOf(read(unreadMessages = 3, lastRead = Date(500))))
    }

    @Test
    fun `a failed mark read restores the cleared unread count`() {
        sut.markRead(restoreOnFailure = true)
        assertEquals(0, state.read.value?.unreadMessages)

        sut.onMarkReadFailed()

        assertEquals(3, state.read.value?.unreadMessages)
    }

    @Test
    fun `a failed mark read adds nothing once a read from the server replaced the local one`() {
        sut.markRead(restoreOnFailure = true)
        // A read from the server that is more recent than the local one
        stateLogic.updateRead(read(unreadMessages = 3, lastRead = Date(500), lastReceivedEventDate = Date(9_000)))

        sut.onMarkReadFailed()

        assertEquals(3, state.read.value?.unreadMessages)
    }

    @Test
    fun `a failed mark read restores the count when the local read was kept as more recent`() {
        state.upsertReads(listOf(read(unreadMessages = 3, lastRead = Date(500), lastReceivedEventDate = Date(5_000))))
        sut.markRead(restoreOnFailure = true)
        // A channel refresh while the request is in flight: older than the local read, so the local 0 is kept
        stateLogic.updateRead(read(unreadMessages = 3, lastRead = Date(500), lastReceivedEventDate = Date(3_000)))
        assertEquals(0, state.read.value?.unreadMessages)

        sut.onMarkReadFailed()

        assertEquals(3, state.read.value?.unreadMessages)
    }

    private fun read(unreadMessages: Int, lastRead: Date, lastReceivedEventDate: Date = lastRead) = ChannelUserRead(
        user = currentUser,
        lastReceivedEventDate = lastReceivedEventDate,
        unreadMessages = unreadMessages,
        lastRead = lastRead,
        lastReadMessageId = null,
    )

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
        private const val CURRENT_USER_ID = "currentUser"
    }
}
