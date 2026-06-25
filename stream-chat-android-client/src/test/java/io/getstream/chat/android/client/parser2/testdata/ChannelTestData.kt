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

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ChannelTestData {

    private const val MINIMAL_CONFIG = """
        "typing_events":true,
        "read_events":true,
        "connect_events":true,
        "search":false,
        "reactions":true,
        "replies":true,
        "mutes":true,
        "uploads":true,
        "url_enrichment":true,
        "custom_events":false,
        "push_notifications":true,
        "polls":false,
        "message_retention":"30",
        "max_message_length":5000,
        "automod":"disabled",
        "automod_behavior":"flag",
        "commands":[],
        "mark_messages_pending":false
    """

    @Language("JSON")
    val jsonAllFields = """{
        "cid":"messaging:123",
        "id":"123",
        "type":"messaging",
        "name":"Test Channel",
        "image":"https://example.com/image.png",
        "watcher_count":5,
        "filter_tags":["tag1","tag2"],
        "frozen":false,
        "last_message_at":"2020-06-29T06:14:28.000Z",
        "created_at":"2020-06-29T06:14:28.000Z",
        "deleted_at":"2020-06-30T06:14:28.000Z",
        "updated_at":"2020-06-30T06:14:28.000Z",
        "member_count":10,
        "messages":[],
        "members":[],
        "watchers":[],
        "read":[],
        "config":{$MINIMAL_CONFIG},
        "created_by":{"id":"user1","role":"user","created_at":"2020-06-29T06:14:28.000Z","updated_at":"2020-06-29T06:14:28.000Z","banned":false,"online":true,"invisible":false,"language":"","created_at":"2020-01-01T00:00:00.000Z","updated_at":"2020-01-01T00:00:00.000Z"},
        "team":"team1",
        "cooldown":30,
        "pinned_messages":[],
        "own_capabilities":["send-message","delete-channel"],
        "membership":null,
        "active_live_locations":[],
        "message_count":42,
        "custom_field":"custom_value"
    }"""

    @Language("JSON")
    val jsonWithNestedCollections = """{
        "cid":"messaging:123",
        "id":"123",
        "type":"messaging",
        "name":"Test Channel",
        "image":"https://example.com/image.png",
        "watcher_count":2,
        "filter_tags":["tag1"],
        "frozen":false,
        "last_message_at":"2020-06-29T06:14:28.000Z",
        "created_at":"2020-06-29T06:14:28.000Z",
        "deleted_at":null,
        "updated_at":"2020-06-29T06:14:28.000Z",
        "member_count":2,
        "messages":[
            {
                "id":"msg-1",
                "cid":"messaging:123",
                "text":"Hello",
                "html":"<p>Hello</p>",
                "type":"regular",
                "user":{"id":"user1","role":"user","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "attachments":[],
                "latest_reactions":[],
                "own_reactions":[],
                "mentioned_users":[],
                "reply_count":0,
                "deleted_reply_count":0,
                "created_at":"2020-06-29T06:00:00.000Z",
                "updated_at":"2020-06-29T06:00:00.000Z",
                "silent":false
            }
        ],
        "members":[
            {
                "user":{"id":"member1","role":"user","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "created_at":"2020-06-29T05:00:00.000Z"
            }
        ],
        "watchers":[
            {"id":"watcher1","role":"user","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"}
        ],
        "read":[
            {
                "user":{"id":"reader1","role":"user","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "last_read":"2020-06-29T06:10:00.000Z",
                "unread_messages":2
            }
        ],
        "config":{$MINIMAL_CONFIG},
        "created_by":{"id":"user1","role":"user","created_at":"2020-06-29T06:14:28.000Z","updated_at":"2020-06-29T06:14:28.000Z","banned":false,"online":true,"invisible":false,"language":"","created_at":"2020-01-01T00:00:00.000Z","updated_at":"2020-01-01T00:00:00.000Z"},
        "team":"team1",
        "cooldown":0,
        "pinned_messages":[
            {
                "id":"msg-pinned",
                "cid":"messaging:123",
                "text":"Pinned message",
                "html":"<p>Pinned message</p>",
                "type":"regular",
                "user":{"id":"user1","role":"user","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},
                "attachments":[],
                "latest_reactions":[],
                "own_reactions":[],
                "mentioned_users":[],
                "reply_count":0,
                "deleted_reply_count":0,
                "created_at":"2020-06-29T05:00:00.000Z",
                "updated_at":"2020-06-29T05:00:00.000Z",
                "silent":false,
                "pinned":true,
                "pinned_at":"2020-06-29T05:30:00.000Z"
            }
        ],
        "own_capabilities":["send-message"],
        "membership":null,
        "active_live_locations":[
            {
                "channel_cid":"messaging:123",
                "message_id":"msg-1",
                "user_id":"user1",
                "latitude":37.7749,
                "longitude":-122.4194,
                "created_by_device_id":"device-1",
                "end_at":"2020-06-29T07:00:00.000Z"
            }
        ],
        "message_count":10
    }"""

    @Language("JSON")
    val jsonOptionalFieldsMissing = """{
        "cid":"messaging:123",
        "id":"123",
        "type":"messaging",
        "frozen":false,
        "config":{$MINIMAL_CONFIG}
    }"""

    @Language("JSON")
    val jsonMissingCid = """{
        "id":"123",
        "type":"messaging",
        "frozen":false,
        "config":{$MINIMAL_CONFIG}
    }"""

    @Language("JSON")
    val jsonMissingId = """{
        "cid":"messaging:123",
        "type":"messaging",
        "frozen":false,
        "config":{$MINIMAL_CONFIG}
    }"""

    @Language("JSON")
    val jsonMissingType = """{
        "cid":"messaging:123",
        "id":"123",
        "frozen":false,
        "config":{$MINIMAL_CONFIG}
    }"""

    @Language("JSON")
    val jsonMissingFrozen = """{
        "cid":"messaging:123",
        "id":"123",
        "type":"messaging",
        "config":{$MINIMAL_CONFIG}
    }"""

    @Language("JSON")
    val jsonMissingConfig = """{
        "cid":"messaging:123",
        "id":"123",
        "type":"messaging",
        "frozen":false
    }"""

    private val minimalConfig = Config(
        createdAt = null,
        updatedAt = null,
        name = "",
        typingEventsEnabled = true,
        readEventsEnabled = true,
        deliveryEventsEnabled = true,
        connectEventsEnabled = true,
        searchEnabled = false,
        isReactionsEnabled = true,
        isThreadEnabled = true,
        muteEnabled = true,
        uploadsEnabled = true,
        urlEnrichmentEnabled = true,
        customEventsEnabled = false,
        pushNotificationsEnabled = true,
        skipLastMsgUpdateForSystemMsgs = false,
        pollsEnabled = false,
        messageRetention = "30",
        maxMessageLength = 5000,
        automod = "disabled",
        automodBehavior = "flag",
        blocklistBehavior = "",
        commands = emptyList(),
        messageRemindersEnabled = false,
        sharedLocationsEnabled = false,
        markMessagesPending = false,
    )

    val expectedAllFields = Channel(
        id = "123",
        type = "messaging",
        name = "Test Channel",
        image = "https://example.com/image.png",
        watcherCount = 5,
        filterTags = listOf("tag1", "tag2"),
        frozen = false,
        createdAt = Date(1593411268000),
        deletedAt = Date(1593497668000),
        updatedAt = Date(1593497668000),
        memberCount = 10,
        messages = emptyList(),
        members = emptyList(),
        watchers = emptyList(),
        read = emptyList(),
        config = minimalConfig,
        createdBy = User(
            id = "user1",
            role = "user",
            createdAt = Date(1593411268000),
            updatedAt = Date(1593411268000),
            banned = false,
            online = true,
            invisible = false,
        ),
        team = "team1",
        cooldown = 30,
        pinnedMessages = emptyList(),
        ownCapabilities = setOf("send-message", "delete-channel"),
        membership = null,
        activeLiveLocations = emptyList(),
        messageCount = 42,
        lastMessageAt = Date(1593411268000),
        extraData = mutableMapOf("custom_field" to "custom_value"),
    )

    val expectedWithNestedCollections = Channel(
        id = "123",
        type = "messaging",
        name = "Test Channel",
        image = "https://example.com/image.png",
        watcherCount = 2,
        filterTags = listOf("tag1"),
        frozen = false,
        createdAt = Date(1593411268000),
        deletedAt = null,
        updatedAt = Date(1593411268000),
        memberCount = 2,
        messages = listOf(
            Message(
                id = "msg-1",
                cid = "messaging:123",
                text = "Hello",
                html = "<p>Hello</p>",
                type = "regular",
                user = User(id = "user1", role = "user", banned = false, online = true, invisible = false, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
                createdAt = Date(1593410400000),
                updatedAt = Date(1593410400000),
                silent = false,
                reactionCounts = mutableMapOf(),
                reactionScores = mutableMapOf(),
                reactionGroups = emptyMap(),
                channelInfo = io.getstream.chat.android.models.ChannelInfo(
                    cid = "messaging:123",
                    id = "123",
                    type = "messaging",
                    memberCount = 2,
                    name = "Test Channel",
                    image = "https://example.com/image.png",
                ),
            ),
        ),
        members = listOf(
            Member(
                user = User(id = "member1", role = "user", banned = false, online = true, invisible = false),
                createdAt = Date(1593406800000), // 2020-06-29T05:00:00Z = 07:00 CEST
            ),
        ),
        watchers = listOf(
            User(id = "watcher1", role = "user", banned = false, online = true, invisible = false),
        ),
        read = listOf(
            ChannelUserRead(
                user = User(id = "reader1", role = "user", banned = false, online = true, invisible = false),
                lastReceivedEventDate = Date(1593411268000), // last_message_at
                unreadMessages = 2,
                lastRead = Date(1593411000000), // 2020-06-29T06:10:00Z = 08:10 CEST
                lastReadMessageId = null,
            ),
        ),
        config = minimalConfig,
        createdBy = User(
            id = "user1",
            role = "user",
            createdAt = Date(1593411268000),
            updatedAt = Date(1593411268000),
            banned = false,
            online = true,
            invisible = false,
        ),
        team = "team1",
        cooldown = 0,
        pinnedMessages = listOf(
            Message(
                id = "msg-pinned",
                cid = "messaging:123",
                text = "Pinned message",
                html = "<p>Pinned message</p>",
                type = "regular",
                user = User(id = "user1", role = "user", banned = false, online = true, invisible = false, createdAt = Date(1577836800000L), updatedAt = Date(1577836800000L)),
                createdAt = Date(1593406800000), // 2020-06-29T05:00:00Z = 07:00 CEST
                updatedAt = Date(1593406800000),
                silent = false,
                pinned = true,
                pinnedAt = Date(1593408600000), // 2020-06-29T05:30:00Z = 07:30 CEST
                reactionCounts = mutableMapOf(),
                reactionScores = mutableMapOf(),
                reactionGroups = emptyMap(),
                channelInfo = io.getstream.chat.android.models.ChannelInfo(
                    cid = "messaging:123",
                    id = "123",
                    type = "messaging",
                    memberCount = 2,
                    name = "Test Channel",
                    image = "https://example.com/image.png",
                ),
            ),
        ),
        ownCapabilities = setOf("send-message"),
        membership = null,
        activeLiveLocations = listOf(
            Location(
                cid = "messaging:123",
                messageId = "msg-1",
                userId = "user1",
                latitude = 37.7749,
                longitude = -122.4194,
                deviceId = "device-1",
                endAt = Date(1593414000000),
            ),
        ),
        messageCount = 10,
        lastMessageAt = Date(1593411268000),
        extraData = mutableMapOf(),
    )

    val expectedOptionalFieldsMissing = Channel(
        id = "123",
        type = "messaging",
        name = "",
        image = "",
        watcherCount = 0,
        filterTags = emptyList(),
        frozen = false,
        createdAt = null,
        deletedAt = null,
        updatedAt = null,
        memberCount = 0,
        messages = emptyList(),
        members = emptyList(),
        watchers = emptyList(),
        read = emptyList(),
        config = minimalConfig,
        createdBy = User(),
        team = "",
        cooldown = 0,
        pinnedMessages = emptyList(),
        ownCapabilities = emptySet(),
        membership = null,
        activeLiveLocations = emptyList(),
        messageCount = null,
        lastMessageAt = null,
        extraData = mutableMapOf(),
    )
}
