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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User

/** Updates collection of members with more recent data of [users]. */
@InternalStreamChatApi
public fun Collection<Member>.updateUsers(userMap: Map<String, User>): Collection<Member> = map { member ->
    if (userMap.containsKey(member.getUserId())) {
        member.copy(user = userMap[member.getUserId()] ?: member.user)
    } else {
        member
    }
}

/**
 * Narrows a full [Member] down to the slim [MemberInfo] carried by [Message.member].
 *
 * [Member.extraData] holds every key the member DTO does not declare, which includes `user_id` and the deprecated
 * member-level `role` that channel payloads send next to `channel_role`. The projection the backend puts on
 * `message.member` carries neither, so both are dropped here to keep [MemberInfo.extraData] identical no matter
 * whether it came from a message payload or from a member event.
 */
@InternalStreamChatApi
public fun Member.toMemberInfo(): MemberInfo = MemberInfo(
    channelRole = channelRole,
    notificationsMuted = notificationsMuted ?: false,
    extraData = extraData - NON_CUSTOM_MEMBER_KEYS,
)

/** Keys that reach [Member.extraData] only because the member DTO does not declare them. */
private val NON_CUSTOM_MEMBER_KEYS = setOf("user_id", "role")
