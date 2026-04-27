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

package io.getstream.chat.android.client.api.event

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import kotlinx.coroutines.flow.StateFlow

/**
 * A [ChatEventHandler] factory.
 * Allows to create chat event handler instance with visible channels map.
 *
 * @param clientState The client used to obtain current user.
 */
public open class ChatEventHandlerFactory(
    private val clientState: ClientState = ChatClient.instance().clientState,
) {

    /**
     * Creates a [ChatEventHandler] instance.
     *
     * @param channels The visible channels map.
     */
    public open fun chatEventHandler(channels: StateFlow<Map<String, Channel>?>): ChatEventHandler {
        return DefaultChatEventHandler(channels = channels, clientState = clientState)
    }
}
