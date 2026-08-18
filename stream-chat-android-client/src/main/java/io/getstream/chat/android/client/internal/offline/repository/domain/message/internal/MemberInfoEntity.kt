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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import com.squareup.moshi.JsonClass

/**
 * DB entity holding limited data about the channel membership of the user who sent a message.
 *
 * @property channelRole The channel-level role of the member.
 * @property notificationsMuted If notifications are muted for the member in the channel.
 * @property extraData The custom data of the member.
 */
@JsonClass(generateAdapter = true)
internal data class MemberInfoEntity(
    val channelRole: String? = null,
    val notificationsMuted: Boolean = false,
    val extraData: Map<String, Any> = emptyMap(),
)
