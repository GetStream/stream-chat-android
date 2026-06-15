/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
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
data class BlockedUserResponse (
    @Json(name = "blocked_user_id")
    val blockedUserId: kotlin.String,

    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "user_id")
    val userId: kotlin.String,

    @Json(name = "blocked_user")
    val blockedUser: io.getstream.chat.android.network.models.UserResponse,

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse
)
