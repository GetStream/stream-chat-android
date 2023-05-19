/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.debugger

import io.getstream.chat.android.models.Message

/**
 * Debugs the [io.getstream.chat.android.client.ChatClient].
 */
public interface ChatClientDebugger {

    /**
     * Creates an instance of [SendMessageDebugger] that allows you to debug the sending process of a message.
     *
     * @param channelType The channel type. ie messaging.
     * @param channelId The channel id. ie 123.
     * @param message Message object
     * @param isRetrying True if this message is being retried.
     *
     * @return Your custom [SendMessageDebugger] implementation.
     */
    public fun debugSendMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean = false,
    ): SendMessageDebugger = StubSendMessageDebugger
}

/**
 * Mock [ChatClientDebugger] implementation.
 */
internal object StubChatClientDebugger : ChatClientDebugger
