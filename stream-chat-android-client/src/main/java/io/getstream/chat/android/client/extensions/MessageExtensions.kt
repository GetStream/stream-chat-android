/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.utils.internal.toMessageSyncDescription
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.result.StreamError
import java.util.Date

public fun Message.enrichWithCid(cid: String): Message = apply {
    replyTo?.enrichWithCid(cid)
    this.cid = cid
}

/**
 * Updates a message that whose request (Edition/Delete/Reaction update...) has failed.
 *
 * @param streamError [StreamError].
 */
@InternalStreamChatApi
public fun Message.updateFailedMessage(streamError: StreamError): Message {
    return this.copy(
        syncStatus = if (streamError.isPermanent()) {
            SyncStatus.FAILED_PERMANENTLY
        } else {
            SyncStatus.SYNC_NEEDED
        },
        syncDescription = streamError.toMessageSyncDescription(),
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
        updatedLocallyAt = Date()
    )
}
