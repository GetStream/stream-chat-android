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
 * Represents user reaction to a message
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ReactionRequest(
    @Json(name = "type")
    internal val type: String,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date? = null,

    @Json(name = "score")
    internal val score: Int? = null,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date? = null,

    @Json(name = "custom")
    internal val custom: Map<String, Any?>? = null,
)
