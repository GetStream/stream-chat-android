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

package io.getstream.chat.android.ui.feature.channels.list.adapter

public data class ChannelListPayloadDiff(
    val nameChanged: Boolean,
    val avatarViewChanged: Boolean,
    val usersChanged: Boolean,
    val lastMessageChanged: Boolean,
    val readStateChanged: Boolean,
    val unreadCountChanged: Boolean,
    val extraDataChanged: Boolean,
    val typingUsersChanged: Boolean,
    val draftMessageChanged: Boolean,
) {
    public fun hasDifference(): Boolean = nameChanged
        .or(avatarViewChanged)
        .or(usersChanged)
        .or(lastMessageChanged)
        .or(readStateChanged)
        .or(unreadCountChanged)
        .or(extraDataChanged)
        .or(typingUsersChanged)
        .or(draftMessageChanged)

    public operator fun plus(other: ChannelListPayloadDiff): ChannelListPayloadDiff = copy(
        nameChanged = nameChanged || other.nameChanged,
        avatarViewChanged = avatarViewChanged || other.avatarViewChanged,
        usersChanged = usersChanged || other.usersChanged,
        lastMessageChanged = lastMessageChanged || other.lastMessageChanged,
        readStateChanged = readStateChanged || other.readStateChanged,
        unreadCountChanged = unreadCountChanged || other.unreadCountChanged,
        extraDataChanged = extraDataChanged || other.extraDataChanged,
        typingUsersChanged = typingUsersChanged || other.typingUsersChanged,
        draftMessageChanged = draftMessageChanged || other.draftMessageChanged,
    )
}
