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
@Suppress("MagicNumber")
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
     * A third participant in the thread.
     */
    public val participant3: User = User(id = "uid3", name = "Third participant")

    /**
     * Single thread with 2 participants (1 reply).
     */
    public val thread: Thread = Thread(
        activeParticipantCount = 2,
        cid = "cid",
        channel = Channel(),
        parentMessageId = "pmid1",
        parentMessage = Message(
            id = "pmid1",
            text = "Hey everyone, who's up for a group ride this Saturday morning?",
            replyCount = 1,
            user = participant1,
        ),
        createdByUserId = "uid2",
        createdBy = participant2,
        participantCount = 2,
        threadParticipants = listOf(
            ThreadParticipant(
                user = participant1,
                lastThreadMessageAt = null,
            ),
            ThreadParticipant(
                user = participant2,
                lastThreadMessageAt = null,
            ),
        ),
        lastMessageAt = Date(),
        createdAt = Date(),
        updatedAt = Date(),
        deletedAt = null,
        title = "Group ride preparation and discussion",
        latestReplies = listOf(
            Message(id = "mid1", text = "See you all there, stay safe on the roads!", user = participant2),
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
     * Single thread with 2 participants (2 replies, one from each).
     */
    public val thread2: Thread = Thread(
        activeParticipantCount = 2,
        cid = "cid",
        channel = Channel(),
        parentMessageId = "pmid2",
        parentMessage = Message(
            id = "pmid2",
            text = "Has anyone tried the new bike lane on River Road?",
            replyCount = 2,
            user = participant1,
        ),
        createdByUserId = "uid2",
        createdBy = participant2,
        participantCount = 2,
        threadParticipants = listOf(
            ThreadParticipant(
                user = participant2,
                lastThreadMessageAt = Date(1_735_700_000_000),
            ),
            ThreadParticipant(
                user = participant1,
                lastThreadMessageAt = Date(1_735_690_000_000),
            ),
        ),
        lastMessageAt = Date(),
        createdAt = Date(),
        updatedAt = Date(),
        deletedAt = null,
        title = "New bike lane discussion",
        latestReplies = listOf(
            Message(id = "mid2", text = "Yes, it's smooth but a bit narrow near the bridge.", user = participant1),
            Message(id = "mid3", text = "Agreed, watch out for pedestrians around the park exit.", user = participant2),
        ),
        read = listOf(
            ChannelUserRead(
                user = participant2,
                lastReceivedEventDate = Date(),
                unreadMessages = 1,
                lastRead = Date(),
                lastReadMessageId = null,
            ),
        ),
        draft = null,
    )

    /**
     * Single thread with 3 participants (3 replies, one from each).
     */
    public val thread3: Thread = Thread(
        activeParticipantCount = 3,
        cid = "cid",
        channel = Channel(),
        parentMessageId = "pmid3",
        parentMessage = Message(
            id = "pmid3",
            text = "What snacks should we bring for the trail ride next weekend?",
            replyCount = 3,
            user = participant1,
        ),
        createdByUserId = "uid2",
        createdBy = participant2,
        participantCount = 3,
        threadParticipants = listOf(
            ThreadParticipant(
                user = participant3,
                lastThreadMessageAt = Date(1_735_710_000_000),
            ),
            ThreadParticipant(
                user = participant2,
                lastThreadMessageAt = Date(1_735_700_000_000),
            ),
            ThreadParticipant(
                user = participant1,
                lastThreadMessageAt = Date(1_735_690_000_000),
            ),
        ),
        lastMessageAt = Date(),
        createdAt = Date(),
        updatedAt = Date(),
        deletedAt = null,
        title = "Trail ride snack planning",
        latestReplies = listOf(
            Message(id = "mid4", text = "Energy bars and bananas are always a safe bet.", user = participant1),
            Message(id = "mid5", text = "I'll bring some trail mix and electrolyte drinks.", user = participant2),
            Message(id = "mid6", text = "Count me in for sandwiches, easy to carry.", user = participant3),
        ),
        read = listOf(
            ChannelUserRead(
                user = participant2,
                lastReceivedEventDate = Date(),
                unreadMessages = 2,
                lastRead = Date(),
                lastReadMessageId = null,
            ),
        ),
        draft = null,
    )

    /**
     * List of threads.
     */
    public val threadList: List<Thread> = listOf(thread, thread2, thread3)
}
