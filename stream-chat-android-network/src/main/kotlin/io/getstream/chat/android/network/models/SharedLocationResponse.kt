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
data class SharedLocationResponse(
    @Json(name = "channel_cid")
    val channelCid: kotlin.String,

    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "created_by_device_id")
    val createdByDeviceId: kotlin.String,

    @Json(name = "duration")
    val duration: kotlin.String,

    @Json(name = "latitude")
    val latitude: kotlin.Float,

    @Json(name = "longitude")
    val longitude: kotlin.Float,

    @Json(name = "message_id")
    val messageId: kotlin.String,

    @Json(name = "updated_at")
    val updatedAt: java.util.Date,

    @Json(name = "user_id")
    val userId: kotlin.String,

    @Json(name = "end_at")
    val endAt: java.util.Date? = null,

    @Json(name = "channel")
    val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "message")
    val message: io.getstream.chat.android.network.models.MessageResponse? = null,
)
