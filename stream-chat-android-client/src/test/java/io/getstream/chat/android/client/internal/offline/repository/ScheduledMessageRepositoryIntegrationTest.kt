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

package io.getstream.chat.android.client.internal.offline.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.integration.BaseDomainTest2
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage.PendingChange
import io.getstream.chat.android.client.scheduled.randomLocalScheduledMessage
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomString
import io.getstream.result.Error
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContainSame
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class ScheduledMessageRepositoryIntegrationTest : BaseDomainTest2() {

    private val repository get() = repos.scheduledMessageRepository

    @Test
    fun `a scheduled message reads back equal, with its attachments, upload state and pending changes`() = runTest {
        val scheduledMessage = fullScheduledMessage(
            pendingChange = PendingChange.Update,
            localFailure = ScheduledMessageFailure.ServerRejectedScheduling(
                reason = ScheduledMessageFailure.ServerRejectedScheduling.Reason.ScheduledLimitChannel,
                message = randomString(),
            ),
        )

        repository.insertScheduledMessage(scheduledMessage)

        repository.selectScheduledMessages() shouldBeEqualTo listOf(scheduledMessage)
    }

    @Test
    fun `the channel summary and the skip flags read back equal`() = runTest {
        val scheduledMessage = fullScheduledMessage().let {
            val (type, id) = it.cid.split(":")
            it.copy(
                channel = ChannelInfo(cid = it.cid, id = id, type = type, name = randomString(), image = randomString()),
                message = it.message.copy(skipPushNotification = true, skipEnrichUrl = true),
            )
        }

        repository.insertScheduledMessage(scheduledMessage)

        repository.selectScheduledMessages() shouldBeEqualTo listOf(scheduledMessage)
    }

    @Test
    fun `a failure to publish and a local device failure read back equal`() = runTest {
        val failedToPublish = fullScheduledMessage(
            serverStatus = ScheduledMessageStatus.Failed,
            publishingFailure = ScheduledMessageFailure.ServerFailedPublishing(
                ScheduledMessageFailure.ServerFailedPublishing.Reason.ChannelDeleted,
            ),
        )
        val failedLocally = fullScheduledMessage(
            pendingChange = PendingChange.Create,
            localFailure = ScheduledMessageFailure.LocalDeviceFailedScheduling(
                reason = ScheduledMessageFailure.LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed,
                message = randomString(),
            ),
        )

        repository.insertScheduledMessage(failedToPublish)
        repository.insertScheduledMessage(failedLocally)

        repository.selectScheduledMessages() shouldContainSame listOf(failedToPublish, failedLocally)
    }

    @Test
    fun `inserting a scheduled message with the same id replaces it`() = runTest {
        val scheduledMessage = fullScheduledMessage()
        val edited = scheduledMessage.copy(
            message = scheduledMessage.message.copy(text = randomString()),
            pendingChange = PendingChange.Update,
        )

        repository.insertScheduledMessage(scheduledMessage)
        repository.insertScheduledMessage(edited)

        repository.selectScheduledMessages() shouldBeEqualTo listOf(edited)
    }

    @Test
    fun `deleting a scheduled message removes only it`() = runTest {
        val deleted = fullScheduledMessage()
        val kept = fullScheduledMessage()
        repository.insertScheduledMessage(deleted)
        repository.insertScheduledMessage(kept)

        repository.deleteScheduledMessage(deleted.id)

        repository.selectScheduledMessages() shouldBeEqualTo listOf(kept)
    }

    @Test
    fun `clearing the repositories removes the scheduled messages`() = runTest {
        repository.insertScheduledMessage(fullScheduledMessage())

        repos.clear()

        repository.selectScheduledMessages().shouldBeEmpty()
    }

    @Test
    fun `a scheduled message never reaches the messages of the channels`() = runTest {
        val scheduledMessage = fullScheduledMessage(pendingChange = PendingChange.Create)

        repository.insertScheduledMessage(scheduledMessage)

        repos.selectMessage(scheduledMessage.id).shouldBeNull()
        // The message retries of the SyncManager and the upload worker read these.
        repos.selectMessageIdsBySyncState(SyncStatus.SYNC_NEEDED).shouldBeEmpty()
        repos.selectMessageIdsBySyncState(SyncStatus.AWAITING_ATTACHMENTS).shouldBeEmpty()
        repos.selectMessageIdsBySyncState(SyncStatus.IN_PROGRESS).shouldBeEmpty()
    }

    private fun fullScheduledMessage(
        serverStatus: ScheduledMessageStatus = ScheduledMessageStatus.Scheduled,
        publishingFailure: ScheduledMessageFailure.ServerFailedPublishing? = null,
        pendingChange: PendingChange? = null,
        localFailure: ScheduledMessageFailure? = null,
    ): LocalScheduledMessage {
        val id = randomString()
        val cid = randomCID()
        val createdAt = randomDate()
        val updatedAt = randomDate()
        return randomLocalScheduledMessage(
            id = id,
            cid = cid,
            message = fullMessage(id, cid, createdAt, updatedAt),
            serverStatus = serverStatus,
            publishingFailure = publishingFailure,
            pendingChange = pendingChange,
            hasPendingScheduledAt = pendingChange == PendingChange.Update,
            isPendingSendNow = true,
            localFailure = localFailure,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
    }

    private fun fullMessage(id: String, cid: String, createdAt: Date, updatedAt: Date) = Message(
        id = id,
        cid = cid,
        user = data.user1,
        text = randomString(),
        html = randomString(),
        type = "reply",
        parentId = randomString(),
        showInChannel = true,
        replyMessageId = randomString(),
        attachments = listOf(
            Attachment(
                assetUrl = randomString(),
                uploadState = Attachment.UploadState.Success,
                extraData = mapOf("key" to randomString()),
            ),
            Attachment(
                upload = File(randomString()).absoluteFile,
                uploadState = Attachment.UploadState.Failed(Error.GenericError(randomString())),
            ),
        ),
        mentionedUsersIds = listOf(randomString()),
        mentionedHere = true,
        mentionedChannel = true,
        mentionedGroups = listOf(UserGroup(id = randomString(), name = "")),
        mentionedRoles = listOf(randomString()),
        pinned = true,
        pinExpires = randomDate(),
        silent = true,
        restrictedVisibility = listOf(randomString()),
        sharedLocation = Location(
            cid = cid,
            messageId = id,
            userId = data.user1.id,
            endAt = randomDate(),
            latitude = 1.5,
            longitude = 2.5,
            deviceId = randomString(),
        ),
        extraData = mapOf("poll_id" to randomString()),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
