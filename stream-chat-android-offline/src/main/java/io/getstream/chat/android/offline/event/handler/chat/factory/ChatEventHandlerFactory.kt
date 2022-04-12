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

package io.getstream.chat.android.offline.event.handler.chat.factory

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.offline.event.handler.chat.DefaultChatEventHandler
import kotlinx.coroutines.flow.StateFlow

/**
* A [ChatEventHandler] factory. Allows passing visible channels` list.
*/
public open class ChatEventHandlerFactory {

    /**
     * Creates a [ChatEventHandler] instance.
     *
     * @param channels The visible channels` list.
     */
    public open fun chatEventHandler(channels: StateFlow<List<Channel>>): ChatEventHandler {
        return DefaultChatEventHandler(channels = channels)
    }
}
