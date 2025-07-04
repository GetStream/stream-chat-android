/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.models

import java.util.Date

/**
 * Represents a user's location.
 *
 * @property cid The channel ID where the location is shared.
 * @property messageId The ID of the message containing the location.
 * @property userId The ID of the user sharing the location.
 * @property endAt The date and time when the location sharing ends.
 * @property latitude The latitude of the user's location.
 * @property longitude The longitude of the user's location.
 * @property deviceId The device ID from which the location is shared.
 */
public data class Location(
    val cid: String = "",
    val messageId: String = "",
    val userId: String = "",
    val endAt: Date? = null,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val deviceId: String = "",
)
