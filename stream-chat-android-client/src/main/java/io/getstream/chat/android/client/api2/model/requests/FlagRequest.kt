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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

internal sealed class FlagRequest {
    abstract val reason: String?
    abstract val custom: Map<String, String>
}

@JsonClass(generateAdapter = true)
internal data class FlagMessageRequest(
    @Json(name = "target_message_id") val targetMessageId: String,
    @Json(name = "reason") override val reason: String?,
    @Json(name = "custom") override val custom: Map<String, String>,
) : FlagRequest()

@JsonClass(generateAdapter = true)
internal data class FlagUserRequest(
    @Json(name = "target_user_id") val targetUserId: String,
    @Json(name = "reason") override val reason: String?,
    @Json(name = "custom") override val custom: Map<String, String>,
) : FlagRequest()
