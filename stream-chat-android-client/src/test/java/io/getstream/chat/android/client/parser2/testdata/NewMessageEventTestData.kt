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

import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import org.intellij.lang.annotations.Language
import java.util.Date

internal object NewMessageEventTestData {

    @Language("JSON")
    private const val MINIMAL_USER_JSON =
        """{"id":"user-1","role":"user","banned":false,"online":true}"""

    @Language("JSON")
    private const val MINIMAL_MESSAGE_JSON = """{
        "id":"msg-1",
        "cid":"messaging:general",
        "text":"Hello",
        "html":"<p>Hello</p>",
        "type":"regular",
        "user":$MINIMAL_USER_JSON,
        "attachments":[],
        "latest_reactions":[],
        "own_reactions":[],
        "mentioned_users":[],
        "reply_count":0,
        "deleted_reply_count":0,
        "created_at":"2020-01-01T00:00:00.000Z",
        "updated_at":"2020-01-01T00:00:00.000Z",
        "silent":false
    }"""

    @Language("JSON")
    val jsonAllFields = """{
        "type": "message.new",
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": ${UserTestData.jsonAllFields},
        "cid": "messaging:general",
        "channel_member_count": 10,
        "channel_custom": {"name": "General", "image": "https://example.com/channel.png"},
        "channel_type": "messaging",
        "channel_id": "general",
        "message": ${MessageTestData.jsonAllFields},
        "watcher_count": 5,
        "total_unread_count": 3,
        "unread_channels": 1,
        "channel_message_count": 42
    }"""

    @Language("JSON")
    val jsonOptionalFieldsMissing = """{
        "type": "message.new",
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": $MINIMAL_USER_JSON,
        "cid": "messaging:general",
        "channel_type": "messaging",
        "channel_id": "general",
        "message": $MINIMAL_MESSAGE_JSON
    }"""

    @Language("JSON")
    val jsonMissingType = """{
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": $MINIMAL_USER_JSON,
        "cid": "messaging:general",
        "channel_type": "messaging",
        "channel_id": "general",
        "message": $MINIMAL_MESSAGE_JSON
    }"""

    @Language("JSON")
    val jsonMissingCreatedAt = """{
        "type": "message.new",
        "user": $MINIMAL_USER_JSON,
        "cid": "messaging:general",
        "channel_type": "messaging",
        "channel_id": "general",
        "message": $MINIMAL_MESSAGE_JSON
    }"""

    @Language("JSON")
    val jsonMissingUser = """{
        "type": "message.new",
        "created_at": "2020-01-01T00:00:00.000Z",
        "cid": "messaging:general",
        "channel_type": "messaging",
        "channel_id": "general",
        "message": $MINIMAL_MESSAGE_JSON
    }"""

    @Language("JSON")
    val jsonMissingCid = """{
        "type": "message.new",
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": $MINIMAL_USER_JSON,
        "channel_type": "messaging",
        "channel_id": "general",
        "message": $MINIMAL_MESSAGE_JSON
    }"""

    @Language("JSON")
    val jsonMissingChannelType = """{
        "type": "message.new",
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": $MINIMAL_USER_JSON,
        "cid": "messaging:general",
        "channel_id": "general",
        "message": $MINIMAL_MESSAGE_JSON
    }"""

    @Language("JSON")
    val jsonMissingChannelId = """{
        "type": "message.new",
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": $MINIMAL_USER_JSON,
        "cid": "messaging:general",
        "channel_type": "messaging",
        "message": $MINIMAL_MESSAGE_JSON
    }"""

    @Language("JSON")
    val jsonMissingMessage = """{
        "type": "message.new",
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": $MINIMAL_USER_JSON,
        "cid": "messaging:general",
        "channel_type": "messaging",
        "channel_id": "general"
    }"""

    @Language("JSON")
    val jsonMalformedCreatedAt = """{
        "type": "message.new",
        "created_at": "not-a-date",
        "user": $MINIMAL_USER_JSON,
        "cid": "messaging:general",
        "channel_type": "messaging",
        "channel_id": "general",
        "message": $MINIMAL_MESSAGE_JSON
    }"""

    @Language("JSON")
    private const val QUOTED_MESSAGE_NO_CHANNEL_JSON = """{
        "id":"msg-q",
        "cid":"messaging:general",
        "text":"Quoted text",
        "html":"<p>Quoted</p>",
        "type":"regular",
        "user":$MINIMAL_USER_JSON,
        "attachments":[],
        "latest_reactions":[],
        "own_reactions":[],
        "mentioned_users":[],
        "reply_count":0,
        "deleted_reply_count":0,
        "created_at":"2020-01-01T00:00:00.000Z",
        "updated_at":"2020-01-01T00:00:00.000Z",
        "silent":false
    }"""

    @Language("JSON")
    private const val MESSAGE_WITH_QUOTED_NO_CHANNEL_JSON = """{
        "id":"msg-1",
        "cid":"messaging:general",
        "text":"Hello",
        "html":"<p>Hello</p>",
        "type":"regular",
        "user":$MINIMAL_USER_JSON,
        "attachments":[],
        "latest_reactions":[],
        "own_reactions":[],
        "mentioned_users":[],
        "reply_count":0,
        "deleted_reply_count":0,
        "created_at":"2020-01-01T00:00:00.000Z",
        "updated_at":"2020-01-01T00:00:00.000Z",
        "silent":false,
        "quoted_message":$QUOTED_MESSAGE_NO_CHANNEL_JSON
    }"""

    @Language("JSON")
    val jsonQuotedMessageNoChannel = """{
        "type": "message.new",
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": $MINIMAL_USER_JSON,
        "cid": "messaging:general",
        "channel_type": "messaging",
        "channel_id": "general",
        "message": $MESSAGE_WITH_QUOTED_NO_CHANNEL_JSON
    }"""

    private val minimalUser = User(
        id = "user-1",
        role = "user",
        invisible = false,
        banned = false,
        online = true,
    )

    private val optionalMissingChannelInfo = ChannelInfo(
        cid = "messaging:general",
        id = "general",
        type = "messaging",
        memberCount = 0,
        name = null,
        image = null,
    )

    private val minimalMessage = Message(
        id = "msg-1",
        cid = "messaging:general",
        text = "Hello",
        html = "<p>Hello</p>",
        type = "regular",
        user = minimalUser,
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

    val expectedOptionalFieldsMissing = NewMessageEvent(
        type = "message.new",
        createdAt = Date(1577836800000L),
        rawCreatedAt = "2020-01-01T00:00:00.000Z",
        user = minimalUser,
        cid = "messaging:general",
        channelType = "messaging",
        channelId = "general",
        message = minimalMessage.copy(channelInfo = optionalMissingChannelInfo),
        watcherCount = 0,
        totalUnreadCount = 0,
        unreadChannels = 0,
        channelMessageCount = null,
    )
}
