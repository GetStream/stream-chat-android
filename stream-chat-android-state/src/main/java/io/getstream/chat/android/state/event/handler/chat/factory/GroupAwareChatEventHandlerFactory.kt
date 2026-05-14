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

package io.getstream.chat.android.state.event.handler.chat.factory

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.state.event.handler.chat.ChannelGroupResolver
import io.getstream.chat.android.state.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.DefaultChannelGroupResolver
import io.getstream.chat.android.state.event.handler.chat.GroupAwareChatEventHandler
import kotlinx.coroutines.flow.StateFlow

/**
 * Produces [GroupAwareChatEventHandler] instances for grouped channel lists.
 *
 * Pair with `QueryChannelsIdentifier.Grouped(groupKey)` when initializing a grouped query's
 * state via `ChannelListViewModel(chatClient, groupKey = ...)` so that `channel.updated` and
 * channel-add events route channels into the correct group.
 *
 * @param groupKey The group identifier this factory is producing handlers for.
 * @param resolver Decides which group keys a channel belongs to. Defaults to
 * [DefaultChannelGroupResolver], which reads `channel.extraData["group"]` and always includes
 * an implicit `"all"` sentinel.
 * @param clientState Used by the inherited [io.getstream.chat.android.state.event.handler.chat.DefaultChatEventHandler]
 * to perform current-user membership checks.
 */
public open class GroupAwareChatEventHandlerFactory(
    private val groupKey: String,
    private val resolver: ChannelGroupResolver = DefaultChannelGroupResolver(),
    private val clientState: ClientState = ChatClient.instance().clientState,
) : ChatEventHandlerFactory(clientState) {

    override fun chatEventHandler(channels: StateFlow<Map<String, Channel>?>): ChatEventHandler =
        GroupAwareChatEventHandler(
            groupKey = groupKey,
            resolver = resolver,
            channels = channels,
            clientState = clientState,
        )
}
