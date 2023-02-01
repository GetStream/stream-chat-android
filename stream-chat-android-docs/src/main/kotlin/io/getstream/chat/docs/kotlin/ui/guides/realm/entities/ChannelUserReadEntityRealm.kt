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

package io.getstream.chat.docs.kotlin.ui.guides.realm.entities

import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.User
import io.getstream.chat.docs.kotlin.ui.guides.realm.utils.toDate
import io.getstream.chat.docs.kotlin.ui.guides.realm.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject

@Suppress("VariableNaming")
internal class ChannelUserReadEntityRealm : RealmObject {
    var user: UserEntityRealm? = null
    var last_read: RealmInstant? = null
    var unread_messages: Int? = null
    var last_message_seen_date: RealmInstant? = null
}

internal fun ChannelUserReadEntityRealm.toDomain(): ChannelUserRead =
    ChannelUserRead(
        user = user?.toDomain() ?: User(),
        lastRead = last_read?.toDate(),
        unreadMessages = unread_messages ?: 0,
        lastMessageSeenDate = last_message_seen_date?.toDate(),
    )

internal fun ChannelUserRead.toRealm(): ChannelUserReadEntityRealm {
    val thisChannelRead: ChannelUserRead = this

    return ChannelUserReadEntityRealm().apply {
        user = thisChannelRead.user.toRealm()
        last_read = thisChannelRead.lastRead?.toRealmInstant()
        unread_messages = thisChannelRead.unreadMessages
        last_message_seen_date = thisChannelRead.lastMessageSeenDate?.toRealmInstant()
    }
}
