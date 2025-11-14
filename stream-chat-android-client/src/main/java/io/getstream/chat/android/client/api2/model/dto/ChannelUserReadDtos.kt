/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class UpstreamChannelUserRead(
    val user: UpstreamUserDto,
    val last_read: Date,
    val unread_messages: Int,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamChannelUserRead(
    val user: DownstreamUserDto,
    val last_read: Date,
    val unread_messages: Int,
    val last_read_message_id: String?,
    val last_delivered_at: Date? = null,
    val last_delivered_message_id: String? = null,
)
