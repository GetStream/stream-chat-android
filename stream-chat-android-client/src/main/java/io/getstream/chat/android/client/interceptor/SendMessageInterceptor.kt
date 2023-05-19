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

package io.getstream.chat.android.client.interceptor

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Intercepts the outgoing requests and potentially modifies the Message being sent to the API.
 */
@InternalStreamChatApi
public interface SendMessageInterceptor : Interceptor {
    /**
     * Intercept the message before sending it to the API.
     *
     * @param channelType The type of the channel in which message is sent.
     * @param channelId The id of the the channel in which message is sent.
     * @param message Message to be sent.
     * @param isRetrying If this message is being retried instead of a new message.
     * @param onUpdate Notifies about intermediate changes in message.
     *
     * @return [Result] of [Message] after intercepting.
     */
    public suspend fun interceptMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean,
        onUpdate: (Message) -> Unit = {},
    ): Result<Message>
}
