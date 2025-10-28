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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.models.Attachment
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
public object PreviewMessageData {
    public val message1: Message =
        Message(
            id = "message-1",
            text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.",
            createdAt = Date(),
            type = MessageType.REGULAR,
        )

    public val message2: Message =
        Message(
            id = "message-2",
            text = "Aenean commodo ligula eget dolor.",
            createdAt = Date(),
            type = MessageType.REGULAR,
        )

    public val message3: Message =
        Message(
            id = "message-3",
            text = "Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus.",
            createdAt = Date(),
            type = MessageType.REGULAR,
        )

    public val message4: Message =
        Message(
            id = "message-4",
            text = "Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem.",
            createdAt = Date(),
            type = MessageType.REGULAR,
        )

    public val message5: Message =
        Message(
            id = "message-5",
            text = "Nulla consequat massa quis enim.",
            createdAt = Date(),
            type = MessageType.REGULAR,
        )

    public val messageWithOwnReaction: Message =
        Message(
            id = "message-with-own-reaction",
            text = "Pellentesque leo dui, finibus et nibh et, congue aliquam lectus",
            createdAt = Date(),
            type = MessageType.REGULAR,
            ownReactions = mutableListOf(Reaction(messageId = "message-with-own-reaction", type = "haha")),
            reactionGroups =
            mutableMapOf(
                "haha" to
                    ReactionGroup(
                        type = "haha",
                        count = 1,
                        sumScore = 1,
                        firstReactionAt = Date(),
                        lastReactionAt = Date(),
                    ),
            ),
        )

    public val messageWithError: Message =
        Message(
            id = "message-with-error",
            text = "Lorem ipsum dolor sqit amet, consectetuer adipiscing elit.",
            createdAt = Date(),
            type = MessageType.ERROR,
        )

    public val messageWithPoll: Message =
        Message(
            id = "message-with-poll",
            createdAt = Date(),
            type = MessageType.REGULAR,
            poll = PreviewPollData.poll1,
        )

    public val messageDeleted: Message =
        Message(
            id = "message-deleted",
            text = "Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex commodo.",
            deletedAt = Date(),
        )

    public val messageWithMention: Message =
        Message(
            id = "message-with-mention",
            text = "@André Rêgo adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            createdAt = Date(),
            type = MessageType.REGULAR,
            mentionedUsers = listOf(PreviewUserData.user7),
        )

    public val messageWithUserAndAttachment: Message =
        Message(
            id = "message-with-user-and-attachment",
            text = "Lorem ipsum dolor sit amet, consectetuer adipiscing elit.",
            type = MessageType.REGULAR,
            user = User(id = "test-user", name = "Test User"),
            attachments =
            listOf(
                Attachment(
                    name = "image1.jpg",
                    fileSize = 2000000,
                    type = "image",
                    mimeType = "image/jpeg",
                    imageUrl = "https://example.com/image1.jpg",
                    thumbUrl = "https://example.com/thumb1.jpg",
                ),
                Attachment(
                    name = "video1.mp4",
                    fileSize = 2000000,
                    type = "video",
                    mimeType = "video/mp4",
                    imageUrl = "https://example.com/image1.jpg",
                    thumbUrl = "https://example.com/thumb1.jpg",
                ),
            ),
            createdAt = Date(),
        )

    public val draftMessage: DraftMessage =
        DraftMessage(
            id = "draft-message",
            cid = "channel-id",
            text = "Some text for the draft message",
        )
}
