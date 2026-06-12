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
    "UnusedImport"
)

package io.getstream.chat.android.network.models

import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.*
import kotlin.io.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 *
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class UpdateChannelRequest (
    @Json(name = "accept_invite")
    val acceptInvite: kotlin.Boolean? = null,

    @Json(name = "cooldown")
    val cooldown: kotlin.Int? = null,

    @Json(name = "hide_history")
    val hideHistory: kotlin.Boolean? = null,

    @Json(name = "hide_history_before")
    val hideHistoryBefore: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "reject_invite")
    val rejectInvite: kotlin.Boolean? = null,

    @Json(name = "skip_push")
    val skipPush: kotlin.Boolean? = null,

    @Json(name = "add_filter_tags")
    val addFilterTags: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "add_members")
    val addMembers: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "add_moderators")
    val addModerators: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "assign_roles")
    val assignRoles: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "demote_moderators")
    val demoteModerators: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "invites")
    val invites: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "remove_filter_tags")
    val removeFilterTags: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "remove_members")
    val removeMembers: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "data")
    val data: io.getstream.chat.android.network.models.ChannelInputRequest? = null,

    @Json(name = "message")
    val message: io.getstream.chat.android.network.models.MessageRequest? = null
)
