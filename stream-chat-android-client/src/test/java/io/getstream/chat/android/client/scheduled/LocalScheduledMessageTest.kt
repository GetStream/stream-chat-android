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
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.randomScheduledMessage
import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class LocalScheduledMessageTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("statusArguments")
    fun `the status merges the local and the server state`(
        @Suppress("UNUSED_PARAMETER") case: String,
        message: LocalScheduledMessage,
        expectedStatus: ScheduledMessageStatus,
        expectedFailure: ScheduledMessageFailure?,
    ) {
        val scheduledMessage = message.toScheduledMessage()

        scheduledMessage.status shouldBeEqualTo expectedStatus
        scheduledMessage.failure shouldBeEqualTo expectedFailure
    }

    @ParameterizedTest
    @MethodSource("pendingChanges")
    fun `a server version keeps the local content and requests while there are local changes`(
        pendingChange: PendingChange,
    ) {
        val local = randomLocalScheduledMessage(pendingChange = pendingChange, isPendingSendNow = true)
        val server = randomScheduledMessage(id = local.id, status = ScheduledMessageStatus.Sending)

        val merged = local.mergeServer(server)

        merged.message shouldBeEqualTo local.message
        merged.scheduledAt shouldBeEqualTo local.scheduledAt
        merged.pendingChange shouldBeEqualTo pendingChange
        merged.isPendingSendNow shouldBeEqualTo true
        merged.serverStatus shouldBeEqualTo ScheduledMessageStatus.Sending
    }

    @ParameterizedTest
    @MethodSource("pendingChanges")
    fun `a server version replaces the content when the server has the local changes`(
        @Suppress("UNUSED_PARAMETER") ignored: PendingChange,
    ) {
        val localFailure = ServerRejectedScheduling(null, randomString())
        val local = randomLocalScheduledMessage(isPendingSendNow = true, localFailure = localFailure)
        val server = randomScheduledMessage(id = local.id)

        val merged = local.mergeServer(server)

        merged.message shouldBeEqualTo server.message
        merged.scheduledAt shouldBeEqualTo server.scheduledAt
        merged.pendingChange.shouldBeNull()
        merged.isPendingSendNow shouldBeEqualTo true
        merged.localFailure shouldBeEqualTo localFailure
    }

    companion object {
        private val publishingFailure = ServerFailedPublishing(ServerFailedPublishing.Reason.ChannelDeleted)
        private val rejection = ServerRejectedScheduling(
            reason = ServerRejectedScheduling.Reason.ScheduledMessageNotEditable,
            message = "",
        )
        private val uploadFailure = LocalDeviceFailedScheduling(
            reason = LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed,
            message = null,
        )

        @JvmStatic
        fun pendingChanges() = PendingChange.entries.map { Arguments.of(it) }

        @JvmStatic
        fun statusArguments() = listOf(
            Arguments.of(
                "not on the server yet",
                randomLocalScheduledMessage(pendingChange = PendingChange.Create),
                ScheduledMessageStatus.PreparingToSchedule,
                null,
            ),
            Arguments.of(
                "an edit not on the server yet",
                randomLocalScheduledMessage(pendingChange = PendingChange.Update),
                ScheduledMessageStatus.PreparingToSchedule,
                null,
            ),
            Arguments.of(
                "a send now not on the server yet",
                randomLocalScheduledMessage(isPendingSendNow = true),
                ScheduledMessageStatus.PreparingToSchedule,
                null,
            ),
            Arguments.of(
                "scheduled on the server",
                randomLocalScheduledMessage(serverStatus = ScheduledMessageStatus.Scheduled),
                ScheduledMessageStatus.Scheduled,
                null,
            ),
            Arguments.of(
                "sending on the server",
                randomLocalScheduledMessage(serverStatus = ScheduledMessageStatus.Sending),
                ScheduledMessageStatus.Sending,
                null,
            ),
        ) + failureArguments()

        private fun failureArguments() = listOf(
            Arguments.of(
                "failed to publish",
                failedToPublish(),
                ScheduledMessageStatus.Failed,
                publishingFailure,
            ),
            Arguments.of(
                "a local failure",
                randomLocalScheduledMessage(pendingChange = PendingChange.Create, localFailure = uploadFailure),
                ScheduledMessageStatus.Failed,
                uploadFailure,
            ),
            Arguments.of(
                "a local failure after a failure to publish",
                failedToPublish().copy(pendingChange = PendingChange.Update, localFailure = rejection),
                ScheduledMessageStatus.Failed,
                rejection,
            ),
            Arguments.of(
                "an edit pending after a failure to publish",
                failedToPublish().copy(pendingChange = PendingChange.Update),
                ScheduledMessageStatus.PreparingToSchedule,
                publishingFailure,
            ),
        )

        private fun failedToPublish() = randomLocalScheduledMessage(
            serverStatus = ScheduledMessageStatus.Failed,
            publishingFailure = publishingFailure,
        )
    }
}
