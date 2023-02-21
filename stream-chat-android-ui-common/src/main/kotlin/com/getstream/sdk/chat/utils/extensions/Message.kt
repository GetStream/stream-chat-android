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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.MessageSyncType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.Date

/**
 * @return if the message has been deleted.
 */
@InternalStreamChatApi
internal fun Message.isDeleted(): Boolean = deletedAt != null

/**
 * @return if the message was sent by current user.
 */
@InternalStreamChatApi
public fun Message.isMine(chatClient: ChatClient): Boolean = chatClient.getCurrentUser()?.id == user.id

@InternalStreamChatApi
public fun Message.isMine(currentUser: User?): Boolean = currentUser?.id == user.id

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

/**
 * @return if the message failed at moderation or not.
 */
@Deprecated(
    "use isModerationFailed version with `currentUser: User?` parameter",
    ReplaceWith(
        "message.isModerationFailed(currentUser = chatClient.getCurrentUser())",
        "io.getstream.chat.android.client.models.User"
    )
)
public fun Message.isModerationFailed(chatClient: ChatClient): Boolean = isMine(chatClient) &&
    syncStatus == SyncStatus.FAILED_PERMANENTLY &&
    syncDescription?.type == MessageSyncType.FAILED_MODERATION

/**
 * @return if the message failed at moderation or not.
 */
public fun Message.isModerationFailed(currentUser: User?): Boolean = isMine(currentUser) &&
    syncStatus == SyncStatus.FAILED_PERMANENTLY &&
    syncDescription?.type == MessageSyncType.FAILED_MODERATION
