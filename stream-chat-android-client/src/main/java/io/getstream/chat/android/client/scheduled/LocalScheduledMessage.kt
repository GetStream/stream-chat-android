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

import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageFailure
import io.getstream.chat.android.models.ScheduledMessageStatus
import java.util.Date

/**
 * A scheduled message as the local store keeps it: its latest content, the state the server last reported, and the
 * changes the server does not have yet. The local content wins until its changes reach the server.
 *
 * @property serverStatus The status the server last reported, or [ScheduledMessageStatus.Scheduled] while the message
 * is not on the server yet.
 * @property publishingFailure Why the server could not send the message, when [serverStatus] is failed.
 * @property pendingChange The change to send to the server, or null when the server has the latest content.
 * @property hasPendingScheduledAt Whether [scheduledAt] changed after the message reached the server, or may have.
 * @property isPendingSendNow Whether the message is sent now, once its pending change is on the server.
 * @property isDeleted Whether the message was deleted. It is deleted on the server if the server may have it, then
 * removed. It is hidden, unless deleting it on the server failed.
 * @property isCreateSent Whether a create was sent with no answer that the server did not store it, so the server
 * may have the message even though its create is still pending.
 * @property localFailure Why the latest changes could not reach the server. Nothing is sent until they are resent.
 */
internal data class LocalScheduledMessage(
    val id: String,
    val cid: String,
    val channel: ChannelInfo?,
    val message: Message,
    val scheduledAt: Date,
    val serverStatus: ScheduledMessageStatus,
    val publishingFailure: ScheduledMessageFailure.ServerFailedPublishing?,
    val pendingChange: PendingChange?,
    val hasPendingScheduledAt: Boolean,
    val isPendingSendNow: Boolean,
    val isDeleted: Boolean,
    val isCreateSent: Boolean,
    val localFailure: ScheduledMessageFailure?,
    val createdAt: Date,
    val updatedAt: Date,
) {

    /**
     * A change of the scheduled message the server does not have yet.
     */
    enum class PendingChange {
        /** The message is not on the server: it is scheduled with its latest content. */
        Create,

        /** The message is on the server: its latest content replaces the one on the server. */
        Update,
    }

    /** Whether the message is not on the server yet. */
    val isLocalOnly: Boolean
        get() = pendingChange == PendingChange.Create

    /** Whether the server may have the message. */
    val mayBeOnServer: Boolean
        get() = !isLocalOnly || isCreateSent

    /** Whether there is something to send to the server, or a deleted message to remove. */
    val hasPendingRequest: Boolean
        get() = (isDeleted && !mayBeOnServer) ||
            (localFailure == null && (isDeleted || pendingChange != null || isPendingSendNow))

    /**
     * The scheduled message to show. The latest local failure wins over a failure to publish: once the local changes
     * are resolved, the failure to publish shows again.
     */
    fun toScheduledMessage(): ScheduledMessage = ScheduledMessage(
        id = id,
        cid = cid,
        channel = channel,
        message = message,
        scheduledAt = scheduledAt,
        status = when {
            localFailure != null -> ScheduledMessageStatus.Failed
            pendingChange != null || isPendingSendNow -> ScheduledMessageStatus.PreparingToSchedule
            else -> serverStatus
        },
        failure = localFailure ?: publishingFailure,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}

/**
 * Saves the server version of a scheduled message into the local one, if any. The local content and date win while
 * they have changes the server does not have yet, and the pending requests and local failure are kept.
 */
internal fun LocalScheduledMessage?.mergeServer(server: ScheduledMessage): LocalScheduledMessage {
    val fromServer = LocalScheduledMessage(
        id = server.id,
        cid = server.cid,
        channel = server.channel ?: this?.channel,
        message = server.message,
        scheduledAt = server.scheduledAt,
        serverStatus = server.status,
        publishingFailure = server.failure as? ScheduledMessageFailure.ServerFailedPublishing,
        pendingChange = null,
        hasPendingScheduledAt = false,
        isPendingSendNow = false,
        isDeleted = false,
        isCreateSent = false,
        localFailure = null,
        createdAt = server.createdAt,
        updatedAt = server.updatedAt,
    )
    return when {
        this == null -> fromServer
        pendingChange == null -> fromServer.copy(
            isPendingSendNow = isPendingSendNow,
            isDeleted = isDeleted,
            localFailure = localFailure,
        )
        else -> copy(
            channel = fromServer.channel,
            serverStatus = fromServer.serverStatus,
            publishingFailure = fromServer.publishingFailure,
            createdAt = fromServer.createdAt,
        )
    }
}
