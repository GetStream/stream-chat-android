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
 * An object representing a device which can receive push notifications.
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class DeviceResponse(
    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "id")
    internal val id: String,

    @Json(name = "push_provider")
    internal val pushProvider: io.getstream.chat.android.network.models.PushProviderName,

    @Json(name = "user_id")
    internal val userId: String,

    @Json(name = "disabled")
    internal val disabled: Boolean? = null,

    @Json(name = "disabled_reason")
    internal val disabledReason: String? = null,

    @Json(name = "hardware_id")
    internal val hardwareId: String? = null,

    @Json(name = "push_provider_name")
    internal val pushProviderName: String? = null,

    @Json(name = "voip")
    internal val voip: Boolean? = null,
)
