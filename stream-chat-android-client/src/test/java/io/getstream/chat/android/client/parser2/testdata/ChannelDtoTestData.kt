package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamChannelUserRead
import io.getstream.chat.android.client.api2.model.dto.UpstreamMemberDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ChannelDtoTestData {

    @Language("JSON")
    val configJson =
        """{
          "created_at": "2020-06-10T11:04:31.000Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "name" : "config1",
          "typing_events": true,
          "read_events": true,
          "connect_events": true,
          "search": false,
          "reactions": true,
          "replies": true,
          "mutes": true,
          "uploads": true,
          "url_enrichment": false,
          "custom_events": false,
          "push_notifications": true,
          "message_retention": "retention",
          "max_message_length": 500,
          "automod": "none",
          "automod_behavior": "none",
          "blocklist_behavior": "empty",
          "commands": [
           {
            "name": "giphy",
            "description": "gif",
            "args": "empty",
            "set": "none"
           }
          ]
        }
        """.withoutWhitespace()

    private val configDto: ConfigDto = ConfigDto(
        created_at = Date(1591787071000),
        updated_at = Date(1591787071588),
        name = "config1",
        typing_events = true,
        read_events = true,
        connect_events = true,
        search = false,
        reactions = true,
        replies = true,
        mutes = true,
        uploads = true,
        url_enrichment = false,
        custom_events = false,
        push_notifications = true,
        message_retention = "retention",
        max_message_length = 500,
        automod = "none",
        automod_behavior = "none",
        blocklist_behavior = "empty",
        commands = listOf(
            CommandDto(
                name = "giphy",
                description = "gif",
                args = "empty",
                set = "none",
            )
        ),
    )

    @Language("JSON")
    val downstreamJson =
        """{
          "cid": "channelType:channelId",
          "id": "channelId",
          "type": "channelType",
          "watcher_count": 1,
          "frozen": false,
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "created_at": "2020-06-10T11:04:31.0Z",
          "deleted_at": "2020-06-10T11:04:31.588Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "member_count": 2,
          "messages": [${MessageDtoTestData.downstreamJson}],
          "members": [
           {
            "user": ${UserDtoTestData.downstreamJson},
            "role": "owner",
            "created_at": "2020-06-10T11:04:31.0Z",
            "updated_at": "2020-06-10T11:04:31.588Z",
            "invited": true,
            "invite_accepted_at": "2020-06-10T11:04:31.588Z",
            "shadow_banned": false
           }
          ],
          "watchers": [${UserDtoTestData.downstreamJson}],
          "read": [
           {
            "user": ${UserDtoTestData.downstreamJson},
            "last_read": "2020-06-10T11:04:31.0Z",
            "unread_messages": 1
           }
          ],
          "config": $configJson,
          "created_by": ${UserDtoTestData.downstreamJson},
          "team": "team1",
          "cooldown": 1,
          "pinned_messages": [${MessageDtoTestData.downstreamJson}],
          "draft": true
        }
        """.withoutWhitespace()
    val downstreamChannel = DownstreamChannelDto(
        cid = "channelType:channelId",
        id = "channelId",
        type = "channelType",
        watcher_count = 1,
        frozen = false,
        last_message_at = Date(1591787071588),
        created_at = Date(1591787071000),
        deleted_at = Date(1591787071588),
        updated_at = Date(1591787071588),
        member_count = 2,
        messages = listOf(MessageDtoTestData.downstreamMessage),
        members = listOf(
            DownstreamMemberDto(
                user = UserDtoTestData.downstreamUser,
                role = "owner",
                created_at = Date(1591787071000),
                updated_at = Date(1591787071588),
                invited = true,
                invite_accepted_at = Date(1591787071588),
                invite_rejected_at = null,
                shadow_banned = false,
            ),
        ),
        watchers = listOf(UserDtoTestData.downstreamUser),
        read = listOf(
            DownstreamChannelUserRead(
                user = UserDtoTestData.downstreamUser,
                last_read = Date(1591787071000),
                unread_messages = 1,
            ),
        ),
        config = configDto,
        created_by = UserDtoTestData.downstreamUser,
        team = "team1",
        cooldown = 1,
        pinned_messages = listOf(MessageDtoTestData.downstreamMessage),
        extraData = mapOf("draft" to true),
    )

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
          "cid": "channelType:channelId",
          "id": "channelId",
          "type": "channelType",
          "frozen": false,
          "config": $configJson
        }
        """.withoutWhitespace()
    val downstreamChannelWithoutExtraData = DownstreamChannelDto(
        cid = "channelType:channelId",
        id = "channelId",
        type = "channelType",
        watcher_count = 0,
        frozen = false,
        last_message_at = null,
        created_at = null,
        deleted_at = null,
        updated_at = null,
        member_count = 0,
        messages = emptyList(),
        members = emptyList(),
        watchers = emptyList(),
        read = emptyList(),
        config = configDto,
        created_by = null,
        team = "",
        cooldown = 0,
        pinned_messages = emptyList(),
        extraData = emptyMap(),
    )

    @Language("JSON")
    val upstreamJson =
        """{
          "cid": "channelType:channelId",
          "id": "channelId",
          "type": "channelType",
          "watcherCount": 1,
          "frozen": false,
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "created_at": "2020-06-10T11:04:31.000Z",
          "deleted_at": "2020-06-10T11:04:31.588Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "member_count": 2,
          "messages": [${MessageDtoTestData.upstreamJson}],
          "members": [
           {
            "user": ${UserDtoTestData.upstreamJson},
            "role": "owner",
            "created_at": "2020-06-10T11:04:31.000Z",
            "updated_at": "2020-06-10T11:04:31.588Z",
            "invited": true,
            "invite_accepted_at": "2020-06-10T11:04:31.588Z",
            "shadow_banned": false
           }
          ],
          "watchers": [${UserDtoTestData.upstreamJson}],
          "read": [
           {
            "user": ${UserDtoTestData.upstreamJson},
            "last_read": "2020-06-10T11:04:31.000Z",
            "unread_messages": 1
           }
          ],
          "config": $configJson,
          "created_by": ${UserDtoTestData.upstreamJson},
          "team": "team1",
          "cooldown": 1,
          "pinned_messages": [${MessageDtoTestData.upstreamJson}],
          "draft": true
        }
        """.withoutWhitespace()
    val upstreamChannel = UpstreamChannelDto(
        cid = "channelType:channelId",
        id = "channelId",
        type = "channelType",
        watcherCount = 1,
        frozen = false,
        last_message_at = Date(1591787071588),
        created_at = Date(1591787071000),
        deleted_at = Date(1591787071588),
        updated_at = Date(1591787071588),
        member_count = 2,
        messages = listOf(MessageDtoTestData.upstreamMessage),
        members = listOf(
            UpstreamMemberDto(
                user = UserDtoTestData.upstreamUser,
                role = "owner",
                created_at = Date(1591787071000),
                updated_at = Date(1591787071588),
                invited = true,
                invite_accepted_at = Date(1591787071588),
                invite_rejected_at = null,
                shadow_banned = false,
            ),
        ),
        watchers = listOf(UserDtoTestData.upstreamUser),
        read = listOf(
            UpstreamChannelUserRead(
                user = UserDtoTestData.upstreamUser,
                last_read = Date(1591787071000),
                unread_messages = 1,
            ),
        ),
        config = configDto,
        created_by = UserDtoTestData.upstreamUser,
        team = "team1",
        cooldown = 1,
        pinned_messages = listOf(MessageDtoTestData.upstreamMessage),
        extraData = mapOf("draft" to true),
    )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
          "cid": "channelType:channelId",
          "id": "channelId",
          "type": "channelType",
          "watcherCount": 0,
          "frozen": false,
          "member_count": 0,
          "messages": [],
          "members": [],
          "watchers": [],
          "read": [],
          "config": $configJson,
          "created_by": ${UserDtoTestData.upstreamJson},
          "team": "",
          "cooldown": 0,
          "pinned_messages": []
        }
        """.withoutWhitespace()
    val upstreamChannelWithoutExtraData = UpstreamChannelDto(
        cid = "channelType:channelId",
        id = "channelId",
        type = "channelType",
        watcherCount = 0,
        frozen = false,
        last_message_at = null,
        created_at = null,
        deleted_at = null,
        updated_at = null,
        member_count = 0,
        messages = emptyList(),
        members = emptyList(),
        watchers = emptyList(),
        read = emptyList(),
        config = configDto,
        created_by = UserDtoTestData.upstreamUser,
        team = "",
        cooldown = 0,
        pinned_messages = emptyList(),
        extraData = emptyMap(),
    )
}
