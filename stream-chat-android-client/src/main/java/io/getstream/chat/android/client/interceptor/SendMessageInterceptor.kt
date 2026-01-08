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

package io.getstream.chat.android.client.interceptor

import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/**
 * Interceptor for sending messages in the Stream Chat SDK. Allows overriding the default 'sendMessage' API call.
 */
public interface SendMessageInterceptor {

    /**
     * Sends a message to the server.
     *
     * @param channelType The type of the channel to which the message is sent.
     * @param channelId The ID of the channel to which the message is sent.
     * @param message The message to be sent.
     *
     * @return The [Result] object containing an instance of [Message] in the case of a successful upload
     * or a failure if the upload had failed.
     *
     * @see [Result.Success]
     * @see [Result.Failure]
     */
    public fun sendMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Result<Message>
}
