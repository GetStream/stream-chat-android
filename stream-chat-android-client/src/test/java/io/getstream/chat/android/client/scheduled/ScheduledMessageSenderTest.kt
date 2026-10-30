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

package io.getstream.chat.android.client.scheduled

import io.getstream.chat.android.client.scheduled.LocalScheduledMessage.PendingChange
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCall
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.inOrder
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.Date

@Suppress("LargeClass")
internal class ScheduledMessageSenderTest {

    private val fixture = ScheduledMessageManagerFixture()
    private val api = fixture.api
    private val manager = fixture.manager
    private val cid = randomCID()
    private val channelType = cid.substringBefore(":")
    private val channelId = cid.substringAfter(":")

    @Test
    fun `uploads the attachments, then schedules the message with them`() = runTest {
        val upload = Attachment(upload = File(randomString()))
        val uploaded = upload.copy(assetUrl = randomString(), uploadState = Attachment.UploadState.Success)
        whenever(fixture.attachmentUploader.uploadAttachment(any(), any(), eq(upload), anyOrNull(), anyOrNull()))
            .thenReturn(Result.Success(uploaded))
        api.givenScheduleAnswers()
        start()

        val scheduled = schedule(Message(text = randomString(), attachments = listOf(upload)))
        runCurrent()

        inOrder(fixture.attachmentUploader, api) {
            verify(fixture.attachmentUploader).uploadAttachment(channelType, channelId, upload, scheduled.id, null)
            verify(api).scheduleMessage(
                eq(channelType),
                eq(channelId),
                argThat { attachments == listOf(uploaded) },
                eq(scheduled.scheduledAt),
            )
        }
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.Scheduled
    }

    @Test
    fun `sends one message at a time`() = runTest {
        val first = PendingCall<ScheduleMessageResult>()
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(first)
        start()
        schedule()
        schedule()

        runCurrent()
        verify(api, times(1)).scheduleMessage(any(), any(), any(), any())

        api.givenScheduleAnswers()
        first.result.complete(Result.Failure(serverError(code = 4)))
        runCurrent()
        verify(api, times(2)).scheduleMessage(any(), any(), any(), any())
    }

    @Test
    fun `offline, the message stays pending and is sent once on reconnect`() = runTest {
        fixture.connectionState.value = ConnectionState.Offline
        api.givenScheduleAnswers()
        start()

        val scheduled = schedule()
        runCurrent()
        verify(api, never()).scheduleMessage(any(), any(), any(), any())
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.PreparingToSchedule

        fixture.connectionState.value = ConnectionState.Connected
        runCurrent()
        fixture.connectionState.value = ConnectionState.Offline
        runCurrent()
        fixture.connectionState.value = ConnectionState.Connected
        runCurrent()

        verify(api, times(1)).scheduleMessage(any(), any(), any(), any())
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.Scheduled
    }

    @Test
    fun `a request that fails because the connection is lost is sent again on reconnect, with no failure`() = runTest {
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenAnswer {
            fixture.connectionState.value = ConnectionState.Offline
            TestCall<ScheduleMessageResult>(Result.Failure(connectionError()))
        }
        start()
        val scheduled = schedule()
        runCurrent()
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.PreparingToSchedule

        api.givenScheduleAnswers()
        api.givenUpdateAnswers()
        fixture.connectionState.value = ConnectionState.Connected
        runCurrent()

        verify(api, times(2)).scheduleMessage(any(), any(), any(), eq(scheduled.scheduledAt))
        // The server may have stored the first create, with which it answers the second one.
        verify(api).updateScheduledMessage(eq(scheduled.id), any(), anyOrNull())
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.Scheduled
    }

    @Test
    fun `changes left in progress before a restart are sent at the next start`() = runTest {
        val notCreated = randomLocalScheduledMessage(cid = cid, pendingChange = PendingChange.Create)
        val notUpdated = randomLocalScheduledMessage(pendingChange = PendingChange.Update, hasPendingScheduledAt = true)
        val manager = ScheduledMessageManagerFixture(InMemoryScheduledMessageRepository(notCreated, notUpdated))
        manager.api.givenScheduleAnswers()
        manager.api.givenUpdateAnswers()

        manager.manager.start(backgroundScope)
        runCurrent()

        verify(manager.api).scheduleMessage(channelType, channelId, notCreated.message, notCreated.scheduledAt)
        verify(manager.api).updateScheduledMessage(notUpdated.id, notUpdated.message, notUpdated.scheduledAt)
    }

    @Test
    fun `an edit before the create reached the server goes out in the first request`() = runTest {
        fixture.connectionState.value = ConnectionState.Offline
        api.givenScheduleAnswers()
        start()
        val scheduled = schedule()
        val edited = scheduled.message.copy(text = randomString())
        val scheduledAt = randomDate()

        manager.update(scheduled.id, edited, scheduledAt)
        fixture.connectionState.value = ConnectionState.Connected
        runCurrent()

        verify(api).scheduleMessage(eq(channelType), eq(channelId), argThat { text == edited.text }, eq(scheduledAt))
        verify(api, never()).updateScheduledMessage(any(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `an edit while the create is in flight is sent as an update after it`() = runTest {
        val create = PendingCall<ScheduleMessageResult>()
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(create)
        api.givenUpdateAnswers()
        start()
        val scheduled = schedule()
        runCurrent()
        val edited = scheduled.message.copy(text = randomString())

        manager.update(scheduled.id, edited, scheduledAt = null)
        create.result.complete(Result.Success(scheduleAnswer(scheduled)))
        runCurrent()

        verify(api).updateScheduledMessage(eq(scheduled.id), argThat { text == edited.text }, eq(null))
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.Scheduled
    }

    @Test
    fun `an upload failure sets attachment_upload_failed, and a resend uploads again`() = runTest {
        val upload = Attachment(upload = File(randomString()))
        val uploadError = serverError(code = 4)
        whenever(fixture.attachmentUploader.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.Failure(uploadError))
        start()
        val scheduled = schedule(Message(text = randomString(), attachments = listOf(upload)))
        runCurrent()

        val failed = scheduledMessage(scheduled.id)
        failed.status shouldBeEqualTo ScheduledMessageStatus.Failed
        failed.failure shouldBeEqualTo LocalDeviceFailedScheduling(
            reason = LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed,
            message = uploadError.message,
        )
        verify(api, never()).scheduleMessage(any(), any(), any(), any())

        whenever(fixture.attachmentUploader.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.Success(upload.copy(assetUrl = randomString(), uploadState = Attachment.UploadState.Success)))
        api.givenScheduleAnswers()
        manager.resend(scheduled.id)
        runCurrent()

        verify(fixture.attachmentUploader, times(2)).uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.Scheduled
    }

    @Test
    fun `a request that fails with no server answer sets request_failed, and a resend sends the same request`() = runTest {
        val error = connectionError()
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(error)))
        start()
        val scheduled = schedule()
        runCurrent()

        scheduledMessage(scheduled.id).failure shouldBeEqualTo LocalDeviceFailedScheduling(
            reason = LocalDeviceFailedScheduling.Reason.RequestFailed,
            message = error.message,
        )

        api.givenScheduleAnswers()
        api.givenUpdateAnswers()
        manager.resend(scheduled.id)
        runCurrent()

        verify(api, times(2)).scheduleMessage(channelType, channelId, scheduled.message, scheduled.scheduledAt)
        verify(api).updateScheduledMessage(eq(scheduled.id), any(), anyOrNull())
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.Scheduled
    }

    @Test
    fun `a create answered without a scheduled message saves the sent message and drops the scheduled one`() = runTest {
        val sent = Message(id = randomString(), cid = cid, text = randomString())
        whenever(api.scheduleMessage(any(), any(), any(), any()))
            .thenReturn(TestCall(Result.Success(ScheduleMessageResult(sent, scheduledMessage = null))))
        start()
        schedule()
        runCurrent()

        fixture.sentAsMessages shouldBeEqualTo listOf(sent)
        fixture.manager.scheduledMessages.value.shouldBeEmpty()
        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [123, 124])
    fun `a create rejected as already sent or canceled drops the local copy`(code: Int) = runTest {
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(serverError(code))))
        start()
        schedule()
        runCurrent()

        fixture.manager.scheduledMessages.value.shouldBeEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [117, 118])
    fun `a create rejected by a limit keeps the content with a rejection that is not retryable`(code: Int) = runTest {
        val error = serverError(code)
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(error)))
        start()
        val scheduled = schedule()
        runCurrent()

        val failed = scheduledMessage(scheduled.id)
        failed.status shouldBeEqualTo ScheduledMessageStatus.Failed
        failed.message shouldBeEqualTo scheduled.message
        val failure = failed.failure as ServerRejectedScheduling
        failure.message shouldBeEqualTo error.message
        failure.reason shouldBeEqualTo if (code == 117) {
            ServerRejectedScheduling.Reason.ScheduledLimitChannel
        } else {
            ServerRejectedScheduling.Reason.ScheduledLimitUser
        }
        failure.isRetryable.shouldBeFalse()
    }

    @Test
    fun `a delete while the create is in flight deletes it on the server once created`() = runTest {
        val create = PendingCall<ScheduleMessageResult>()
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(create)
        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Success(Unit)))
        start()
        val scheduled = schedule()
        runCurrent()

        manager.delete(scheduled.id)
        fixture.manager.scheduledMessages.value.shouldBeEmpty()
        verify(api, never()).deleteScheduledMessage(any())

        create.result.complete(Result.Success(scheduleAnswer(scheduled)))
        runCurrent()

        verify(api).deleteScheduledMessage(scheduled.id)
        fixture.manager.scheduledMessages.value.shouldBeEmpty()
        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @Test
    fun `a failed server delete after the create shows the message again with the failure`() = runTest {
        val create = PendingCall<ScheduleMessageResult>()
        val error = Error.NetworkError(message = randomString(), serverErrorCode = -1, statusCode = 500)
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(create)
        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Failure(error)))
        start()
        val scheduled = schedule()
        runCurrent()

        manager.delete(scheduled.id)
        create.result.complete(Result.Success(scheduleAnswer(scheduled)))
        runCurrent()

        scheduledMessage(scheduled.id).failure shouldBeEqualTo LocalDeviceFailedScheduling(
            reason = LocalDeviceFailedScheduling.Reason.RequestFailed,
            message = error.message,
        )
        // It stays deleted, so a resend deletes it again.
        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().single().isDeleted.shouldBeTrue()
    }

    @Test
    fun `a failed delete is sent again on resend`() = runTest {
        val onServer = givenScheduledOnServer()
        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Failure(serverError(code = 4))))
        start()
        manager.delete(onServer.id)
        runCurrent()
        status(onServer.id) shouldBeEqualTo ScheduledMessageStatus.Failed

        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Success(Unit)))
        manager.resend(onServer.id)
        runCurrent()

        verify(api, times(2)).deleteScheduledMessage(onServer.id)
        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [400, 500])
    fun `a delete while an edit is in flight is sent to the server when the edit fails`(statusCode: Int) = runTest {
        val onServer = givenScheduledOnServer()
        val edit = PendingCall<ScheduledMessage>()
        whenever(api.updateScheduledMessage(any(), anyOrNull(), anyOrNull())).thenReturn(edit)
        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Success(Unit)))
        start()
        manager.update(onServer.id, onServer.message.copy(text = randomString()), scheduledAt = null)
        runCurrent()

        manager.delete(onServer.id)
        edit.result.complete(Result.Failure(serverError(code = 4, statusCode = statusCode)))
        runCurrent()

        verify(api).deleteScheduledMessage(onServer.id)
        manager.scheduledMessages.value.shouldBeEmpty()
        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @Test
    fun `a delete while an upload is in flight is sent to the server when the upload fails`() = runTest {
        val onServer = givenScheduledOnServer()
        val upload = CompletableDeferred<Result<Attachment>>()
        whenever(fixture.attachmentUploader.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull()))
            .doSuspendableAnswer { upload.await() }
        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Success(Unit)))
        start()
        val attachment = Attachment(upload = File(randomString()))
        manager.update(onServer.id, onServer.message.copy(attachments = listOf(attachment)), scheduledAt = null)
        runCurrent()

        manager.delete(onServer.id)
        upload.complete(Result.Failure(serverError(code = 4)))
        runCurrent()

        verify(api).deleteScheduledMessage(onServer.id)
        verify(api, never()).updateScheduledMessage(any(), anyOrNull(), anyOrNull())
        manager.scheduledMessages.value.shouldBeEmpty()
        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @Test
    fun `a create sends the content whose uploads were checked, and a file added meanwhile goes after it`() = runTest {
        val repository = InMemoryScheduledMessageRepository()
        val gated = ScheduledMessageManagerFixture(repository)
        val upload = Attachment(upload = File(randomString()))
        val uploaded = upload.copy(assetUrl = randomString(), uploadState = Attachment.UploadState.Success)
        whenever(gated.attachmentUploader.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.Success(uploaded))
        gated.api.givenScheduleAnswers()
        gated.api.givenUpdateAnswers()
        gated.connectionState.value = ConnectionState.Offline
        gated.manager.start(backgroundScope)
        val scheduled = (gated.manager.schedule(cid, Message(id = randomString()), randomDate()) as Result.Success).value

        // The edit holds the store while it saves, so the create starts in the middle of it.
        repository.insertGate = CompletableDeferred()
        backgroundScope.launch {
            gated.manager.update(scheduled.id, scheduled.message.copy(attachments = listOf(upload)), null)
        }
        runCurrent()
        gated.connectionState.value = ConnectionState.Connected
        runCurrent()
        repository.insertGate?.complete(Unit)
        runCurrent()

        inOrder(gated.api, gated.attachmentUploader) {
            verify(gated.api).scheduleMessage(any(), any(), argThat { attachments.isEmpty() }, any())
            verify(gated.attachmentUploader).uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())
            verify(gated.api).updateScheduledMessage(
                eq(scheduled.id),
                argThat { attachments == listOf(uploaded) },
                anyOrNull(),
            )
        }
    }

    @Test
    fun `a delete is local first, and sent to the server once online`() = runTest {
        val onServer = givenScheduledOnServer()
        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Success(Unit)))
        fixture.connectionState.value = ConnectionState.Offline
        start()

        manager.delete(onServer.id) shouldBeEqualTo Result.Success(Unit)
        runCurrent()
        fixture.manager.scheduledMessages.value.shouldBeEmpty()
        verify(api, never()).deleteScheduledMessage(any())

        fixture.connectionState.value = ConnectionState.Connected
        runCurrent()

        verify(api).deleteScheduledMessage(onServer.id)
        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @Test
    fun `a delete answered with not found removes the message`() = runTest {
        val onServer = givenScheduledOnServer()
        whenever(api.deleteScheduledMessage(any()))
            .thenReturn(TestCall(Result.Failure(serverError(code = 16, statusCode = 404))))
        start()

        manager.delete(onServer.id)
        runCurrent()

        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @Test
    fun `a delete rejected by the server shows the message again, and it can be deleted again`() = runTest {
        val onServer = givenScheduledOnServer()
        val error = serverError(code = 4)
        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Failure(error)))
        start()

        manager.delete(onServer.id)
        runCurrent()
        (scheduledMessage(onServer.id).failure as ServerRejectedScheduling).message shouldBeEqualTo error.message

        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Success(Unit)))
        manager.delete(onServer.id)
        runCurrent()

        verify(api, times(2)).deleteScheduledMessage(onServer.id)
        fixture.manager.scheduledMessages.value.shouldBeEmpty()
    }

    @Test
    fun `starting twice runs a single sender`() = runTest {
        val create = PendingCall<ScheduleMessageResult>()
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(create)
        start()
        start()

        schedule()
        runCurrent()

        verify(api, times(1)).scheduleMessage(any(), any(), any(), any())
    }

    @Test
    fun `a delete after a create with no answer deletes it on the server, which may have it`() = runTest {
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(connectionError())))
        whenever(api.deleteScheduledMessage(any()))
            .thenReturn(TestCall(Result.Failure(serverError(code = 16, statusCode = 404))))
        start()
        val scheduled = schedule()
        runCurrent()

        manager.delete(scheduled.id)
        runCurrent()

        verify(api).deleteScheduledMessage(scheduled.id)
        fixture.repositoryFacade.scheduledMessageRepository.selectScheduledMessages().shouldBeEmpty()
    }

    @ParameterizedTest
    @ValueSource(ints = [408, 429, 500, 502, 503, 504])
    fun `a create failing with an unknown outcome is a request failure, and a delete reaches the server`(
        statusCode: Int,
    ) = runTest {
        val error = Error.NetworkError(message = randomString(), serverErrorCode = -1, statusCode = statusCode)
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(error)))
        whenever(api.deleteScheduledMessage(any())).thenReturn(TestCall(Result.Success(Unit)))
        start()
        val scheduled = schedule()
        runCurrent()
        scheduledMessage(scheduled.id).failure shouldBeEqualTo LocalDeviceFailedScheduling(
            reason = LocalDeviceFailedScheduling.Reason.RequestFailed,
            message = error.message,
        )

        manager.delete(scheduled.id)
        runCurrent()

        verify(api).deleteScheduledMessage(scheduled.id)
    }

    @Test
    fun `a reschedule after a create with no answer sends the new date after the repeated create`() = runTest {
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(connectionError())))
        api.givenUpdateAnswers()
        start()
        val scheduled = schedule()
        runCurrent()
        val stored = scheduleAnswer(scheduled).scheduledMessage!!
        val scheduledAt = randomDate()

        whenever(api.scheduleMessage(any(), any(), any(), any()))
            .thenReturn(TestCall(Result.Success(ScheduleMessageResult(stored.message, stored))))
        manager.reschedule(scheduled.id, scheduledAt)
        runCurrent()

        verify(api).updateScheduledMessage(eq(scheduled.id), any(), eq(scheduledAt))
    }

    @Test
    fun `a delete after a rejected create sends no request`() = runTest {
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(serverError(117))))
        start()
        val scheduled = schedule()
        runCurrent()

        manager.delete(scheduled.id)
        runCurrent()

        verify(api, never()).deleteScheduledMessage(any())
        fixture.manager.scheduledMessages.value.shouldBeEmpty()
    }

    @Test
    fun `an edit after a create with no answer is sent as an update after the repeated create`() = runTest {
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(connectionError())))
        api.givenUpdateAnswers()
        start()
        val scheduled = schedule()
        runCurrent()
        val stored = scheduleAnswer(scheduled).scheduledMessage!!
        val edited = scheduled.message.copy(text = randomString())

        // The server stored the first create, and answers the repeated one with it.
        whenever(api.scheduleMessage(any(), any(), any(), any()))
            .thenReturn(TestCall(Result.Success(ScheduleMessageResult(stored.message, stored))))
        manager.update(scheduled.id, edited, scheduledAt = null)
        runCurrent()

        verify(api).updateScheduledMessage(eq(scheduled.id), argThat { text == edited.text }, eq(null))
        scheduledMessage(scheduled.id).message.text shouldBeEqualTo edited.text
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.Scheduled
    }

    @Test
    fun `a repeated create of a message the server already sent, as its date had passed, drops it`() = runTest {
        val duplicate = Error.NetworkError(
            message = "a message with ID ${randomString()} already exists",
            serverErrorCode = 4,
            statusCode = 400,
        )
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(connectionError())))
        start()
        val scheduled = schedule()
        runCurrent()

        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(duplicate)))
        manager.resend(scheduled.id)
        runCurrent()

        fixture.manager.scheduledMessages.value.shouldBeEmpty()
    }

    @Test
    fun `a repeated create answered with a message the server is sending saves it, with no update`() = runTest {
        whenever(api.scheduleMessage(any(), any(), any(), any())).thenReturn(TestCall(Result.Failure(connectionError())))
        start()
        val scheduled = schedule()
        runCurrent()

        val sending = scheduleAnswer(scheduled).scheduledMessage!!.copy(status = ScheduledMessageStatus.Sending)
        whenever(api.scheduleMessage(any(), any(), any(), any()))
            .thenReturn(TestCall(Result.Success(ScheduleMessageResult(sending.message, sending))))
        manager.resend(scheduled.id)
        runCurrent()

        verify(api, never()).updateScheduledMessage(any(), anyOrNull(), anyOrNull())
        status(scheduled.id) shouldBeEqualTo ScheduledMessageStatus.Sending
    }

    @Test
    fun `a create left in flight before a restart is followed by an update`() = runTest {
        val inFlight = randomLocalScheduledMessage(cid = cid, pendingChange = PendingChange.Create, isCreateSent = true)
        val restarted = ScheduledMessageManagerFixture(InMemoryScheduledMessageRepository(inFlight))
        restarted.api.givenScheduleAnswers()
        restarted.api.givenUpdateAnswers()

        restarted.manager.start(backgroundScope)
        runCurrent()

        inOrder(restarted.api) {
            verify(restarted.api).scheduleMessage(channelType, channelId, inFlight.message, inFlight.scheduledAt)
            verify(restarted.api).updateScheduledMessage(eq(inFlight.id), eq(inFlight.message), anyOrNull())
        }
    }

    @ParameterizedTest
    @ValueSource(ints = [120, 121, 122])
    fun `an edit rejected by the server keeps the message with the reason`(code: Int) = runTest {
        val onServer = givenScheduledOnServer()
        whenever(api.updateScheduledMessage(any(), anyOrNull(), anyOrNull()))
            .thenReturn(TestCall(Result.Failure(serverError(code, statusCode = 409))))
        start()

        val edited = onServer.message.copy(text = randomString())
        manager.update(onServer.id, edited, scheduledAt = null)
        runCurrent()

        val failed = scheduledMessage(onServer.id)
        failed.status shouldBeEqualTo ScheduledMessageStatus.Failed
        failed.message.text shouldBeEqualTo edited.text
        val failure = failed.failure as ServerRejectedScheduling
        failure.reason?.value shouldBeEqualTo when (code) {
            120 -> "scheduled_message_not_editable"
            121 -> "scheduled_message_sending"
            else -> "scheduled_message_conflict"
        }
        failure.isRetryable shouldBeEqualTo (code == 122)
    }

    @Test
    fun `an edit rejected with a conflict is sent again once, and succeeds`() = runTest {
        val onServer = givenScheduledOnServer()
        val conflict = TestCall<ScheduledMessage>(Result.Failure(serverError(code = 122, statusCode = 409)))
        whenever(api.updateScheduledMessage(any(), anyOrNull(), anyOrNull()))
            .thenReturn(conflict)
            .thenAnswer { TestCall(Result.Success(onServer.copy(scheduledAt = randomDate()))) }
        start()

        manager.reschedule(onServer.id, randomDate())
        runCurrent()

        verify(api, times(2)).updateScheduledMessage(any(), anyOrNull(), anyOrNull())
        status(onServer.id) shouldBeEqualTo ScheduledMessageStatus.Scheduled
    }

    @ParameterizedTest
    @ValueSource(ints = [120, 121, 122])
    fun `an edit rejected by the server is sent again only on a conflict`(code: Int) = runTest {
        val onServer = givenScheduledOnServer()
        whenever(api.updateScheduledMessage(any(), anyOrNull(), anyOrNull()))
            .thenReturn(TestCall(Result.Failure(serverError(code, statusCode = 409))))
        start()

        manager.reschedule(onServer.id, randomDate())
        runCurrent()

        verify(api, times(if (code == 122) 2 else 1)).updateScheduledMessage(any(), anyOrNull(), anyOrNull())
    }

    @Test
    fun `an edit answered with not found drops the message`() = runTest {
        val onServer = givenScheduledOnServer()
        whenever(api.updateScheduledMessage(any(), anyOrNull(), anyOrNull()))
            .thenReturn(TestCall(Result.Failure(serverError(code = 16, statusCode = 404))))
        start()

        manager.reschedule(onServer.id, randomDate())
        runCurrent()

        fixture.manager.scheduledMessages.value.shouldBeEmpty()
    }

    @Test
    fun `a reschedule sends the new date, and an edit alone sends no date`() = runTest {
        val onServer = givenScheduledOnServer()
        api.givenUpdateAnswers()
        start()
        val scheduledAt = randomDate()

        manager.reschedule(onServer.id, scheduledAt)
        runCurrent()
        val edited = scheduledMessage(onServer.id).message.copy(text = randomString())
        manager.update(onServer.id, edited, scheduledAt = null)
        runCurrent()

        inOrder(api) {
            verify(api).updateScheduledMessage(onServer.id, onServer.message.copy(createdAt = scheduledAt), scheduledAt)
            verify(api).updateScheduledMessage(eq(onServer.id), argThat { text == edited.text }, eq(null))
        }
    }

    @Test
    fun `send now is sent once the pending edit and its uploads are on the server`() = runTest {
        val onServer = givenScheduledOnServer()
        val upload = Attachment(upload = File(randomString()))
        val uploaded = upload.copy(assetUrl = randomString(), uploadState = Attachment.UploadState.Success)
        whenever(fixture.attachmentUploader.uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Result.Success(uploaded))
        api.givenUpdateAnswers()
        api.givenSendNowAnswers(onServer.id)
        fixture.connectionState.value = ConnectionState.Offline
        start()

        manager.update(onServer.id, onServer.message.copy(attachments = listOf(upload)), scheduledAt = null)
        manager.sendNow(onServer.id)
        status(onServer.id) shouldBeEqualTo ScheduledMessageStatus.PreparingToSchedule
        fixture.connectionState.value = ConnectionState.Connected
        runCurrent()

        inOrder(fixture.attachmentUploader, api) {
            verify(fixture.attachmentUploader).uploadAttachment(any(), any(), any(), anyOrNull(), anyOrNull())
            verify(api).updateScheduledMessage(eq(onServer.id), argThat { attachments == listOf(uploaded) }, eq(null))
            verify(api).sendScheduledMessageNow(onServer.id)
        }
        status(onServer.id) shouldBeEqualTo ScheduledMessageStatus.Sending
        fixture.manager.scheduledMessages.value.single().failure.shouldBeNull()
    }

    @Test
    fun `a failed send now is kept, and sent again on resend`() = runTest {
        val onServer = givenScheduledOnServer()
        whenever(api.sendScheduledMessageNow(any())).thenReturn(TestCall(Result.Failure(serverError(code = 4))))
        start()

        manager.sendNow(onServer.id)
        runCurrent()
        status(onServer.id) shouldBeEqualTo ScheduledMessageStatus.Failed

        api.givenSendNowAnswers(onServer.id)
        manager.resend(onServer.id)
        runCurrent()

        verify(api, times(2)).sendScheduledMessageNow(onServer.id)
        status(onServer.id) shouldBeEqualTo ScheduledMessageStatus.Sending
    }

    private suspend fun TestScope.start() {
        manager.start(backgroundScope)
    }

    private suspend fun schedule(
        message: Message = Message(text = randomString()),
        scheduledAt: Date = randomDate(),
    ): ScheduledMessage {
        val result = manager.schedule(cid, message.copy(id = randomString()), scheduledAt)
        return (result as Result.Success).value
    }

    private suspend fun givenScheduledOnServer(): ScheduledMessage {
        val onServer = randomScheduledMessage(cid = cid)
        manager.onEvent(scheduledMessageCreatedEvent(onServer))
        return onServer
    }

    private fun scheduledMessage(id: String): ScheduledMessage =
        fixture.manager.scheduledMessages.value.single { it.id == id }

    private fun status(id: String): ScheduledMessageStatus = scheduledMessage(id).status

    private fun scheduleAnswer(scheduled: ScheduledMessage) = ScheduleMessageResult(
        message = scheduled.message,
        scheduledMessage = scheduled.copy(status = ScheduledMessageStatus.Scheduled),
    )
}
