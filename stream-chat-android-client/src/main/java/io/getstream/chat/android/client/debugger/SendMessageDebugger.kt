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

package io.getstream.chat.android.client.debugger

import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/**
 * Debugs the sending message flow.
 */
public interface SendMessageDebugger {
    /**
     * Called when the sending of a message starts.
     *
     * @param message The message being sent.
     */
    public fun onStart(message: Message) {}

    /**
     * Called when the interception of a message starts.
     *
     * @param message The message being intercepted.
     */
    public fun onInterceptionStart(message: Message) {}

    /**
     * Called when an intercepted message is updated during interception.
     *
     * @param message The updated message.
     */
    public fun onInterceptionUpdate(message: Message) {}

    /**
     * Called when the interception of a message is stopped and the result is available.
     *
     * @param result The result of the intercepted message.
     * @param message The updated message.
     */
    public fun onInterceptionStop(result: Result<Message>, message: Message) {}

    /**
     * Called when the sending of a message starts after interception.
     *
     * @param message The message being sent.
     */
    public fun onSendStart(message: Message) {}

    /**
     * Called when the sending of a message is stopped and the result is available.
     *
     * @param result The result of the sent message.
     * @param message The sent message.
     */
    public fun onSendStop(result: Result<Message>, message: Message) {}

    /**
     * Called when the sending of a message is completely stopped and the final result is available.
     *
     * @param result The final result of the message.
     * @param message The sent message.
     */
    public fun onStop(result: Result<Message>, message: Message) {}
}

internal object StubSendMessageDebugger : SendMessageDebugger
