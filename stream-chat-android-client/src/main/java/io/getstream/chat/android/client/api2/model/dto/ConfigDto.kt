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
internal data class ConfigDto(
    val created_at: Date?,
    val updated_at: Date?,
    val name: String?,
    val typing_events: Boolean,
    val read_events: Boolean,
    val connect_events: Boolean,
    val search: Boolean,
    val reactions: Boolean,
    val replies: Boolean,
    val mutes: Boolean,
    val uploads: Boolean,
    val url_enrichment: Boolean,
    val custom_events: Boolean,
    val push_notifications: Boolean,
    val skip_last_msg_update_for_system_msgs: Boolean?,
    val polls: Boolean,
    val message_retention: String,
    val max_message_length: Int,
    val automod: String,
    val automod_behavior: String,
    val blocklist_behavior: String?,
    val commands: List<CommandDto>,
    val user_message_reminders: Boolean?,
)
