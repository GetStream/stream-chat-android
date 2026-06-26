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

import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import org.intellij.lang.annotations.Language
import java.util.Date

@Suppress("LargeClass")
internal object MessageTestData {

    /**
     * Truly all-fields fixture: every optional Message field is populated, plus
     * a poll whose updated_at is later than the message's updated_at (exercises
     * the lastUpdateTime resolution), plus literal "extraData" and arbitrary
     * top-level custom keys with mixed value types.
     */
    @Language("JSON")
    val jsonAllFields = """{
        "id": "msg-all",
        "cid": "messaging:general",
        "text": "Message with nested objects @user-2",
        "html": "<p>Message with nested objects @user-2</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "command": "giphy",
        "parent_id": "parent-1",
        "quoted_message_id": "quoted-1",
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
                "message_id": "msg-all",
                "type": "like",
                "score": 1,
                "user_id": "user-2",
                "user": {"id": "user-2", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "created_at": "2020-01-01T01:00:00.000Z",
                "updated_at": "2020-01-01T01:00:00.000Z"
            }
        ],
        "own_reactions": [
            {
                "message_id": "msg-all",
                "type": "love",
                "score": 1,
                "user_id": "user-1",
                "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "created_at": "2020-01-01T00:30:00.000Z",
                "updated_at": "2020-01-01T00:30:00.000Z"
            }
        ],
        "mentioned_users": [
            {"id": "user-2", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"}
        ],
        "thread_participants": [
            {"id": "user-3", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"}
        ],
        "reply_count": 2,
        "deleted_reply_count": 1,
        "reaction_counts": {"like": 1, "love": 1},
        "reaction_scores": {"like": 1, "love": 1},
        "reaction_groups": {
            "like": {
                "count": 1,
                "sum_scores": 1,
                "first_reaction_at": "2020-01-01T01:00:00.000Z",
                "last_reaction_at": "2020-01-01T01:00:00.000Z"
            }
        },
        "i18n": {"en": "Hello", "es": "Hola"},
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
                        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
                        "user": {"id": "user-2", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
                    "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                    "is_answer": false
                }
            ],
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T04:00:00.000Z",
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
                    "user": {"id": "user-3", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                    "is_answer": true
                }
            ],
            "created_by": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
            "created_by_id": "user-1"
        },
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T03:00:00.000Z",
        "deleted_at": "2020-01-01T05:00:00.000Z",
        "pin_expires": "2020-01-02T00:00:00.000Z",
        "pinned": true,
        "pinned_at": "2020-01-01T04:00:00.000Z",
        "pinned_by": {"id": "user-2", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "message_text_updated_at": "2020-01-01T03:30:00.000Z",
        "shadowed": true,
        "show_in_channel": true,
        "silent": true,
        "deleted_for_me": true,
        "channel": {
            "cid": "messaging:general",
            "id": "general",
            "type": "messaging",
            "member_count": 5,
            "name": "General"
        },
        "member": {"channel_role": "channel_member"},
        "moderation_details": {
            "original_text": "spam_text",
            "action": "MESSAGE_RESPONSE_ACTION_BOUNCE",
            "error_msg": "this_message_did_not_meet_our_content_guidelines"
        },
        "moderation": {
            "action": "bounce",
            "original_text": "spam_text",
            "text_harms": ["harm-1", "harm-2"],
            "image_harms": [],
            "blocklist_matched": "block-1",
            "semantic_filter_matched": "semantic-1",
            "platform_circumvented": true
        },
        "reminder": {
            "channel_cid": "messaging:general",
            "message_id": "msg-1",
            "user_id": "user-1",
            "remind_at": "2020-02-01T00:00:00.000Z",
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T00:00:00.000Z"
        },
        "shared_location": {
            "channel_cid": "messaging:general",
            "message_id": "msg-all",
            "user_id": "user-1",
            "latitude": 12.5,
            "longitude": 56.5,
            "created_by_device_id": "device-1",
            "end_at": "2020-01-01T06:00:00.000Z",
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T00:00:00.000Z"
        },
        "extraData": {
            "key1": "value1",
            "key2": true,
            "key3": {"key4": "val4"}
        },
        "customString": "customVal1",
        "customBool": true,
        "customNumber": 42,
        "customArray": ["a", "b", "c"],
        "customNestedObject": {
            "outerKey": "outer",
            "nested": {"deepKey": [1, 2, 3]}
        }
    }"""

    @Language("JSON")
    val jsonOptionalFieldsMissing = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
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
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    val expectedOptionalFieldsMissing = Message(
        id = "msg-1",
        cid = "messaging:general",
        text = "Hello world",
        html = "<p>Hello world</p>",
        type = "regular",
        user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
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

    @Language("JSON")
    val jsonWithExplicitNulls = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello world",
        "html": "<p>Hello world</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false,
        "pinned": false,
        "command": null,
        "parent_id": null,
        "quoted_message_id": null,
        "deleted_for_me": null
    }"""

    val expectedWithExplicitNulls = Message(
        id = "msg-1",
        cid = "messaging:general",
        text = "Hello world",
        html = "<p>Hello world</p>",
        type = "regular",
        user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
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
        command = null,
        parentId = null,
        replyMessageId = null,
        deletedForMe = false,
        shadowed = false,
        showInChannel = false,
        extraData = emptyMap(),
    )

    // region Quoted message with channel info (field-order independence test)

    /**
     * JSON where "quoted_message" appears BEFORE "channel" in the object.
     * Tests that the direct parser produces the same result regardless of field order.
     */
    @Language("JSON")
    val jsonWithQuotedMessageBeforeChannel = """{
        "id": "msg-parent",
        "cid": "messaging:general",
        "text": "Parent message",
        "html": "<p>Parent message</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "silent": false,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "quoted_message_id": "msg-quoted",
        "quoted_message": {
            "id": "msg-quoted",
            "cid": "messaging:general",
            "text": "Quoted message",
            "html": "<p>Quoted message</p>",
            "type": "regular",
            "user": {"id": "user-2", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
            "attachments": [],
            "latest_reactions": [],
            "own_reactions": [],
            "mentioned_users": [],
            "reply_count": 0,
            "deleted_reply_count": 0,
            "silent": false,
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T00:00:00.000Z"
        },
        "channel": {
            "cid": "messaging:general",
            "id": "general",
            "type": "messaging",
            "member_count": 5,
            "name": "General"
        }
    }"""

    /**
     * Same JSON content but "channel" appears BEFORE "quoted_message".
     */
    @Language("JSON")
    val jsonWithQuotedMessageAfterChannel = """{
        "id": "msg-parent",
        "cid": "messaging:general",
        "text": "Parent message",
        "html": "<p>Parent message</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "silent": false,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "channel": {
            "cid": "messaging:general",
            "id": "general",
            "type": "messaging",
            "member_count": 5,
            "name": "General"
        },
        "quoted_message_id": "msg-quoted",
        "quoted_message": {
            "id": "msg-quoted",
            "cid": "messaging:general",
            "text": "Quoted message",
            "html": "<p>Quoted message</p>",
            "type": "regular",
            "user": {"id": "user-2", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
            "attachments": [],
            "latest_reactions": [],
            "own_reactions": [],
            "mentioned_users": [],
            "reply_count": 0,
            "deleted_reply_count": 0,
            "silent": false,
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T00:00:00.000Z"
        }
    }"""

    val expectedChannelInfo = ChannelInfo(
        cid = "messaging:general",
        id = "general",
        type = "messaging",
        memberCount = 5,
        name = "General",
    )

    /**
     * Outer message has `channel`; the inner two messages (depth 1 and depth 2) do not.
     * Locks down the documented one-level depth limit of channelInfo propagation in the
     * direct path: depth-1 message gets the outer's channelInfo, depth-2 message stays null.
     */
    @Language("JSON")
    val jsonTwoDeepQuotedMessage = """{
        "id": "msg-outer",
        "cid": "messaging:general",
        "text": "Outer",
        "html": "<p>Outer</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "silent": false,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "channel": {
            "cid": "messaging:general",
            "id": "general",
            "type": "messaging",
            "member_count": 5,
            "name": "General"
        },
        "quoted_message": {
            "id": "msg-mid",
            "cid": "messaging:general",
            "text": "Mid",
            "html": "<p>Mid</p>",
            "type": "regular",
            "user": {"id": "user-2", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
            "attachments": [],
            "latest_reactions": [],
            "own_reactions": [],
            "mentioned_users": [],
            "reply_count": 0,
            "deleted_reply_count": 0,
            "silent": false,
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T00:00:00.000Z",
            "quoted_message": {
                "id": "msg-inner",
                "cid": "messaging:general",
                "text": "Inner",
                "html": "<p>Inner</p>",
                "type": "regular",
                "user": {"id": "user-3", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "attachments": [],
                "latest_reactions": [],
                "own_reactions": [],
                "mentioned_users": [],
                "reply_count": 0,
                "deleted_reply_count": 0,
                "silent": false,
                "created_at": "2020-01-01T00:00:00.000Z",
                "updated_at": "2020-01-01T00:00:00.000Z"
            }
        }
    }"""

    val expectedQuotedMessageWithChannel = Message(
        id = "msg-parent",
        cid = "messaging:general",
        text = "Parent message",
        html = "<p>Parent message</p>",
        type = "regular",
        user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
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
        channelInfo = expectedChannelInfo,
        replyMessageId = "msg-quoted",
        replyTo = Message(
            id = "msg-quoted",
            cid = "messaging:general",
            text = "Quoted message",
            html = "<p>Quoted message</p>",
            type = "regular",
            user = User(id = "user-2", role = "user", invisible = false, banned = false, online = true, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
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
            channelInfo = expectedChannelInfo,
        ),
        extraData = emptyMap(),
    )

    // endregion

    // region Reactions filtered by messageId

    /**
     * Parent message id is "msg-A". latest_reactions and own_reactions contain reactions
     * pointing to a mix of messages. Both paths should keep only those whose
     * message_id matches the parent's id.
     */
    @Language("JSON")
    val jsonReactionsWithMixedMessageId = """{
        "id": "msg-A",
        "cid": "messaging:general",
        "text": "Hello",
        "html": "<p>Hello</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [
            {
                "message_id": "msg-A",
                "type": "like",
                "score": 1,
                "user_id": "user-2",
                "user": {"id": "user-2", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "created_at": "2020-01-01T01:00:00.000Z",
                "updated_at": "2020-01-01T01:00:00.000Z"
            },
            {
                "message_id": "msg-B",
                "type": "love",
                "score": 1,
                "user_id": "user-3",
                "user": {"id": "user-3", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "created_at": "2020-01-01T01:01:00.000Z",
                "updated_at": "2020-01-01T01:01:00.000Z"
            },
            {
                "message_id": "msg-C",
                "type": "haha",
                "score": 1,
                "user_id": "user-4",
                "user": {"id": "user-4", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "created_at": "2020-01-01T01:02:00.000Z",
                "updated_at": "2020-01-01T01:02:00.000Z"
            }
        ],
        "own_reactions": [
            {
                "message_id": "msg-A",
                "type": "love",
                "score": 1,
                "user_id": "user-1",
                "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "created_at": "2020-01-01T00:30:00.000Z",
                "updated_at": "2020-01-01T00:30:00.000Z"
            },
            {
                "message_id": "msg-X",
                "type": "wow",
                "score": 1,
                "user_id": "user-1",
                "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "created_at": "2020-01-01T00:31:00.000Z",
                "updated_at": "2020-01-01T00:31:00.000Z"
            }
        ],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false
    }"""

    val expectedReactionsFiltered = Message(
        id = "msg-A",
        cid = "messaging:general",
        text = "Hello",
        html = "<p>Hello</p>",
        type = "regular",
        user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
        attachments = emptyList(),
        latestReactions = listOf(
            Reaction(
                messageId = "msg-A",
                type = "like",
                score = 1,
                userId = "user-2",
                user = User(id = "user-2", role = "user", invisible = false, banned = false, online = true, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
                createdAt = Date(1577840400000L),
                updatedAt = Date(1577840400000L),
            ),
        ),
        ownReactions = listOf(
            Reaction(
                messageId = "msg-A",
                type = "love",
                score = 1,
                userId = "user-1",
                user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
                createdAt = Date(1577838600000L),
                updatedAt = Date(1577838600000L),
            ),
        ),
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

    // endregion

    // region Explicit-null collections (defaults must apply identically on both paths)

    /**
     * Only the genuinely-nullable collection fields are set to explicit JSON null:
     * `reaction_counts`, `reaction_scores`, and `reaction_groups` (all `T?` in the DTO).
     * Both paths must coerce them to empty defaults.
     *
     * Non-null DTO fields like `i18n` and `thread_participants` reject explicit null in
     * the DTO path; the throw cases below cover them separately.
     */
    @Language("JSON")
    val jsonExplicitNullCollections = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello",
        "html": "<p>Hello</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false,
        "reaction_counts": null,
        "reaction_scores": null,
        "reaction_groups": null
    }"""

    val expectedExplicitNullCollections = Message(
        id = "msg-1",
        cid = "messaging:general",
        text = "Hello",
        html = "<p>Hello</p>",
        type = "regular",
        user = User(id = "user-1", role = "user", invisible = false, banned = false, online = true, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
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
        reactionCounts = mutableMapOf(),
        reactionScores = mutableMapOf(),
        reactionGroups = emptyMap(),
        extraData = emptyMap(),
    )

    /**
     * `i18n` is non-nullable in DownstreamMessageDto (defaults to `emptyMap()`). The DTO path
     * throws on explicit JSON null; MessageAdapter must do the same. Field-absent is a separate
     * case (covered elsewhere) where both paths fall back to the empty default.
     */
    @Language("JSON")
    val jsonExplicitNullI18n = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello",
        "html": "<p>Hello</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false,
        "i18n": null
    }"""

    /**
     * `thread_participants` is non-nullable in DownstreamMessageDto (defaults to `emptyList()`).
     * Same explicit-null-rejection as `i18n` above.
     */
    @Language("JSON")
    val jsonExplicitNullThreadParticipants = """{
        "id": "msg-1",
        "cid": "messaging:general",
        "text": "Hello",
        "html": "<p>Hello</p>",
        "type": "regular",
        "user": {"id": "user-1", "role": "user", "banned": false, "online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
        "attachments": [],
        "latest_reactions": [],
        "own_reactions": [],
        "mentioned_users": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
        "silent": false,
        "thread_participants": null
    }"""

    // endregion
}
