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

package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.models.Channel

/**
 * Internal result wrapper for [io.getstream.chat.android.client.api.ChatApi.queryChannels].
 * Holds both the list of channels and the optional parsed predefined filter returned by the backend.
 *
 * @param channels The list of channels returned by the query.
 * @param predefinedFilter The parsed predefined filter metadata, or null if a regular filter was used.
 */
internal data class QueryChannelsResult(
    val channels: List<Channel>,
    val predefinedFilter: PredefinedFilter?,
)
