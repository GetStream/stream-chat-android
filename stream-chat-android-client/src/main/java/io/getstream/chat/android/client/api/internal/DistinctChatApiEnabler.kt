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

package io.getstream.chat.android.client.api.internal

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel

/**
 * Enables/Disables [DistinctChatApi] based on [distinctCallsEnabled] return value.
 */
@Suppress("UNCHECKED_CAST")
internal class DistinctChatApiEnabler(
    private val distinctApi: DistinctChatApi,
    private val distinctCallsEnabled: () -> Boolean
) : ChatApi by distinctApi.delegate {

    private val originalApi = distinctApi.delegate

    override fun queryChannel(channelType: String, channelId: String, query: QueryChannelRequest): Call<Channel> {
        return when (distinctCallsEnabled()) {
            true -> distinctApi.queryChannel(channelType, channelId, query)
            else -> originalApi.queryChannel(channelType, channelId, query)
        }
    }
}
