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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.querythreads.internal.QueryThreadsLogic
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.randomMessage
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever

internal class DeleteMessageForMeListenerStateTest {

    @Test
    fun `on request, when message found and network available, should upsert message`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenNetworkAvailable(true)
            .givenMessage(message)
        val sut = fixture.get()

        sut.onDeleteMessageForMeRequest(message.id)

        val updatedMessage = message.copy(deletedForMe = true, syncStatus = SyncStatus.IN_PROGRESS)
        fixture.verifyUpsertMessage(updatedMessage)
    }

    @Test
    fun `on request, when message found and network unavailable, should upsert message`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenNetworkAvailable(false)
            .givenMessage(message)
        val sut = fixture.get()

        sut.onDeleteMessageForMeRequest(message.id)

        val updatedMessage = message.copy(deletedForMe = true, syncStatus = SyncStatus.SYNC_NEEDED)
        fixture.verifyUpsertMessage(updatedMessage)
    }

    @Test
    fun `on request, when channel not found, should do no interactions`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenNoChannelFound(message.id)
        val sut = fixture.get()

        sut.onDeleteMessageForMeRequest(message.id)

        fixture.verifyNoInteractions()
    }

    @Test
    fun `on request, when message not found, should do no interactions`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onDeleteMessageForMeRequest(message.id)

        fixture.verifyNoInteractions()
    }

    @Test
    fun `on result success, should upsert message`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenMessage(message)
        val sut = fixture.get()

        sut.onDeleteMessageForMeResult(message.id, Result.Success(message))

        val updatedMessage = message.copy(syncStatus = SyncStatus.COMPLETED)
        fixture.verifyUpsertMessage(updatedMessage)
    }

    @Test
    fun `on result failure, when message found, should upsert message`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenMessage(message)
        val sut = fixture.get()

        sut.onDeleteMessageForMeResult(message.id, Result.Failure(mock()))

        val updatedMessage = message.copy(deletedForMe = true, syncStatus = SyncStatus.SYNC_NEEDED)
        fixture.verifyUpsertMessage(updatedMessage)
    }

    @Test
    fun `on result failure, when channel not found, should do no interactions`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
            .givenNoChannelFound(message.id)
        val sut = fixture.get()

        sut.onDeleteMessageForMeResult(message.id, Result.Failure(mock()))

        fixture.verifyNoInteractions()
    }

    @Test
    fun `on result failure, when message not found, should do no interactions`() = runTest {
        val message = randomMessage()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onDeleteMessageForMeResult(message.id, Result.Failure(mock()))

        fixture.verifyNoInteractions()
    }

    private class Fixture {

        private val logicRegistry: LogicRegistry = mock()
        private val clientState: ClientState = mock()

        private val channelLogic: ChannelLogic = mock()
        private val threadsLogics = listOf(mock<QueryThreadsLogic>())
        private val threadLogic: ThreadLogic = mock()

        fun givenNetworkAvailable(isNetworkAvailable: Boolean) = apply {
            whenever(clientState.isNetworkAvailable) doReturn isNetworkAvailable
        }

        fun givenMessage(message: Message) = apply {
            whenever(channelLogic.getMessage(message.id)) doReturn message
            whenever(logicRegistry.channelFromMessageId(message.id)) doReturn channelLogic
            whenever(logicRegistry.getActiveQueryThreadsLogic()) doReturn threadsLogics
            whenever(logicRegistry.threadFromMessageId(message.id)) doReturn threadLogic
        }

        fun givenNoChannelFound(messageId: String) = apply {
            whenever(logicRegistry.channelFromMessageId(messageId)) doReturn null
        }

        fun verifyUpsertMessage(message: Message) = apply {
            verify(channelLogic).upsertMessage(message)
            threadsLogics.forEach { verify(it).upsertMessage(message) }
            verify(threadLogic).upsertMessage(message)
        }

        fun verifyNoInteractions() = apply {
            threadsLogics.forEach(::verifyNoInteractions)
            verifyNoInteractions(channelLogic, threadLogic)
        }

        fun get() = DeleteMessageForMeListenerState(
            logicRegistry = logicRegistry,
            clientState = clientState,
        )
    }
}
