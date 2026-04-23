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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

/**
 * Request body for the grouped query channels endpoint (`POST /channels/grouped`).
 *
 * @param limit The maximum number of channels to return per bucket. `null` uses the server default.
 * @param watch Whether to start watching the returned channels for real-time events.
 * @param presence Whether to receive presence events for the members of the returned channels.
 */
@JsonClass(generateAdapter = true)
internal data class QueryGroupedChannelsRequest(
    val limit: Int?,
    val watch: Boolean,
    val presence: Boolean,
)
