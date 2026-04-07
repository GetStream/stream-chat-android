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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Option
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.VotingVisibility
import org.intellij.lang.annotations.Language
import java.util.Date

internal object MessageTestData {

    @Language("JSON")
    val jsonAllFields = """{
        "id": "msg-nested",
        "cid": "messaging:general",
        "text": "Message with nested objects @user-2",
        "html": "<p>Message with nested objects @user-2</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [
            {
                "type": "image",
                "asset_url": "https://example.com/image.png",
                "image_url": "https://example.com/image.png",
                "thumb_url": "https://example.com/thumb.png",
                "name": "photo.png",
                "file_size": 2048,
                "mime_type": "image/png"
            }
        ],
        "latest_reactions": [
            {
                "message_id": "msg-nested",
                "type": "like",
                "score": 1,
                "user_id": "user-2",
                "user": {"id": "user-2", "role": "user", "banned": false, "online": true},
                "created_at": "2020-01-01T01:00:00.000Z",
                "updated_at": "2020-01-01T01:00:00.000Z"
            }
        ],
        "own_reactions": [
            {
                "message_id": "msg-nested",
                "type": "love",
                "score": 1,
                "user_id": "user-1",
                "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
                "created_at": "2020-01-01T00:30:00.000Z",
                "updated_at": "2020-01-01T00:30:00.000Z"
            }
        ],
        "mentioned_users": [
            {"id": "user-2", "role": "user", "banned": false, "online": true}
        ],
        "thread_participants": [
            {"id": "user-3", "role": "user", "banned": false, "online": true}
        ],
        "reply_count": 2,
        "deleted_reply_count": 0,
        "reaction_counts": {"like": 1, "love": 1},
        "reaction_scores": {"like": 1, "love": 1},
        "reaction_groups": {},
        "poll": {
            "id": "poll-1",
            "name": "Favorite color",
            "description": "Choose your favorite color",
            "options": [
                {"id": "option-1", "text": "Red"},
                {"id": "option-2", "text": "Blue"}
            ],
            "voting_visibility": "public",
            "enforce_unique_vote": true,
            "max_votes_allowed": 1,
            "allow_user_suggested_options": false,
            "allow_answers": true,
            "vote_count": 2,
            "vote_counts_by_option": {"option-1": 1, "option-2": 1},
            "latest_votes_by_option": {
                "option-1": [
                    {
                        "id": "vote-1",
                        "poll_id": "poll-1",
                        "option_id": "option-1",
                        "created_at": "2020-01-01T02:00:00.000Z",
                        "updated_at": "2020-01-01T02:00:00.000Z",
                        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
                        "is_answer": false
                    }
                ],
                "option-2": [
                    {
                        "id": "vote-2",
                        "poll_id": "poll-1",
                        "option_id": "option-2",
                        "created_at": "2020-01-01T02:00:00.000Z",
                        "updated_at": "2020-01-01T02:00:00.000Z",
                        "user": {"id": "user-2", "role": "user", "banned": false, "online": true},
                        "is_answer": false
                    }
                ]
            },
            "own_votes": [
                {
                    "id": "vote-1",
                    "poll_id": "poll-1",
                    "option_id": "option-1",
                    "created_at": "2020-01-01T02:00:00.000Z",
                    "updated_at": "2020-01-01T02:00:00.000Z",
                    "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
                    "is_answer": false
                }
            ],
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T03:00:00.000Z",
            "is_closed": false,
            "answers_count": 1,
            "latest_answers": [
                {
                    "id": "answer-1",
                    "poll_id": "poll-1",
                    "option_id": "",
                    "answer_text": "Purple",
                    "created_at": "2020-01-01T02:30:00.000Z",
                    "updated_at": "2020-01-01T02:30:00.000Z",
                    "user": {"id": "user-3", "role": "user", "banned": false, "online": true},
                    "is_answer": true
                }
            ],
            "created_by": {"id": "user-1", "role": "user", "banned": false, "online": true},
            "created_by_id": "user-1"
        },
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T03:00:00.000Z",
        "silent": false,
        "i18n": {},
        "pinned": false,
        "shadowed": false,
        "show_in_channel": false,
        "deleted_for_me": false
    }"""

    @Language("JSON")
    val jsonOptionalFieldsMissing = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingCid = """{
        "id": "msg-1",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingCreatedAt = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingHtml = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingId = """{
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingReplyCount = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingDeletedReplyCount = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingSilent = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z"
    }"""

    @Language("JSON")
    val jsonMissingText = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingType = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingUpdatedAt = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingUser = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingAttachments = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingLatestReactions = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingMentionedUsers = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    @Language("JSON")
    val jsonMissingOwnReactions = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true},
        "attachments": [],
        "latest_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    val expectedAllFields = Message(
        id = "msg-nested",
        cid = "messaging:general",
        text = "Message with nested objects @user-2",
        html = "<p>Message with nested objects @user-2</p>",
        type = "regular",
        user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true),
        attachments = listOf(
            Attachment(
                type = "image",
                assetUrl = "https://example.com/image.png",
                imageUrl = "https://example.com/image.png",
                thumbUrl = "https://example.com/thumb.png",
                name = "photo.png",
                fileSize = 2048,
                mimeType = "image/png",
            ),
        ),
        latestReactions = listOf(
            Reaction(
                messageId = "msg-nested",
                type = "like",
                score = 1,
                userId = "user-2",
                user = User(id = "user-2", role = "user", invisible = false, banned = false, online = true),
                createdAt = Date(1577840400000L), // 2020-01-01T01:00:00.000Z
                updatedAt = Date(1577840400000L),
            ),
        ),
        ownReactions = listOf(
            Reaction(
                messageId = "msg-nested",
                type = "love",
                score = 1,
                userId = "user-1",
                user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true),
                createdAt = Date(1577838600000L), // 2020-01-01T00:30:00.000Z
                updatedAt = Date(1577838600000L),
            ),
        ),
        mentionedUsersIds = emptyList(),
        mentionedUsers = listOf(
            User(id = "user-2", role = "user", invisible = false, banned = false, online = true),
        ),
        threadParticipants = listOf(
            User(id = "user-3", role = "user", invisible = false, banned = false, online = true),
        ),
        replyCount = 2,
        deletedReplyCount = 0,
        reactionCounts = mutableMapOf("like" to 1, "love" to 1),
        reactionScores = mutableMapOf("like" to 1, "love" to 1),
        reactionGroups = emptyMap(),
        poll = Poll(
            id = "poll-1",
            name = "Favorite color",
            description = "Choose your favorite color",
            options = listOf(
                Option(id = "option-1", text = "Red"),
                Option(id = "option-2", text = "Blue"),
            ),
            votingVisibility = VotingVisibility.PUBLIC,
            enforceUniqueVote = true,
            maxVotesAllowed = 1,
            allowUserSuggestedOptions = false,
            allowAnswers = true,
            voteCount = 2,
            voteCountsByOption = mapOf("option-1" to 1, "option-2" to 1),
            votes = listOf(
                Vote(
                    id = "vote-1",
                    pollId = "poll-1",
                    optionId = "option-1",
                    createdAt = Date(1577844000000L), // 2020-01-01T02:00:00.000Z
                    updatedAt = Date(1577844000000L),
                    user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true),
                ),
                Vote(
                    id = "vote-2",
                    pollId = "poll-1",
                    optionId = "option-2",
                    createdAt = Date(1577844000000L), // 2020-01-01T02:00:00.000Z
                    updatedAt = Date(1577844000000L),
                    user = User(id = "user-2", role = "user", invisible = false, banned = false, online = true),
                ),
            ),
            ownVotes = listOf(
                Vote(
                    id = "vote-1",
                    pollId = "poll-1",
                    optionId = "option-1",
                    createdAt = Date(1577844000000L), // 2020-01-01T02:00:00.000Z
                    updatedAt = Date(1577844000000L),
                    user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true),
                ),
            ),
            createdAt = Date(1577836800000L), // 2020-01-01T00:00:00.000Z
            updatedAt = Date(1577847600000L), // 2020-01-01T03:00:00.000Z
            closed = false,
            answersCount = 1,
            answers = listOf(
                Answer(
                    id = "answer-1",
                    pollId = "poll-1",
                    text = "Purple",
                    createdAt = Date(1577845800000L), // 2020-01-01T02:30:00.000Z
                    updatedAt = Date(1577845800000L),
                    user = User(id = "user-3", role = "user", invisible = false, banned = false, online = true),
                ),
            ),
            createdBy = User(id = "user-1", role = "user", invisible = false, banned = false, online = true),
            extraData = emptyMap(),
        ),
        createdAt = Date(1577836800000L), // 2020-01-01T00:00:00.000Z
        updatedAt = Date(1577847600000L), // Message updatedAt matches poll updatedAt due to parser behavior
        silent = false,
        i18n = emptyMap(),
        pinned = false,
        shadowed = false,
        showInChannel = false,
        deletedForMe = false,
        extraData = emptyMap(),
    )

    val expectedOptionalFieldsMissing = Message(
        id = "msg-1",
        cid = "messaging:general",
        text = "Hello world",
        html = "<p>Hello world</p>",
        type = "regular",
        user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true),
        attachments = emptyList(),
        latestReactions = emptyList(),
        ownReactions = emptyList(),
        mentionedUsersIds = emptyList(),
        mentionedUsers = emptyList(),
        replyCount = 0,
        deletedReplyCount = 0,
        createdAt = Date(1577836800000L),
        updatedAt = Date(1577836800000L),
        silent = false,
        pinned = false,
        shadowed = false,
        showInChannel = false,
        deletedForMe = false,
        extraData = emptyMap(),
    )
}
