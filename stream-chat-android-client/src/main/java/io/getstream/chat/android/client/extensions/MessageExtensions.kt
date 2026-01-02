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

package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.errors.isPermanent
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.result.Error
import java.util.Date

/**
 * Enriches the message with the given [newCid].
 * Sets the [Message.cid] and the [Message.replyTo] cid to the [newCid] value.
 */
public fun Message.enrichWithCid(newCid: String): Message = copy(
    replyTo = replyTo?.enrichWithCid(newCid),
    cid = newCid,
)

/**
 * Updates a message that whose request (Edition/Delete/Reaction update...) has failed.
 *
 * @param error [Error].
 */
@InternalStreamChatApi
public fun Message.updateFailedMessage(error: Error): Message {
    return this.copy(
        syncStatus = if (error.isPermanent()) {
            SyncStatus.FAILED_PERMANENTLY
        } else {
            SyncStatus.SYNC_NEEDED
        },
        updatedLocallyAt = Date(),
    )
}

/**
 * Update the online state of a message.
 *
 * @param isOnline [Boolean].
 */
@InternalStreamChatApi
public fun Message.updateMessageOnlineState(isOnline: Boolean): Message {
    return this.copy(
        syncStatus = if (isOnline) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
        updatedLocallyAt = Date(),
    )
}

/**
 * @return when the message was created or throw an exception.
 */
public fun Message.getCreatedAtOrThrow(): Date {
    val created = getCreatedAtOrNull()
    return checkNotNull(created) { "a message needs to have a non null value for either createdAt or createdLocallyAt" }
}

/**
 * @return when the message was created or null.
 */
public fun Message.getCreatedAtOrNull(): Date? {
    return createdLocallyAt ?: createdAt
}

/**
 * @return when the message was created or `default`.
 */
public fun Message.getCreatedAtOrDefault(default: Date): Date {
    return getCreatedAtOrNull() ?: default
}
