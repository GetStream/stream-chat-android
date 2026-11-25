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

package io.getstream.chat.android.client.internal.offline.plugin.listener.internal

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.MockChatClientBuilder
import io.getstream.chat.android.client.internal.offline.integration.BaseRepositoryFacadeIntegrationTest
import io.getstream.chat.android.client.persistance.repository.ChannelRepository
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

/** Listener hooks against a real database, where a write is a transaction. */
@RunWith(AndroidJUnit4::class)
internal class ListenerDatabaseWriteIntegrationTest : BaseRepositoryFacadeIntegrationTest() {

    init {
        // Merging with a cached channel syncs unread counts against the current user.
        MockChatClientBuilder().build()
    }

    private val user = randomUser()
    private val channel = randomChannel(
        createdBy = user,
        messages = emptyList(),
        hiddenMessagesBefore = null,
        truncatedAt = null,
    )
    private val message = randomMessage(
        cid = channel.cid,
        user = user,
        deletedAt = null,
        deletedForMe = false,
        parentId = null,
        replyTo = null,
        poll = null,
    )

    @Test
    fun `Given a send request When its hook ends Should store the message and its channel's last message`() =
        runWriteTest {
            repositoryFacade.insertChannel(channel)
            val sut = SendAttachmentsListenerDatabase(repositoryFacade.store, repositoryFacade, repositoryFacade)

            sut.onAttachmentSendRequest(channel.type, channel.id, message)

            chatDatabase.messageDao().select(message.id).shouldNotBeNull()
            chatDatabase.channelStateDao().select(channel.cid)?.lastMessageId shouldBeEqualTo message.id
        }

    @Test
    fun `Given a send request When the second write of its hook fails Should store nothing of it`() = runWriteTest {
        repositoryFacade.insertChannel(channel)
        val failingChannels = object : ChannelRepository by repositoryFacade {
            override suspend fun updateLastMessageForChannel(cid: String, lastMessage: Message) = error("failed")
        }
        val sut = SendAttachmentsListenerDatabase(repositoryFacade.store, repositoryFacade, failingChannels)

        assertThrows(IllegalStateException::class.java) {
            runBlocking { sut.onAttachmentSendRequest(channel.type, channel.id, message) }
        }

        chatDatabase.messageDao().select(message.id) shouldBeEqualTo null
        repositoryFacade.selectMessage(message.id) shouldBeEqualTo null
    }

    @Test
    fun `Given a stored channel When its create request fails Should read and update it in one write`() =
        runWriteTest {
            val clientState = mock<ClientState> { on { it.isOnline } doReturn true }
            val sut = CreateChannelListenerDatabase(clientState, repositoryFacade, repositoryFacade)
            val id = randomString()
            sut.onCreateChannelRequest("messaging", id, CreateChannelParams(emptyList(), emptyMap()), user)
            chatDatabase.channelStateDao().select("messaging:$id")?.syncStatus shouldBeEqualTo SyncStatus.IN_PROGRESS

            sut.onCreateChannelResult("messaging", id, emptyList(), Result.Failure(Error.GenericError("failed")))

            chatDatabase.channelStateDao().select("messaging:$id")?.syncStatus shouldBeEqualTo SyncStatus.SYNC_NEEDED
        }

    private fun runWriteTest(testBody: suspend () -> Unit) {
        // Awaited from outside, as a write is not cancellable: a timeout around a deadlocked one would wait for it.
        val test = CoroutineScope(Dispatchers.Default).async { testBody() }
        runBlocking { withTimeout(TIMEOUT_MILLIS) { test.await() } }
    }

    private companion object {
        private const val TIMEOUT_MILLIS = 10_000L
    }
}
