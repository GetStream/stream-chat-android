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
internal data class QueryMembersPayload(
    @Json(name = "type")
    internal val type: String,

    @Json(name = "filter_conditions")
    internal val filterConditions: Map<String, Any?> = emptyMap(),

    @Json(name = "id")
    internal val id: String? = null,

    @Json(name = "limit")
    internal val limit: Int? = null,

    @Json(name = "offset")
    internal val offset: Int? = null,

    @Json(name = "members")
    internal val members: List<io.getstream.chat.android.network.models.ChannelMemberRequest>? = emptyList(),

    @Json(name = "sort")
    internal val sort: List<io.getstream.chat.android.network.models.SortParamRequest>? = emptyList(),
)
