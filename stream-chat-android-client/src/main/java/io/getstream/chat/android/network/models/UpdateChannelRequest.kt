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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.Json

/**
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class UpdateChannelRequest(
    @Json(name = "accept_invite")
    internal val acceptInvite: Boolean? = null,

    @Json(name = "cooldown")
    internal val cooldown: Int? = null,

    @Json(name = "hide_history")
    internal val hideHistory: Boolean? = null,

    @Json(name = "hide_history_before")
    internal val hideHistoryBefore: java.util.Date? = null,

    @Json(name = "reject_invite")
    internal val rejectInvite: Boolean? = null,

    @Json(name = "skip_push")
    internal val skipPush: Boolean? = null,

    @Json(name = "add_filter_tags")
    internal val addFilterTags: List<String>? = emptyList(),

    @Json(name = "add_members")
    internal val addMembers: List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "add_moderators")
    internal val addModerators: List<String>? = emptyList(),

    @Json(name = "assign_roles")
    internal val assignRoles: List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "demote_moderators")
    internal val demoteModerators: List<String>? = emptyList(),

    @Json(name = "invites")
    internal val invites: List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "remove_filter_tags")
    internal val removeFilterTags: List<String>? = emptyList(),

    @Json(name = "remove_members")
    internal val removeMembers: List<String>? = emptyList(),

    @Json(name = "data")
    internal val data: io.getstream.chat.android.network.models.ChannelInputRequest? = null,

    @Json(name = "message")
    internal val message: io.getstream.chat.android.network.models.MessageRequest? = null,
)
