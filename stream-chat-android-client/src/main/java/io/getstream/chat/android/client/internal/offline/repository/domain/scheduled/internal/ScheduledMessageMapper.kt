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

package io.getstream.chat.android.client.internal.offline.repository.domain.scheduled.internal

import io.getstream.chat.android.client.errors.scheduledMessageRejectionReasons
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.AttachmentEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.attachment.internal.toModel
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.toEntity
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.toModel
import io.getstream.chat.android.client.scheduled.LocalScheduledMessage
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageFailure.LocalDeviceFailedScheduling
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerFailedPublishing
import io.getstream.chat.android.models.ScheduledMessageFailure.ServerRejectedScheduling
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.models.User

private const val LOCAL_FAILURE_SERVER_REJECTED = "server_rejected"
private const val LOCAL_FAILURE_LOCAL_DEVICE = "local_device"

private val STATUSES = listOf(
    ScheduledMessageStatus.PreparingToSchedule,
    ScheduledMessageStatus.Scheduled,
    ScheduledMessageStatus.Sending,
    ScheduledMessageStatus.Failed,
    ScheduledMessageStatus.Sent,
    ScheduledMessageStatus.Canceled,
)

private val PUBLISHING_FAILURE_REASONS = listOf(
    ServerFailedPublishing.Reason.Banned,
    ServerFailedPublishing.Reason.ChannelDeleted,
    ServerFailedPublishing.Reason.ChannelFrozen,
    ServerFailedPublishing.Reason.Expired,
    ServerFailedPublishing.Reason.HookRejected,
    ServerFailedPublishing.Reason.InternalError,
    ServerFailedPublishing.Reason.InvalidMessage,
    ServerFailedPublishing.Reason.ModerationBlocked,
    ServerFailedPublishing.Reason.NoPermission,
    ServerFailedPublishing.Reason.PendingNotSupported,
    ServerFailedPublishing.Reason.UserDeleted,
)

private val LOCAL_DEVICE_FAILURE_REASONS = listOf(
    LocalDeviceFailedScheduling.Reason.AttachmentUploadFailed,
    LocalDeviceFailedScheduling.Reason.RequestFailed,
)

internal fun LocalScheduledMessage.toEntity(): ScheduledMessageEntity = ScheduledMessageEntity(
    id = id,
    cid = cid,
    channelName = channel?.name,
    channelImage = channel?.image,
    userId = message.user.id,
    text = message.text,
    html = message.html,
    type = message.type,
    parentId = message.parentId,
    showInChannel = message.showInChannel,
    replyMessageId = message.replyMessageId,
    attachments = message.attachments.mapIndexed { index, attachment -> attachment.toEntity(id, index) },
    mentionedUsersIds = message.mentionedUsersIds,
    mentionedHere = message.mentionedHere,
    mentionedChannel = message.mentionedChannel,
    mentionedGroups = message.mentionedGroups,
    mentionedRoles = message.mentionedRoles,
    pinned = message.pinned,
    pinnedAt = message.pinnedAt,
    pinExpires = message.pinExpires,
    silent = message.silent,
    restrictedVisibility = message.restrictedVisibility,
    sharedLocation = message.sharedLocation?.toEntity(),
    extraData = message.extraData,
    skipPushNotification = message.skipPushNotification,
    skipEnrichUrl = message.skipEnrichUrl,
    scheduledAt = scheduledAt,
    serverStatus = serverStatus.value,
    publishingFailureReason = publishingFailure?.reason?.value,
    hasPublishingFailure = publishingFailure != null,
    pendingChange = pendingChange?.name,
    hasPendingScheduledAt = hasPendingScheduledAt,
    isPendingSendNow = isPendingSendNow,
    isDeleted = isDeleted,
    isCreateSent = isCreateSent,
    localFailureType = when (localFailure) {
        is ServerRejectedScheduling -> LOCAL_FAILURE_SERVER_REJECTED
        is LocalDeviceFailedScheduling -> LOCAL_FAILURE_LOCAL_DEVICE
        else -> null
    },
    localFailureReason = when (localFailure) {
        is ServerRejectedScheduling -> localFailure.reason?.value
        is LocalDeviceFailedScheduling -> localFailure.reason.value
        else -> null
    },
    localFailureMessage = when (localFailure) {
        is ServerRejectedScheduling -> localFailure.message
        is LocalDeviceFailedScheduling -> localFailure.message
        else -> null
    },
    createdAt = createdAt,
    updatedAt = updatedAt,
)

internal suspend fun ScheduledMessageEntity.toModel(getUser: suspend (userId: String) -> User): LocalScheduledMessage =
    LocalScheduledMessage(
        id = id,
        cid = cid,
        channel = toChannelInfo(),
        message = Message(
            id = id,
            cid = cid,
            user = getUser(userId),
            text = text,
            html = html,
            type = type,
            parentId = parentId,
            showInChannel = showInChannel,
            replyMessageId = replyMessageId,
            // The ID only keys the attachment rows of the messages table: it is not part of the content.
            attachments = attachments.map { entity ->
                entity.toModel().let { it.copy(extraData = it.extraData - AttachmentEntity.EXTRA_DATA_ID_KEY) }
            },
            mentionedUsersIds = mentionedUsersIds,
            mentionedHere = mentionedHere,
            mentionedChannel = mentionedChannel,
            mentionedGroups = mentionedGroups,
            mentionedRoles = mentionedRoles,
            pinned = pinned,
            pinnedAt = pinnedAt,
            pinExpires = pinExpires,
            silent = silent,
            restrictedVisibility = restrictedVisibility,
            sharedLocation = sharedLocation?.toModel(),
            extraData = extraData,
            skipPushNotification = skipPushNotification,
            skipEnrichUrl = skipEnrichUrl,
            createdAt = createdAt,
            updatedAt = updatedAt,
        ),
        scheduledAt = scheduledAt,
        serverStatus = STATUSES.find { it.value == serverStatus } ?: ScheduledMessageStatus.Unknown(serverStatus),
        publishingFailure = toPublishingFailure(),
        pendingChange = pendingChange?.let(LocalScheduledMessage.PendingChange::valueOf),
        hasPendingScheduledAt = hasPendingScheduledAt,
        isPendingSendNow = isPendingSendNow,
        isDeleted = isDeleted,
        isCreateSent = isCreateSent,
        localFailure = toLocalFailure(),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

private fun ScheduledMessageEntity.toChannelInfo(): ChannelInfo? =
    if (channelName != null || channelImage != null) {
        val (type, channelId) = cid.cidToTypeAndId()
        ChannelInfo(cid = cid, id = channelId, type = type, name = channelName, image = channelImage)
    } else {
        null
    }

private fun ScheduledMessageEntity.toPublishingFailure(): ServerFailedPublishing? =
    if (hasPublishingFailure) {
        ServerFailedPublishing(
            reason = publishingFailureReason?.let { value ->
                PUBLISHING_FAILURE_REASONS.find { it.value == value } ?: ServerFailedPublishing.Reason.Unknown(value)
            },
        )
    } else {
        null
    }

private fun ScheduledMessageEntity.toLocalFailure(): ScheduledMessageFailure? = when (localFailureType) {
    LOCAL_FAILURE_SERVER_REJECTED -> ServerRejectedScheduling(
        reason = scheduledMessageRejectionReasons.find { it.value == localFailureReason },
        message = localFailureMessage.orEmpty(),
    )
    LOCAL_FAILURE_LOCAL_DEVICE -> LocalDeviceFailedScheduling(
        reason = LOCAL_DEVICE_FAILURE_REASONS.find { it.value == localFailureReason }
            ?: LocalDeviceFailedScheduling.Reason.RequestFailed,
        message = localFailureMessage,
    )
    else -> null
}
