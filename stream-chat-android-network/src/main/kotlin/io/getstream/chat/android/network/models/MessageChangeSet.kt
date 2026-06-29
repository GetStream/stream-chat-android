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
data class MessageChangeSet(
    @Json(name = "attachments")
    val attachments: kotlin.Boolean,

    @Json(name = "custom")
    val custom: kotlin.Boolean,

    @Json(name = "html")
    val html: kotlin.Boolean,

    @Json(name = "mentioned_user_ids")
    val mentionedUserIds: kotlin.Boolean,

    @Json(name = "mml")
    val mml: kotlin.Boolean,

    @Json(name = "pin")
    val pin: kotlin.Boolean,

    @Json(name = "quoted_message_id")
    val quotedMessageId: kotlin.Boolean,

    @Json(name = "silent")
    val silent: kotlin.Boolean,

    @Json(name = "text")
    val text: kotlin.Boolean,
)
