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

package io.getstream.chat.android.client.api2.mapping

import io.getstream.chat.android.client.api2.model.dto.DownstreamMuteDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMuteDto
import io.getstream.chat.android.client.models.Mute

internal fun Mute.toDto(): UpstreamMuteDto =
    UpstreamMuteDto(
        user = user.toDto(),
        target = target.toDto(),
        created_at = createdAt,
        updated_at = updatedAt,
        expires = expires,
    )

internal fun DownstreamMuteDto.toDomain(): Mute =
    Mute(
        user = user.toDomain(),
        target = target.toDomain(),
        createdAt = created_at,
        updatedAt = updated_at,
        expires = expires,
    )
