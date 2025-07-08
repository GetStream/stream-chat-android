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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * Used to update a live location message.
 *
 * @param message_id The ID of the message to update.
 * @param latitude The new latitude of the live location. Required to update the live location.
 * @param longitude The new longitude of the live location. Required to update the live location.
 * @param created_by_device_id The ID of the device that created the live location.
 * @param end_at The time when the live location should end. Required to stop the live location.
 */
@JsonClass(generateAdapter = true)
internal data class UpdateLiveLocationRequest(
    val message_id: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val created_by_device_id: String,
    val end_at: Date? = null,
)
