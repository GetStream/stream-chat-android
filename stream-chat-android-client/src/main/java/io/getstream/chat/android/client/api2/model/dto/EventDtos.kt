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

package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.api2.model.dto.utils.internal.ExactDate
import io.getstream.chat.android.network.models.OwnUserResponse
import io.getstream.result.Error
import java.util.Date

internal sealed class ChatEventDto

@JsonClass(generateAdapter = true)
internal data class ConnectedEventDto(
    val type: String,
    val created_at: ExactDate,
    val me: OwnUserResponse,
    val connection_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ConnectionErrorEventDto(
    val type: String,
    val created_at: ExactDate,
    val connection_id: String,
    val error: ErrorDto,
) : ChatEventDto()

/**
 * Special upstream event class, as we have to send this event
 * after connecting.
 */
@JsonClass(generateAdapter = true)
internal data class UpstreamConnectedEventDto(
    val type: String,
    val created_at: Date,
    val me: UpstreamUserDto,
    val connection_id: String,
)

@JsonClass(generateAdapter = true)
internal data class ConnectingEventDto(
    val type: String,
    val created_at: ExactDate,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class DisconnectedEventDto(
    val type: String,
    val created_at: ExactDate,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ErrorEventDto(
    val type: String,
    val created_at: ExactDate,
    val error: Error,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UnknownEventDto(
    val type: String,
    val created_at: ExactDate,
    val user: DownstreamUserDto?,
    val rawData: Map<*, *>,
) : ChatEventDto()
