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
internal data class UpstreamMuteDto(
    val user: UpstreamUserDto?,
    val target: UpstreamUserDto?,
    val created_at: Date,
    val updated_at: Date,
    val expires: Date?,
)

@JsonClass(generateAdapter = true)
internal data class DownstreamMuteDto(
    val user: DownstreamUserDto?,
    val target: DownstreamUserDto?,
    val created_at: Date,
    val updated_at: Date,
    val expires: Date?,
)
