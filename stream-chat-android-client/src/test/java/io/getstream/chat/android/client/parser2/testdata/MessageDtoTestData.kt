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

import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto
import io.getstream.chat.android.network.models.MentionedUserGroupResponse
import io.getstream.chat.android.network.models.MessageMemberResponse
import org.intellij.lang.annotations.Language
import java.util.Date
import io.getstream.chat.android.network.models.ReactionGroupResponse as ReactionGroupDto

internal object MessageDtoTestData {

    @Language("JSON")
    val downstreamJson =
        """{
          "attachments" : [${AttachmentDtoTestData.json}],
          "cid": "cid",
          "command": "command",
          "created_at": "2020-06-10T11:04:31.0Z",
          "deleted_at": "2020-06-10T11:04:31.588Z",
          "html": "html",
          "i18n": {},
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "latest_reactions": [${ReactionDtoTestData.downstreamJsonWithoutExtraData}],
          "mentioned_users": [${UserDtoTestData.downstreamJsonWithoutExtraData}],
          "mentioned_here": true,
          "mentioned_channel": true,
          "mentioned_groups": [
            {
              "id": "g1",
              "name": "platform",
              "created_at": "1970-01-01T00:00:00.000Z",
              "updated_at": "1970-01-01T00:00:00.000Z"
            }
          ],
          "mentioned_roles": ["admin", "moderator"],
          "own_reactions": [],
          "reaction_counts": {"like":  2},
          "reaction_scores": {"like":  10},
          "reaction_groups": {
            "like": {
              "count": 2,
              "sum_scores": 10,
              "first_reaction_at": "2020-06-10T11:04:31.588Z",
              "last_reaction_at": "2020-06-10T11:04:31.588Z"
            }
          },
          "reply_count": 0,
          "deleted_reply_count": 0,
          "parent_id": "parentId",
          "pinned": false,
          "pinned_by": ${UserDtoTestData.downstreamJsonWithoutExtraData},
          "quoted_message_id": "messageId",
          "shadowed": false,
          "show_in_channel": false,
          "silent": false,
          "text": "text",
          "thread_participants" : [],
          "type": "regular",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "user": ${UserDtoTestData.downstreamJson},
          "moderation_details": {
            "original_text": "spam_text",
            "action": "bounce",
            "error_msg": "this_message_did_not_meet_our_content_guidelines"
          },
          "member": {
            "channel_role": "channel_member",
            "notifications_muted": false
          },
          "deleted_for_me": true,
          "key1": "value1",
          "key2": true,
          "key3": {
            "key4": "val4"
          },
          "customKey1": "customVal1",
          "customKey2": true,
          "customKey3": [
            "a",
            "b",
            "c"
          ]
        }""".withoutWhitespace()
    val downstreamMessage = DownstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "text",
        html = "html",
        parentId = "parentId",
        command = "command",
        user = UserDtoTestData.downstreamUser,
        silent = false,
        shadowed = false,
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        deletedAt = Date(1591787071588),
        custom = mapOf(
            "moderation_details" to mapOf(
                "original_text" to "spam_text",
                "action" to "bounce",
                "error_msg" to "this_message_did_not_meet_our_content_guidelines",
            ),
            "key1" to "value1",
            "key2" to true,
            "key3" to mapOf(
                "key4" to "val4",
            ),
            "customKey1" to "customVal1",
            "customKey2" to true,
            "customKey3" to listOf("a", "b", "c"),
        ),
        type = "regular",
        replyCount = 0,
        deletedReplyCount = 0,
        reactionCounts = mapOf("like" to 2),
        reactionScores = mapOf("like" to 10),
        reactionGroups = mapOf(
            "like" to ReactionGroupDto(
                count = 2,
                sumScores = 10,
                firstReactionAt = Date(1591787071588),
                lastReactionAt = Date(1591787071588),
            ),
        ),
        latestReactions = listOf(ReactionDtoTestData.downstreamReactionWithoutExtraData),
        ownReactions = emptyList(),
        showInChannel = false,
        mentionedUsers = listOf(UserDtoTestData.downstreamUserWithoutExtraData),
        mentionedHere = true,
        mentionedChannel = true,
        mentionedGroups = listOf(
            MentionedUserGroupResponse(id = "g1", name = "platform", createdAt = Date(0), updatedAt = Date(0)),
        ),
        mentionedRoles = listOf("admin", "moderator"),
        i18n = emptyMap(),
        threadParticipants = emptyList(),
        attachments = listOf(AttachmentDtoTestData.attachment),
        quotedMessageId = "messageId",
        quotedMessage = null,
        pinned = false,
        pinnedBy = UserDtoTestData.downstreamUserWithoutExtraData,
        pinnedAt = null,
        pinExpires = null,
        messageTextUpdatedAt = null,
        member = MessageMemberResponse(channelRole = "channel_member", notificationsMuted = false),
        deletedForMe = true,
    )

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
          "attachments" : [],
          "cid": "cid",
          "created_at": "2020-06-10T11:04:31.0Z",
          "html": "",
          "i18n": {},
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "latest_reactions": [],
          "mentioned_users": [],
          "mentioned_here": false,
          "mentioned_channel": false,
          "own_reactions": [],
          "reaction_counts": {},
          "reaction_scores": {},
          "reaction_groups": {},
          "reply_count": 0,
          "deleted_reply_count": 0,
          "pinned": false,
          "shadowed": false,
          "show_in_channel": false,
          "silent": false,
          "text": "",
          "thread_participants" : [],
          "type": "",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "user": ${UserDtoTestData.downstreamJson},
          "deleted_for_me": false
        }""".withoutWhitespace()
    val downstreamMessageWithoutExtraData = DownstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        cid = "cid",
        text = "",
        html = "",
        parentId = null,
        command = null,
        user = UserDtoTestData.downstreamUser,
        silent = false,
        shadowed = false,
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        deletedAt = null,
        custom = emptyMap(),
        type = "",
        replyCount = 0,
        deletedReplyCount = 0,
        reactionCounts = emptyMap(),
        reactionScores = emptyMap(),
        reactionGroups = emptyMap(),
        latestReactions = emptyList(),
        ownReactions = emptyList(),
        showInChannel = false,
        mentionedUsers = emptyList(),
        mentionedHere = false,
        mentionedChannel = false,
        i18n = emptyMap(),
        threadParticipants = emptyList(),
        attachments = emptyList(),
        quotedMessageId = null,
        quotedMessage = null,
        pinned = false,
        pinnedBy = null,
        pinnedAt = null,
        pinExpires = null,
        messageTextUpdatedAt = null,
        deletedForMe = false,
    )

    @Language("JSON")
    val upstreamJson =
        """{
          "attachments": [${AttachmentDtoTestData.json}],
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "type": "regular",
          "mentioned_users": [],
          "mentioned_here": false,
          "mentioned_channel": false,
          "mentioned_group_ids": [],
          "mentioned_roles": [],
          "pinned": true,
          "show_in_channel": false,
          "silent": false,
          "text": "text",
          "key1": "value1",
          "key2": true,
          "key3": {
            "key4": "val4"
          },
          "customKey1": "customVal1",
          "customKey2": true,
          "customKey3": [
            "a",
            "b",
            "c"
          ],
          "restricted_visibility": ["jc"]
        }""".withoutWhitespace()
    val upstreamMessage = UpstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = io.getstream.chat.android.network.models.MessageRequest.Type.Regular,
        text = "text",
        silent = false,
        showInChannel = false,
        mentionedUsers = emptyList(),
        mentionedHere = false,
        mentionedChannel = false,
        mentionedGroupIds = emptyList(),
        mentionedRoles = emptyList(),
        attachments = listOf(AttachmentDtoTestData.attachment),
        quotedMessageId = null,
        pinned = true,
        pinnedAt = null,
        pinExpires = null,
        parentId = null,
        restrictedVisibility = listOf("jc"),
        sharedLocation = null,
        custom = mapOf(
            "key1" to "value1",
            "key2" to true,
            "key3" to mapOf(
                "key4" to "val4",
            ),
            "customKey1" to "customVal1",
            "customKey2" to true,
            "customKey3" to listOf("a", "b", "c"),
        ),
    )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
          "attachments": [],
          "id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "type": "regular",
          "mentioned_users": [],
          "mentioned_here": false,
          "mentioned_channel": false,
          "mentioned_group_ids": [],
          "mentioned_roles": [],
          "pinned": false,
          "show_in_channel": false,
          "silent": false,
          "text": "",
          "restricted_visibility": []
        }""".withoutWhitespace()
    val upstreamMessageWithoutExtraData = UpstreamMessageDto(
        id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = io.getstream.chat.android.network.models.MessageRequest.Type.Regular,
        text = "",
        silent = false,
        showInChannel = false,
        mentionedUsers = emptyList(),
        mentionedHere = false,
        mentionedChannel = false,
        mentionedGroupIds = emptyList(),
        mentionedRoles = emptyList(),
        attachments = emptyList(),
        quotedMessageId = null,
        pinned = false,
        pinnedAt = null,
        pinExpires = null,
        parentId = null,
        restrictedVisibility = emptyList(),
        sharedLocation = null,
        custom = emptyMap(),
    )
}
