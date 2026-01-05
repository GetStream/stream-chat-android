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

package io.getstream.chat.android.client.plugin

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.plugin.factory.PluginFactory
import io.getstream.chat.android.client.receipts.MessageReceiptManager
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.result.Result
import io.getstream.result.onSuccessSuspend

/**
 * A plugin that marks messages as delivered when channels are queried.
 */
internal class MessageDeliveredPlugin(
    chatClient: ChatClient = ChatClient.instance(),
) : Plugin {
    private val messageReceiptManager: MessageReceiptManager by lazy { chatClient.messageReceiptManager }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        result.onSuccessSuspend { channels ->
            messageReceiptManager.markChannelsAsDelivered(channels)
        }
    }

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        result.onSuccessSuspend { channel ->
            if (request.pagination() == null) { // only mark as delivered on initial load
                messageReceiptManager.markChannelsAsDelivered(channels = listOf(channel))
            }
        }
    }
}

internal object MessageDeliveredPluginFactory : PluginFactory {
    override fun get(user: User): Plugin = MessageDeliveredPlugin()
}
