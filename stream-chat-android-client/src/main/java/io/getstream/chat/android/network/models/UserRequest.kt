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
 * User request object
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class UserRequest(
    @Json(name = "id")
    internal val id: String,

    @Json(name = "image")
    internal val image: String? = null,

    @Json(name = "invisible")
    internal val invisible: Boolean? = null,

    @Json(name = "language")
    internal val language: String? = null,

    @Json(name = "name")
    internal val name: String? = null,

    @Json(name = "custom")
    internal val custom: Map<String, Any?>? = emptyMap(),

    @Json(name = "privacy_settings")
    internal val privacySettings: io.getstream.chat.android.network.models.PrivacySettingsResponse? = null,
)
