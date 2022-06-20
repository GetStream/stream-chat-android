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

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.util.Date

public fun Message.enrichWithCid(cid: String): Message = apply {
    replyTo?.enrichWithCid(cid)
    this.cid = cid
}

/**
 * Checks if the message mentions the [user].
 */
internal fun Message.containsUserMention(user: User): Boolean {
    return mentionedUsersIds.contains(user.id) || mentionedUsers.any { mentionedUser -> mentionedUser.id == user.id }
}

/**
 * Check if the message was created after the given [date].
 */
@InternalStreamChatApi
public fun Message.wasCreatedAfter(date: Date?): Boolean {
    return createdAt?.time ?: createdLocallyAt?.time ?: 0 > date?.time ?: 0
}
