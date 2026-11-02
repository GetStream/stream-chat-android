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

import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessageAction.CopyText
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessageAction.Delete
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessageAction.Edit
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessageAction.Reschedule
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessageAction.Retry
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessageAction.ScheduleAgain
import io.getstream.chat.android.compose.sample.feature.scheduled.ScheduledMessageAction.SendNow
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

internal class ScheduledMessageActionTest {

    @Test
    fun `a pending message can be edited, rescheduled, sent now and deleted`() {
        listOf(ScheduledMessageStatus.Scheduled, ScheduledMessageStatus.PreparingToSchedule).forEach { status ->
            randomScheduledMessage(status = status).actions shouldBeEqualTo listOf(Edit, Reschedule, SendNow, Delete)
        }
    }

    @Test
    fun `a message the server is sending has no actions`() {
        randomScheduledMessage(status = ScheduledMessageStatus.Sending).actions.shouldBeEmpty()
    }

    @Test
    fun `a message the server failed to send can be scheduled again as a new message, or deleted`() {
        failed(ServerFailedPublishing(ServerFailedPublishing.Reason.Banned)).actions shouldBeEqualTo
            listOf(ScheduleAgain, Delete)
        failed(ServerFailedPublishing(reason = null)).actions shouldBeEqualTo listOf(ScheduleAgain, Delete)
    }

    @Test
    fun `a message of a deleted channel can only be copied or deleted`() {
        failed(ServerFailedPublishing(ServerFailedPublishing.Reason.ChannelDeleted)).actions shouldBeEqualTo
            listOf(CopyText, Delete)
    }

    @Test
    fun `a message whose changes failed on the device can be retried`() {
        val failure = LocalDeviceFailedScheduling(LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed, null)

        failed(failure).actions shouldBeEqualTo listOf(Retry, Edit, Reschedule, SendNow, Delete)
    }

    @Test
    fun `a conflict can be retried, other rejections need a change first`() {
        failed(rejection(ServerRejectedScheduling.Reason.ScheduledMessageConflict)).actions shouldBeEqualTo
            listOf(Retry, Edit, Reschedule, SendNow, Delete)
        failed(rejection(ServerRejectedScheduling.Reason.ScheduledLimitChannel)).actions shouldBeEqualTo
            listOf(Edit, Reschedule, SendNow, Delete)
        failed(rejection(reason = null)).actions shouldBeEqualTo listOf(Edit, Reschedule, SendNow, Delete)
    }

    @Test
    fun `a message the server sends or can no longer change can only be deleted`() {
        failed(rejection(ServerRejectedScheduling.Reason.ScheduledMessageSending)).actions shouldBeEqualTo
            listOf(Delete)
        failed(rejection(ServerRejectedScheduling.Reason.ScheduledMessageNotEditable)).actions shouldBeEqualTo
            listOf(Delete)
    }

    @Test
    fun `the failure description says why the message failed`() {
        ServerFailedPublishing(ServerFailedPublishing.Reason.ChannelDeleted).description shouldBeEqualTo
            R.string.scheduled_messages_failure_channel_deleted
        ServerFailedPublishing(ServerFailedPublishing.Reason.Unknown(randomString())).description shouldBeEqualTo
            R.string.scheduled_messages_failed_to_send
        rejection(ServerRejectedScheduling.Reason.ScheduledLimitChannel).description shouldBeEqualTo
            R.string.scheduled_messages_failure_limit_channel
        LocalDeviceFailedScheduling(LocalDeviceFailedScheduling.Reason.RequestFailed, null).description shouldBeEqualTo
            R.string.scheduled_messages_failure_request
    }

    @Test
    fun `a rejection the sample does not describe shows the message of the server`() {
        rejection(ServerRejectedScheduling.Reason.ScheduledMessageIdTaken).description.shouldBeNull()
        rejection(reason = null).description.shouldBeNull()
    }

    private fun failed(failure: ScheduledMessageFailure) =
        randomScheduledMessage(status = ScheduledMessageStatus.Failed, failure = failure)

    private fun rejection(reason: ServerRejectedScheduling.Reason?) = ServerRejectedScheduling(reason, randomString())
}
