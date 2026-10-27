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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.Mother.randomChannelResponse
import io.getstream.chat.android.client.Mother.randomScheduledMessageResponse
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.network.models.Attachment
import io.getstream.chat.android.network.models.ChannelOwnCapability
import io.getstream.chat.android.network.models.QueryScheduledMessagesResponse
import io.getstream.chat.android.network.models.ScheduledMessageChannelResponse
import io.getstream.chat.android.network.models.ScheduledMessageContentResponse
import io.getstream.chat.android.network.models.ScheduledMessageFailureReason
import io.getstream.chat.android.network.models.ScheduledMessageLocation
import io.getstream.chat.android.randomString
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date
import io.getstream.chat.android.network.models.ScheduledMessageStatus as ScheduledMessageStatusDto

internal class ScheduledMessageMappingTest {

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { randomString() },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )
    private val dtoMapping = DtoMapping(NoOpMessageTransformer, NoOpUserTransformer)

    @Test
    fun `A scheduled message maps every field`() {
        val response = randomScheduledMessageResponse(
            id = "scheduled-1",
            cid = "messaging:general",
            message = CONTENT,
            channel = ScheduledMessageChannelResponse(
                cid = "messaging:general",
                id = "general",
                type = "messaging",
                name = "General",
                image = "https://example.com/general.png",
            ),
        )

        val scheduledMessage = with(domainMapping) { response.toDomain() }

        scheduledMessage.id shouldBeEqualTo "scheduled-1"
        scheduledMessage.cid shouldBeEqualTo "messaging:general"
        scheduledMessage.channel shouldBeEqualTo ChannelInfo(
            cid = "messaging:general",
            id = "general",
            type = "messaging",
            name = "General",
            image = "https://example.com/general.png",
        )
        scheduledMessage.scheduledAt shouldBeEqualTo response.scheduledAt
        scheduledMessage.createdAt shouldBeEqualTo response.createdAt
        scheduledMessage.updatedAt shouldBeEqualTo response.updatedAt
        scheduledMessage.status shouldBeEqualTo ScheduledMessageStatus.Scheduled
        scheduledMessage.failure.shouldBeNull()
    }

    @Test
    fun `The content of a scheduled message maps to a message with its id, channel, author and dates`() {
        val response = randomScheduledMessageResponse(id = "scheduled-1", cid = "messaging:general", message = CONTENT)

        val message = with(domainMapping) { response.toDomain() }.message

        message.id shouldBeEqualTo "scheduled-1"
        message.cid shouldBeEqualTo "messaging:general"
        message.user.id shouldBeEqualTo response.user.id
        message.createdAt shouldBeEqualTo response.createdAt
        message.updatedAt shouldBeEqualTo response.updatedAt
        message.text shouldBeEqualTo "Good morning team"
        message.html shouldBeEqualTo "<p>Good morning team</p>"
        message.type shouldBeEqualTo "reply"
        message.parentId shouldBeEqualTo "parent-1"
        message.showInChannel shouldBeEqualTo true
        message.replyMessageId shouldBeEqualTo "quoted-1"
        message.attachments.single().imageUrl shouldBeEqualTo "https://example.com/image.png"
        message.mentionedUsersIds shouldBeEqualTo listOf("bob")
        message.mentionedHere shouldBeEqualTo true
        message.mentionedChannel shouldBeEqualTo true
        message.mentionedGroups shouldBeEqualTo listOf(UserGroup(id = "group-1", name = ""))
        message.mentionedRoles shouldBeEqualTo listOf("admin")
        message.pinned shouldBeEqualTo true
        message.pinExpires shouldBeEqualTo Date(PIN_EXPIRES)
        message.silent shouldBeEqualTo true
        message.restrictedVisibility shouldBeEqualTo listOf("bob")
        message.sharedLocation shouldBeEqualTo Location(
            cid = "messaging:general",
            messageId = "scheduled-1",
            userId = response.user.id,
            latitude = 1.5,
            longitude = 2.5,
            deviceId = "device-1",
        )
        message.extraData["mood"] shouldBeEqualTo "happy"
        message.extraData["poll_id"] shouldBeEqualTo "poll-1"
        message.extraData["mml"] shouldBeEqualTo "<mml/>"
    }

    @Test
    fun `The content of a scheduled message is sent back as it came`() {
        val response = randomScheduledMessageResponse(id = "scheduled-1", message = CONTENT)
        val message = with(domainMapping) { response.toDomain() }.message

        val request = with(dtoMapping) { message.toMessageRequest() }

        request.id shouldBeEqualTo "scheduled-1"
        request.text shouldBeEqualTo CONTENT.text
        request.parentId shouldBeEqualTo CONTENT.parentId
        request.showInChannel shouldBeEqualTo CONTENT.showInChannel
        request.quotedMessageId shouldBeEqualTo CONTENT.quotedMessageId
        request.attachments?.single()?.imageUrl shouldBeEqualTo "https://example.com/image.png"
        request.mentionedUsers shouldBeEqualTo CONTENT.mentionedUsers
        request.mentionedHere shouldBeEqualTo CONTENT.mentionedHere
        request.mentionedChannel shouldBeEqualTo CONTENT.mentionedChannel
        request.mentionedGroupIds shouldBeEqualTo CONTENT.mentionedGroupIds
        request.mentionedRoles shouldBeEqualTo CONTENT.mentionedRoles
        request.pinned shouldBeEqualTo CONTENT.pinned
        request.pinExpires shouldBeEqualTo CONTENT.pinExpires
        request.silent shouldBeEqualTo CONTENT.silent
        request.pollId shouldBeEqualTo CONTENT.pollId
        request.mml shouldBeEqualTo CONTENT.mml
        request.restrictedVisibility shouldBeEqualTo CONTENT.restrictedVisibility
        request.sharedLocation?.latitude shouldBeEqualTo 1.5
        request.custom shouldBeEqualTo mapOf("mood" to "happy")
    }

    @ParameterizedTest
    @MethodSource("statuses")
    fun `The status of a scheduled message is mapped`(
        dto: ScheduledMessageStatusDto,
        expected: ScheduledMessageStatus,
    ) {
        val response = randomScheduledMessageResponse(status = dto)

        with(domainMapping) { response.toDomain() }.status shouldBeEqualTo expected
    }

    @ParameterizedTest
    @MethodSource("failureReasons")
    fun `A failed scheduled message carries the publishing failure`(
        dto: ScheduledMessageFailureReason?,
        expected: ServerFailedPublishing.Reason?,
    ) {
        val response = randomScheduledMessageResponse(status = ScheduledMessageStatusDto.Failed, failureReason = dto)

        val failure = with(domainMapping) { response.toDomain() }.failure

        failure shouldBeEqualTo ServerFailedPublishing(expected)
        failure?.isRetryable shouldBeEqualTo false
    }

    @Test
    fun `Only a failed scheduled message has a failure`() {
        val response = randomScheduledMessageResponse(
            status = ScheduledMessageStatusDto.Sending,
            failureReason = ScheduledMessageFailureReason.Banned,
        )

        with(domainMapping) { response.toDomain() }.failure.shouldBeNull()
    }

    @Test
    fun `A query response maps its scheduled messages and next cursor`() {
        val responses = listOf(randomScheduledMessageResponse(), randomScheduledMessageResponse())

        val result = with(domainMapping) {
            QueryScheduledMessagesResponse(duration = "1ms", scheduledMessages = responses, next = "next-1").toDomain()
        }

        result.scheduledMessages.map { it.id } shouldBeEqualTo responses.map { it.id }
        result.next shouldBeEqualTo "next-1"
    }

    @Test
    fun `The schedule-message capability is a channel capability`() {
        val response = randomChannelResponse().copy(ownCapabilities = listOf(ChannelOwnCapability.ScheduleMessage))

        val channel = with(domainMapping) { response.toDomain() }

        channel.ownCapabilities shouldContain ChannelCapabilities.SCHEDULE_MESSAGE
    }

    @Test
    fun `A local failure can always be retried`() {
        val failure = ScheduledMessageFailure.LocalDeviceFailedScheduling(
            reason = ScheduledMessageFailure.LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed,
            message = null,
        )

        failure.isRetryable shouldBeEqualTo true
    }

    companion object {
        private const val PIN_EXPIRES = 1_791_529_200_000L

        private val CONTENT = ScheduledMessageContentResponse(
            text = "Good morning team",
            custom = mapOf("mood" to "happy"),
            html = "<p>Good morning team</p>",
            mentionedChannel = true,
            mentionedHere = true,
            mml = "<mml/>",
            parentId = "parent-1",
            pinExpires = Date(PIN_EXPIRES),
            pinned = true,
            pollId = "poll-1",
            quotedMessageId = "quoted-1",
            showInChannel = true,
            silent = true,
            type = "reply",
            attachments = listOf(Attachment(type = "image", imageUrl = "https://example.com/image.png")),
            mentionedGroupIds = listOf("group-1"),
            mentionedRoles = listOf("admin"),
            mentionedUsers = listOf("bob"),
            restrictedVisibility = listOf("bob"),
            sharedLocation = ScheduledMessageLocation(createdByDeviceId = "device-1", latitude = 1.5, longitude = 2.5),
        )

        @JvmStatic
        fun statuses() = listOf(
            Arguments.of(ScheduledMessageStatusDto.Scheduled, ScheduledMessageStatus.Scheduled),
            Arguments.of(ScheduledMessageStatusDto.Sending, ScheduledMessageStatus.Sending),
            Arguments.of(ScheduledMessageStatusDto.Failed, ScheduledMessageStatus.Failed),
            Arguments.of(ScheduledMessageStatusDto.Sent, ScheduledMessageStatus.Sent),
            Arguments.of(ScheduledMessageStatusDto.Canceled, ScheduledMessageStatus.Canceled),
            Arguments.of(ScheduledMessageStatusDto.Unknown("paused"), ScheduledMessageStatus.Unknown("paused")),
        )

        @JvmStatic
        fun failureReasons() = listOf(
            Arguments.of(ScheduledMessageFailureReason.Banned, ServerFailedPublishing.Reason.Banned),
            Arguments.of(ScheduledMessageFailureReason.ChannelDeleted, ServerFailedPublishing.Reason.ChannelDeleted),
            Arguments.of(ScheduledMessageFailureReason.ChannelFrozen, ServerFailedPublishing.Reason.ChannelFrozen),
            Arguments.of(ScheduledMessageFailureReason.Expired, ServerFailedPublishing.Reason.Expired),
            Arguments.of(ScheduledMessageFailureReason.HookRejected, ServerFailedPublishing.Reason.HookRejected),
            Arguments.of(ScheduledMessageFailureReason.InternalError, ServerFailedPublishing.Reason.InternalError),
            Arguments.of(ScheduledMessageFailureReason.InvalidMessage, ServerFailedPublishing.Reason.InvalidMessage),
            Arguments.of(
                ScheduledMessageFailureReason.ModerationBlocked,
                ServerFailedPublishing.Reason.ModerationBlocked,
            ),
            Arguments.of(ScheduledMessageFailureReason.NoPermission, ServerFailedPublishing.Reason.NoPermission),
            Arguments.of(
                ScheduledMessageFailureReason.PendingNotSupported,
                ServerFailedPublishing.Reason.PendingNotSupported,
            ),
            Arguments.of(ScheduledMessageFailureReason.UserDeleted, ServerFailedPublishing.Reason.UserDeleted),
            Arguments.of(
                ScheduledMessageFailureReason.Unknown("quota"),
                ServerFailedPublishing.Reason.Unknown("quota"),
            ),
            Arguments.of(null, null),
        )
    }
}
