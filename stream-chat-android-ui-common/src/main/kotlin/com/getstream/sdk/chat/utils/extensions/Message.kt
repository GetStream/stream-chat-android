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

package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.Date

/**
 * @return if the message has been deleted.
 */
@InternalStreamChatApi
internal fun Message.isDeleted(): Boolean = deletedAt != null

/**
 * @return when the message was created or throw an exception.
 */
public fun Message.getCreatedAtOrThrow(): Date {
    val created = createdAt ?: createdLocallyAt
    return checkNotNull(created) { "a message needs to have a non null value for either createdAt or createdLocallyAt" }
}

/**
 * @return when the message was created or null.
 */
public fun Message.getCreatedAtOrNull(): Date? {
    return createdAt ?: createdLocallyAt
}

public fun Message.syncStatusWithAttachments(): SyncStatus {
    val reducedSyncStatus = attachments.map { it.uploadState }
        .reduce(::accumulateUploadState) ?: Attachment.UploadState.Idle

    return when (reducedSyncStatus) {
        is Attachment.UploadState.Failed -> SyncStatus.FAILED_PERMANENTLY
        Attachment.UploadState.Idle -> this.syncStatus
        is Attachment.UploadState.InProgress -> SyncStatus.IN_PROGRESS
        Attachment.UploadState.Success -> this.syncStatus
    }
}

private fun accumulateUploadState(
    acc: Attachment.UploadState?,
    newState: Attachment.UploadState?,
): Attachment.UploadState {
    return when {
        acc is Attachment.UploadState.Failed -> acc

        newState is Attachment.UploadState.Failed -> newState

        newState is Attachment.UploadState.InProgress -> newState

        acc is Attachment.UploadState.InProgress -> acc

        else -> newState ?: Attachment.UploadState.Idle
    }
}

