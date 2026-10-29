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

package io.getstream.chat.android.compose.sample.feature.scheduled

import app.cash.turbine.test
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.QueryScheduledMessagesResult
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.result.Error
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.TimeZone

@ExtendWith(TestCoroutineExtension::class)
internal class ScheduledMessagesViewModelTest {

    private val scheduledMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())
    private val hasLoadedAll = MutableStateFlow(false)
    private val connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Connected)
    private val clientState: ClientState = mock {
        on { user } doReturn MutableStateFlow<User?>(null)
        on { connectionState } doReturn connectionState
    }
    private val client: ChatClient = mock {
        on { scheduledMessages } doReturn scheduledMessages
        on { hasLoadedAllScheduledMessages } doReturn hasLoadedAll
        on { clientState } doReturn clientState
        on { queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()) } doReturn
            QueryScheduledMessagesResult(emptyList(), next = null).asCall()
        on { queryChannels(any()) } doReturn emptyList<Channel>().asCall()
    }

    @Test
    fun `groups the failed messages first, then the others by day, with the actions of their status`() = runTest {
        val day = Date()
        val scheduled = randomScheduledMessage(status = ScheduledMessageStatus.Scheduled, scheduledAt = day)
        val sending = randomScheduledMessage(status = ScheduledMessageStatus.Sending, scheduledAt = day)
        val failed = randomScheduledMessage(
            status = ScheduledMessageStatus.Failed,
            failure = ServerRejectedScheduling(ServerRejectedScheduling.Reason.ScheduledMessageSending, randomString()),
        )
        scheduledMessages.value = listOf(scheduled, sending, failed)

        val state = ScheduledMessagesViewModel(client).state.value

        state.groups.map { it::class } shouldBeEqualTo
            listOf(ScheduledMessagesGroup.Failed::class, ScheduledMessagesGroup.Day::class)
        state.groups[0].items shouldBeEqualTo
            listOf(ScheduledMessageItemState(failed, listOf(ScheduledMessageAction.Delete)))
        state.groups[1].items shouldBeEqualTo listOf(
            ScheduledMessageItemState(scheduled, scheduled.actions),
            ScheduledMessageItemState(sending, emptyList()),
        )
    }

    @Test
    fun `while offline, the messages not on the server yet wait for the connection`() = runTest {
        val preparing = randomScheduledMessage(status = ScheduledMessageStatus.PreparingToSchedule)
        val scheduled = randomScheduledMessage(status = ScheduledMessageStatus.Scheduled)
        scheduledMessages.value = listOf(preparing, scheduled)
        val viewModel = ScheduledMessagesViewModel(client)
        viewModel.state.value.groups.none { it is ScheduledMessagesGroup.Waiting } shouldBeEqualTo true

        connectionState.value = ConnectionState.Offline

        val waiting = viewModel.state.value.groups.filterIsInstance<ScheduledMessagesGroup.Waiting>().single()
        waiting.items.map { it.scheduledMessage } shouldBeEqualTo listOf(preparing)
    }

    @Test
    fun `the other messages are grouped by the day they are sent, in their order`() {
        val timeZone = TimeZone.getTimeZone("Europe/Lisbon")
        val day1Morning = item(Date(1_793_350_800_000)) // 30 Oct 2026, 09:00
        val day1Evening = item(Date(1_793_386_800_000)) // 30 Oct 2026, 19:00
        val day2 = item(Date(1_793_437_200_000)) // 31 Oct 2026, 09:00

        val groups = scheduledMessagesGroups(listOf(day1Morning, day1Evening, day2), isOffline = false, timeZone)

        groups.map { it.items } shouldBeEqualTo listOf(listOf(day1Morning, day1Evening), listOf(day2))
    }

    @Test
    fun `each message comes with its channel, for its name`() = runTest {
        val channel = randomChannel()
        scheduledMessages.value = listOf(
            randomScheduledMessage(cid = channel.cid, status = ScheduledMessageStatus.Scheduled),
        )
        whenever(client.queryChannels(any())) doReturn listOf(channel).asCall()

        val state = ScheduledMessagesViewModel(client).state.value

        state.groups.single().items.single().channel shouldBeEqualTo channel
    }

    @Test
    fun `loads the scheduled messages when they are not loaded yet`() = runTest {
        ScheduledMessagesViewModel(client)

        verify(client).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `loads every page of the scheduled messages`() = runTest {
        val next = randomString()
        whenever(client.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())).doReturn(
            QueryScheduledMessagesResult(emptyList(), next = next).asCall(),
            QueryScheduledMessagesResult(emptyList(), next = null).asCall(),
        )

        ScheduledMessagesViewModel(client)

        verify(client).queryScheduledMessages(anyOrNull(), any(), any(), isNull())
        verify(client).queryScheduledMessages(anyOrNull(), any(), any(), eq(next))
    }

    @Test
    fun `does not load the scheduled messages again once they are loaded`() = runTest {
        hasLoadedAll.value = true

        ScheduledMessagesViewModel(client)

        verify(client, never()).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `loads the scheduled messages again once connected after the connection was lost`() = runTest {
        hasLoadedAll.value = true
        ScheduledMessagesViewModel(client)
        verify(client, never()).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())

        connectionState.value = ConnectionState.Offline
        hasLoadedAll.value = false
        verify(client, never()).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
        connectionState.value = ConnectionState.Connected

        verify(client).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `is loading until the messages are loaded, then empty`() = runTest {
        whenever(client.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())) doReturn
            CoroutineCall<QueryScheduledMessagesResult>(backgroundScope) { awaitCancellation() }
        val viewModel = ScheduledMessagesViewModel(client)
        viewModel.state.value.isLoading shouldBeEqualTo true

        hasLoadedAll.value = true

        val state = viewModel.state.value
        state.isLoading shouldBeEqualTo false
        state.isLoadFailed shouldBeEqualTo false
        state.groups.shouldBeEmpty()
    }

    @Test
    fun `shows the messages known on the device while it loads`() = runTest {
        scheduledMessages.value = listOf(randomScheduledMessage(status = ScheduledMessageStatus.Scheduled))

        val state = ScheduledMessagesViewModel(client).state.value

        state.isLoading shouldBeEqualTo false
        state.groups.single().items.size shouldBeEqualTo 1
    }

    @Test
    fun `once loaded, losing the connection keeps showing that there are none`() = runTest {
        val viewModel = ScheduledMessagesViewModel(client)
        viewModel.state.value.isLoading shouldBeEqualTo false

        connectionState.value = ConnectionState.Offline

        viewModel.state.value.isLoading shouldBeEqualTo false
        viewModel.state.value.isLoadFailed shouldBeEqualTo false
        viewModel.state.value.groups.shouldBeEmpty()
    }

    @Test
    fun `offline with nothing known, the messages can't load`() = runTest {
        connectionState.value = ConnectionState.Offline

        val state = ScheduledMessagesViewModel(client).state.value

        state.isLoading shouldBeEqualTo false
        state.isLoadFailed shouldBeEqualTo true
        state.isOffline shouldBeEqualTo true
        verify(client, never()).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `a failed load can be retried`() = runTest {
        val error = Error.GenericError(randomString())
        whenever(client.queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull()))
            .doReturn(error.asCall(), QueryScheduledMessagesResult(emptyList(), next = null).asCall())

        val viewModel = ScheduledMessagesViewModel(client)
        viewModel.state.value.isLoadFailed shouldBeEqualTo true
        viewModel.events.test { awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Error(error.message) }

        viewModel.onRetryLoadClick()

        viewModel.state.value.isLoadFailed shouldBeEqualTo false
        viewModel.state.value.isLoading shouldBeEqualTo false
        verify(client, times(2)).queryScheduledMessages(anyOrNull(), any(), any(), anyOrNull())
    }

    @Test
    fun `reschedule tells the date the server answered, once it answered`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = randomScheduledMessage(status = ScheduledMessageStatus.Scheduled)
        val requested = randomDate()
        val local = scheduledMessage.copy(scheduledAt = requested, status = ScheduledMessageStatus.PreparingToSchedule)
        whenever(client.rescheduleMessage(scheduledMessage.id, requested)).thenAnswer {
            scheduledMessages.value = listOf(local)
            local.asCall()
        }
        val viewModel = ScheduledMessagesViewModel(client)

        viewModel.events.test {
            viewModel.onRescheduleClick(scheduledMessage.id, requested)
            expectNoEvents()

            val rounded = Date(requested.time + 30_000)
            scheduledMessages.value = listOf(local.copy(scheduledAt = rounded, status = ScheduledMessageStatus.Scheduled))

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Scheduled(rounded)
        }
    }

    @Test
    fun `send now tells the message was sent once the server sends it`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = randomScheduledMessage(status = ScheduledMessageStatus.PreparingToSchedule)
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.sendScheduledMessageNow(scheduledMessage.id)) doReturn scheduledMessage.asCall()
        val viewModel = ScheduledMessagesViewModel(client)

        viewModel.events.test {
            viewModel.onSendNowClick(scheduledMessage.id)
            scheduledMessages.value = emptyList()

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Sent
        }
    }

    @Test
    fun `a retry that fails on the server tells why`() = runTest {
        hasLoadedAll.value = true
        val scheduledMessage = randomScheduledMessage(status = ScheduledMessageStatus.PreparingToSchedule)
        val failure = ServerRejectedScheduling(ServerRejectedScheduling.Reason.ScheduledLimitChannel, randomString())
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.resendScheduledMessage(scheduledMessage.id)) doReturn scheduledMessage.asCall()
        val viewModel = ScheduledMessagesViewModel(client)

        viewModel.events.test {
            viewModel.onRetryClick(scheduledMessage.id)
            scheduledMessages.value = listOf(
                scheduledMessage.copy(status = ScheduledMessageStatus.Failed, failure = failure),
            )

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Failed(failure)
        }
    }

    @Test
    fun `a change made offline tells it is queued`() = runTest {
        hasLoadedAll.value = true
        connectionState.value = ConnectionState.Offline
        val scheduledMessage = randomScheduledMessage(status = ScheduledMessageStatus.PreparingToSchedule)
        scheduledMessages.value = listOf(scheduledMessage)
        whenever(client.rescheduleMessage(eq(scheduledMessage.id), any())) doReturn scheduledMessage.asCall()
        val viewModel = ScheduledMessagesViewModel(client)

        viewModel.events.test {
            viewModel.onRescheduleClick(scheduledMessage.id, randomDate())

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Queued
        }
    }

    @Test
    fun `a refused change tells why`() = runTest {
        hasLoadedAll.value = true
        val id = randomString()
        val error = Error.GenericError(randomString())
        whenever(client.deleteScheduledMessage(id)) doReturn error.asCall()
        val viewModel = ScheduledMessagesViewModel(client)

        viewModel.events.test {
            viewModel.onDeleteClick(id)

            awaitItem() shouldBeEqualTo ScheduledMessagesEvent.Error(error.message)
        }
    }

    private fun item(scheduledAt: Date): ScheduledMessageItemState {
        val scheduledMessage = randomScheduledMessage(
            status = ScheduledMessageStatus.Scheduled,
            scheduledAt = scheduledAt,
        )
        return ScheduledMessageItemState(scheduledMessage, scheduledMessage.actions)
    }
}
