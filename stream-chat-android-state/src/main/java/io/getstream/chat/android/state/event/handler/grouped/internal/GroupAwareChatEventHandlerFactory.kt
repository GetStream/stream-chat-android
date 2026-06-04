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

package io.getstream.chat.android.state.event.handler.grouped.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.state.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import kotlinx.coroutines.flow.StateFlow

/**
 * Produces [GroupAwareChatEventHandler] instances for grouped channel lists.
 *
 * Internal: external consumers should not construct this directly. Compose code reaches it via
 * [groupAwareChatEventHandlerFactory], which is the only seam exposed across module boundaries.
 */
internal class GroupAwareChatEventHandlerFactory(
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

/**
 * Builds the group-aware [ChatEventHandlerFactory] used to drive grouped channel lists.
 *
 * Marked as [InternalStreamChatApi] because the underlying handler/factory/resolver classes are
 * deliberately hidden — the grouped-channels contract is still settling and we do not yet want to
 * commit to a public extension point. Consumers should instantiate a grouped
 * `ChannelListViewModel` instead of calling this directly.
 */
@InternalStreamChatApi
public fun groupAwareChatEventHandlerFactory(
    groupKey: String,
    clientState: ClientState,
): ChatEventHandlerFactory = GroupAwareChatEventHandlerFactory(
    groupKey = groupKey,
    clientState = clientState,
)
