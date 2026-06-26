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

internal typealias DownstreamChannelDto = io.getstream.chat.android.network.models.ChannelResponse

/**
 * Model holding custom channel fields delivered with `message.new` events.
 *
 * Note: It is currently relevant only for the [name] and [image] fields. If in the future we need to support more or
 * even custom fields, consider changing this DTO (to a Map<String, Any> for example).
 *
 * @param name The channel name (if available).
 * @param image The channel image (if available).
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamChannelCustomDto(
    val name: String?,
    val image: String?,
)
