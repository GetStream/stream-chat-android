/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.core.internal.StreamHandsOff

/**
 * Upstream Json model member data.
 * See [io.getstream.chat.android.client.parser2.adapters.UpstreamMemberDataDtoAdapter] for special [extraData]
 * handling.
 *
 * @param user_id The user id of the member.
 * @param extraData Custom extra data for the member.
 */
@StreamHandsOff(
    reason = "Field names can't be changed because [CustomObjectDtoAdapter] class uses reflections to add/remove " +
        "content of [extraData] map",
)
@JsonClass(generateAdapter = true)
internal data class UpstreamMemberDataDto(
    val user_id: String,

    val extraData: Map<String, Any>,
) : ExtraDataDto
