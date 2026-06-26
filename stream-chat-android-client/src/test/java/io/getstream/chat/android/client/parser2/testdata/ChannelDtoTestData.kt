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

import io.getstream.chat.android.client.api2.model.dto.DownstreamChannelDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamMemberDto
import io.getstream.chat.android.network.models.ChannelConfigWithInfo
import io.getstream.chat.android.network.models.ChannelOwnCapability
import io.getstream.chat.android.network.models.Command as CommandDto
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
          "delivery_events": true,
          "connect_events": true,
          "search": false,
          "reactions": true,
          "replies": true,
          "quotes": true,
          "mutes": true,
          "uploads": true,
          "url_enrichment": false,
          "custom_events": false,
          "push_notifications": true,
          "skip_last_msg_update_for_system_msgs": false,
          "polls": true,
          "reminders": true,
          "count_messages": true,
          "max_message_length": 500,
          "automod": "disabled",
          "automod_behavior": "flag",
          "blocklist_behavior": "flag",
          "commands": [
           {
            "name": "giphy",
            "description": "gif",
            "args": "empty",
            "set": "none"
           }
          ],
          "user_message_reminders": false,
          "shared_locations": true,
          "mark_messages_pending": false
        }
        """.withoutWhitespace()

    private val configDto: ChannelConfigWithInfo = ChannelConfigWithInfo(
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        name = "config1",
        typingEvents = true,
        readEvents = true,
        deliveryEvents = true,
        connectEvents = true,
        search = false,
        reactions = true,
        replies = true,
        quotes = true,
        mutes = true,
        uploads = true,
        urlEnrichment = false,
        customEvents = false,
        pushNotifications = true,
        skipLastMsgUpdateForSystemMsgs = false,
        polls = true,
        reminders = true,
        countMessages = true,
        maxMessageLength = 500,
        automod = ChannelConfigWithInfo.Automod.Disabled,
        automodBehavior = ChannelConfigWithInfo.AutomodBehavior.Flag,
        blocklistBehavior = ChannelConfigWithInfo.BlocklistBehavior.Flag,
        commands = listOf(
            CommandDto(
                name = "giphy",
                description = "gif",
                args = "empty",
                set = "none",
            ),
        ),
        userMessageReminders = false,
        sharedLocations = true,
        markMessagesPending = false,
    )

    @Language("JSON")
    val downstreamJson =
        """{
          "cid": "channelType:channelId",
          "id": "channelId",
          "type": "channelType",
          "disabled": false,
          "name": "channelName",
          "image": "channelImage",
          "filter_tags": ["tag1"],
          "frozen": false,
          "last_message_at": "2020-06-10T11:04:31.588Z",
          "created_at": "2020-06-10T11:04:31.0Z",
          "deleted_at": "2020-06-10T11:04:31.588Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "member_count": 2,
          "members": [
           {
            "user": ${UserDtoTestData.downstreamJson},
            "created_at": "2020-06-10T11:04:31.0Z",
            "updated_at": "2020-06-10T11:04:31.588Z",
            "invited": true,
            "invite_accepted_at": "2020-06-10T11:04:31.588Z",
            "shadow_banned": false,
            "banned": false,
            "channel_role": "member",
            "notifications_muted": false,
            "status": "member",
            "ban_expires" : "2021-03-08T15:42:31.355Z",
            "pinned_at": "2020-06-10T11:04:31.588Z",
            "archived_at": "2020-06-10T11:04:31.588Z"
           }
          ],
          "config": $configJson,
          "created_by": ${UserDtoTestData.downstreamJson},
          "team": "team1",
          "cooldown": 1,
          "draft": true,
          "own_capabilities": ["connect-events", "pin-message"]
        }
        """.withoutWhitespace()
    val downstreamChannel = DownstreamChannelDto(
        cid = "channelType:channelId",
        id = "channelId",
        type = "channelType",
        disabled = false,
        filterTags = listOf("tag1"),
        frozen = false,
        lastMessageAt = Date(1591787071588),
        createdAt = Date(1591787071000),
        deletedAt = Date(1591787071588),
        updatedAt = Date(1591787071588),
        memberCount = 2,
        members = listOf(
            DownstreamMemberDto(
                user = UserDtoTestData.downstreamUser,
                createdAt = Date(1591787071000),
                updatedAt = Date(1591787071588),
                invited = true,
                inviteAcceptedAt = Date(1591787071588),
                inviteRejectedAt = null,
                shadowBanned = false,
                banned = false,
                channelRole = "member",
                notificationsMuted = false,
                status = "member",
                banExpires = Date(1615218151355),
                pinnedAt = Date(1591787071588),
                archivedAt = Date(1591787071588),
                custom = emptyMap(),
            ),
        ),
        config = configDto,
        createdBy = UserDtoTestData.downstreamUser,
        team = "team1",
        cooldown = 1,
        ownCapabilities = listOf(
            ChannelOwnCapability.ConnectEvents,
            ChannelOwnCapability.PinMessage,
        ),
        custom = mapOf(
            "name" to "channelName",
            "image" to "channelImage",
            "draft" to true,
        ),
    )

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
          "cid": "channelType:channelId",
          "id": "channelId",
          "type": "channelType",
          "disabled": false,
          "name": "channelName",
          "image": "channelImage",
          "frozen": false,
          "created_at": "2020-06-10T11:04:31.0Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "config": $configJson
        }
        """.withoutWhitespace()
    val downstreamChannelWithoutExtraData = DownstreamChannelDto(
        cid = "channelType:channelId",
        id = "channelId",
        type = "channelType",
        disabled = false,
        frozen = false,
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        config = configDto,
        custom = mapOf(
            "name" to "channelName",
            "image" to "channelImage",
        ),
    )

    @Language("JSON")
    val downstreamJsonWithoutNameAndImage =
        """{
          "cid": "channelType:channelId",
          "id": "channelId",
          "type": "channelType",
          "disabled": false,
          "frozen": false,
          "created_at": "2020-06-10T11:04:31.0Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "config": $configJson
        }
        """.withoutWhitespace()
    val downstreamChannelWithoutNameAndImage = DownstreamChannelDto(
        cid = "channelType:channelId",
        id = "channelId",
        type = "channelType",
        disabled = false,
        frozen = false,
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        config = configDto,
        custom = emptyMap(),
    )
}
