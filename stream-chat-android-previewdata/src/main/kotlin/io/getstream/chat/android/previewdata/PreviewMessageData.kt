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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageType
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ReactionGroup
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * Provides sample messages that will be used to render previews.
 */
@OptIn(InternalStreamChatApi::class)
public object PreviewMessageData {

    private const val CREATION_GAP_MILLIS = 1_000L
    private var lastCreatedAt = Date()

    /**
     * Returns a creation date one second after the previous one, so the relative order of the
     * sample messages is deterministic instead of depending on initialization timing.
     */
    private fun nextCreatedAt(): Date {
        lastCreatedAt = Date(lastCreatedAt.time + CREATION_GAP_MILLIS)
        return lastCreatedAt
    }

    public val message1: Message = Message(
        id = "message-1",
        text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.",
        createdAt = nextCreatedAt(),
        type = MessageType.REGULAR,
    )

    public val message2: Message = Message(
        id = "message-2",
        text = "Aenean commodo ligula eget dolor.",
        createdAt = nextCreatedAt(),
        type = MessageType.REGULAR,
    )

    public val message3: Message = Message(
        id = "message-3",
        text = "Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.",
        createdAt = nextCreatedAt(),
        type = MessageType.REGULAR,
    )

    public val message4: Message = Message(
        id = "message-4",
        text = "Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem.",
        createdAt = nextCreatedAt(),
        type = MessageType.REGULAR,
    )

    public val message5: Message = Message(
        id = "message-5",
        text = "Nulla consequat massa quis enim.",
        createdAt = nextCreatedAt(),
        type = MessageType.REGULAR,
    )

    public val messageWithOwnReaction: Message = Message(
        id = "message-with-own-reaction",
        text = "Pellentesque leo dui, finibus et nibh et, congue aliquam lectus",
        createdAt = nextCreatedAt(),
        type = MessageType.REGULAR,
        ownReactions = mutableListOf(Reaction(messageId = "message-with-own-reaction", type = "haha")),
        reactionGroups = mutableMapOf(
            "haha" to ReactionGroup(
                type = "haha",
                count = 1,
                sumScore = 1,
                firstReactionAt = Date(),
                lastReactionAt = Date(),
            ),
        ),
    )

    public val messageWithError: Message = Message(
        id = "message-with-error",
        text = "Lorem ipsum dolor sqit amet, consectetuer adipiscing elit.",
        createdAt = nextCreatedAt(),
        type = MessageType.ERROR,
    )

    public val messageWithPoll: Message = Message(
        id = "message-with-poll",
        createdAt = nextCreatedAt(),
        type = MessageType.REGULAR,
        poll = PreviewPollData.poll1,
    )

    public val messageDeleted: Message = Message(
        id = "message-deleted",
        text = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex commodo.",
        deletedAt = Date(),
    )

    public val messageWithMention: Message = Message(
        id = "message-with-mention",
        text = "@André Rêgo adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        createdAt = nextCreatedAt(),
        type = MessageType.REGULAR,
        mentionedUsers = listOf(PreviewUserData.user7),
    )

    public val messageWithUserAndAttachment: Message = Message(
        id = "message-with-user-and-attachment",
        text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.",
        type = MessageType.REGULAR,
        user = User(id = "test-user", name = "Test User"),
        attachments = listOf(
            PreviewAttachmentData.attachmentImage1,
            PreviewAttachmentData.attachmentVideo1,
        ),
        createdAt = nextCreatedAt(),
    )

    public val draftMessage: DraftMessage = DraftMessage(
        id = "draft-message",
        cid = "channel-id",
        text = "Some text for the draft message",
    )
}
