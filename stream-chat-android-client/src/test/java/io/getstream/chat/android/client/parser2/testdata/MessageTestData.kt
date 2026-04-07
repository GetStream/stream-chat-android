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

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import org.intellij.lang.annotations.Language
import java.util.Date

internal object MessageTestData {

    @Language("JSON")
    val jsonAllFields = """{
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
        "thread_participants": [],
        "reply_count": 0,
        "deleted_reply_count": 0,
        "reaction_counts": {},
        "reaction_scores": {},
        "reaction_groups": {},
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-01T00:00:00.000Z",
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
        reactionCounts = emptyMap(),
        reactionScores = emptyMap(),
        reactionGroups = emptyMap(),
        createdAt = Date(1577836800000L),
        updatedAt = Date(1577836800000L),
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
