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
 * User request object
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class UserRequest (
    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "image")
    val image: kotlin.String? = null,

    @Json(name = "invisible")
    val invisible: kotlin.Boolean? = null,

    @Json(name = "language")
    val language: kotlin.String? = null,

    @Json(name = "name")
    val name: kotlin.String? = null,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap()
)
