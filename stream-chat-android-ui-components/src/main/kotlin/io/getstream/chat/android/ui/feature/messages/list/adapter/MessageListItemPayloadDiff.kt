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

package io.getstream.chat.android.ui.feature.messages.list.adapter

public data class MessageListItemPayloadDiff(
    val text: Boolean,
    val replyText: Boolean,
    val reactions: Boolean,
    val attachments: Boolean,
    val replies: Boolean,
    val syncStatus: Boolean,
    val deleted: Boolean,
    val positions: Boolean,
    val pinned: Boolean,
    val user: Boolean,
    val mentions: Boolean,
    val footer: Boolean,
    val poll: Boolean,
) {
    public operator fun plus(other: MessageListItemPayloadDiff): MessageListItemPayloadDiff {
        return MessageListItemPayloadDiff(
            text = text || other.text,
            replyText = replyText || other.replyText,
            reactions = reactions || other.reactions,
            attachments = attachments || other.attachments,
            replies = replies || other.replies,
            syncStatus = syncStatus || other.syncStatus,
            deleted = deleted || other.deleted,
            positions = positions || other.positions,
            pinned = pinned || other.pinned,
            user = user || other.user,
            mentions = mentions || other.mentions,
            footer = footer || other.footer,
            poll = poll || other.poll,
        )
    }
}
