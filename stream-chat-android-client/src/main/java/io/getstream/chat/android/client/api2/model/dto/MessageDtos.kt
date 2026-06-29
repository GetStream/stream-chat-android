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
import io.getstream.chat.android.network.models.Attachment as AttachmentDto

internal typealias UpstreamMessageDto = io.getstream.chat.android.network.models.MessageRequest
internal typealias DownstreamMessageDto = io.getstream.chat.android.network.models.MessageResponse

@JsonClass(generateAdapter = true)
internal data class DownstreamDraftDto(
    val message: DownstreamDraftMessageDto,
    val channel_cid: String,
    val quoted_message: DownstreamMessageDto? = null,
    val parent_id: String? = null,
    val parent_message: DownstreamMessageDto? = null,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamDraftMessageDto(
    val id: String,
    val text: String,
    val command: String? = null,
    val args: String? = null,
    val attachments: List<AttachmentDto>? = null,
    val mentioned_users: List<DownstreamUserDto>? = null,
    val silent: Boolean = false,
    val show_in_channel: Boolean = false,

    val extraData: Map<String, Any>? = null,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamPendingMessageDto(
    val message: DownstreamMessageDto,
    val metadata: Map<String, String>?,
)
