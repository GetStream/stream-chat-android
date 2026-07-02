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

package io.getstream.chat.android.client.api2.optimisation.hash

import io.getstream.chat.android.client.api2.toMessagePaginationParams
import io.getstream.chat.android.client.api2.toPaginationParams
import io.getstream.chat.android.network.models.ChannelGetOrCreateRequest
import io.getstream.chat.android.network.models.ChannelInput

/**
 * A unique identifier of [ChannelGetOrCreateRequest] per channel.
 */
internal data class ChannelQueryKey(
    val channelType: String,
    val channelId: String,
    val queryKey: ChannelGetOrCreateRequest,
) {

    companion object {
        fun from(
            channelType: String,
            channelId: String,
            query: io.getstream.chat.android.client.api.models.QueryChannelRequest,
        ): ChannelQueryKey {
            return ChannelQueryKey(
                channelType = channelType,
                channelId = channelId,
                queryKey = ChannelGetOrCreateRequest(
                    state = query.state,
                    watch = query.watch,
                    presence = query.presence,
                    messages = query.messages.toMessagePaginationParams(),
                    watchers = query.watchers.toPaginationParams(),
                    members = query.members.toPaginationParams(),
                    data = query.data.takeIf { it.isNotEmpty() }?.let { ChannelInput(custom = it) },
                ),
            )
        }
    }
}
