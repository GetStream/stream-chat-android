/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Thread
import io.getstream.chat.android.models.ThreadParticipant
import io.getstream.chat.android.models.User
import java.util.Date

/**
 * Provides sample thread data that will be used to render component previews.
 */
public object PreviewThreadData {

    /**
     * A participant in the thread.
     */
    public val participant1: User = User(id = "uid1", name = "First participant")

    /**
     * Another participant in the thread.
     */
    public val participant2: User = User(id = "uid2", name = "Second participant")

    /**
     * Single thread with 2 participants.
     */
    public val thread: Thread = Thread(
        activeParticipantCount = 2,
        cid = "cid",
        channel = Channel(),
        parentMessageId = "pmid1",
        parentMessage = Message(
            id = "pmid1",
            text = "Hey everyone, who's up for a group ride this Saturday morning?",
        ),
        createdByUserId = "uid2",
        createdBy = participant2,
        participantCount = 2,
        threadParticipants = listOf(
            ThreadParticipant(participant1),
            ThreadParticipant(participant2),
        ),
        lastMessageAt = Date(),
        createdAt = Date(),
        updatedAt = Date(),
        deletedAt = null,
        title = "Group ride preparation and discussion",
        latestReplies = listOf(
            Message(id = "mid1", text = "See you all there, stay safe on the roads!", user = participant1),
        ),
        read = listOf(
            ChannelUserRead(
                user = participant2,
                lastReceivedEventDate = Date(),
                unreadMessages = 3,
                lastRead = Date(),
                lastReadMessageId = null,
            ),
        ),
        draft = null,
    )

    /**
     * Single thread with 2 participants.
     */
    public val thread2: Thread = Thread(
        activeParticipantCount = 2,
        cid = "cid",
        channel = Channel(),
        parentMessageId = "pmid2",
        parentMessage = Message(
            id = "pmid2",
            text = "Hello hello!",
        ),
        createdByUserId = "uid2",
        createdBy = participant1,
        participantCount = 2,
        threadParticipants = listOf(
            ThreadParticipant(participant1),
            ThreadParticipant(participant2),
        ),
        lastMessageAt = Date(),
        createdAt = Date(),
        updatedAt = Date(),
        deletedAt = null,
        title = "Group ride preparation and discussion",
        latestReplies = listOf(
            Message(id = "mid1", text = "Welcome to the group!", user = participant2),
        ),
        read = listOf(
            ChannelUserRead(
                user = participant1,
                lastReceivedEventDate = Date(),
                unreadMessages = 3,
                lastRead = Date(),
                lastReadMessageId = null,
            ),
        ),
        draft = null,
    )

    /**
     * List of threads.
     */
    public val threadList: List<Thread> = listOf(thread, thread2)
}
