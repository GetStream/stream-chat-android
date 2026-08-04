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

import io.getstream.chat.android.network.models.ChannelInputRequest
import io.getstream.chat.android.network.models.ChannelMemberRequest
import io.getstream.chat.android.network.models.MessageRequest
import io.getstream.chat.android.network.models.UpdateChannelRequest

internal object UpdateChannelRequestTestData {

    private const val EMPTY_LISTS =
        """"add_filter_tags":[],"add_members":[],"add_moderators":[],"assign_roles":[],""" +
            """"demote_moderators":[],"invites":[],"remove_filter_tags":[],"remove_members":[]"""

    val updateChannelRequest = UpdateChannelRequest(
        data = ChannelInputRequest(custom = mapOf("probe_key" to "probe_value")),
    )
    const val updateChannelJson =
        """{$EMPTY_LISTS,"data":{"invites":[],"members":[],"probe_key":"probe_value"}}"""

    val acceptInviteRequest = UpdateChannelRequest(
        acceptInvite = true,
        message = MessageRequest(text = "accepting"),
    )
    const val acceptInviteJson =
        """{"accept_invite":true,$EMPTY_LISTS,"message":{"text":"accepting","attachments":[],""" +
            """"mentioned_group_ids":[],"mentioned_roles":[],"mentioned_users":[],"restricted_visibility":[]}}"""

    val removeMembersRequest = UpdateChannelRequest(
        removeMembers = listOf("han"),
        skipPush = true,
    )
    const val removeMembersJson =
        """{"skip_push":true,"add_filter_tags":[],"add_members":[],"add_moderators":[],"assign_roles":[],""" +
            """"demote_moderators":[],"invites":[],"remove_filter_tags":[],"remove_members":["han"]}"""

    val inviteMembersRequest = UpdateChannelRequest(
        invites = listOf(ChannelMemberRequest(userId = "han")),
        skipPush = true,
    )
    const val inviteMembersJson =
        """{"skip_push":true,"add_filter_tags":[],"add_members":[],"add_moderators":[],"assign_roles":[],""" +
            """"demote_moderators":[],"invites":[{"user_id":"han"}],"remove_filter_tags":[],"remove_members":[]}"""

    val rejectInviteRequest = UpdateChannelRequest(rejectInvite = true)
    const val rejectInviteJson =
        """{"reject_invite":true,"add_filter_tags":[],"add_members":[],"add_moderators":[],""" +
            """"assign_roles":[],"demote_moderators":[],"invites":[],"remove_filter_tags":[],""" +
            """"remove_members":[]}"""

    val addMembersRequest = UpdateChannelRequest(
        addMembers = listOf(
            ChannelMemberRequest(userId = "u1", channelRole = "moderator", custom = mapOf("k" to "v")),
        ),
        message = MessageRequest(
            text = "added",
            type = MessageRequest.Type.System,
            custom = mapOf("mk" to "mv"),
        ),
        skipPush = true,
    )
    const val addMembersJson =
        """{"skip_push":true,"add_filter_tags":[],"add_members":[{"user_id":"u1","channel_role":"moderator",""" +
            """"k":"v"}],"add_moderators":[],"assign_roles":[],"demote_moderators":[],"invites":[],""" +
            """"remove_filter_tags":[],"remove_members":[],"message":{"text":"added","type":"system",""" +
            """"attachments":[],"mentioned_group_ids":[],"mentioned_roles":[],"mentioned_users":[],""" +
            """"restricted_visibility":[],"mk":"mv"}}"""
}
