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

package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/**
 * Listener for editions in messages.
 */
public interface EditMessageListener {

    /**
     * Method called when a request for message edition happens. Use it to update database, update messages in the SDK,
     * update the UI when a message occurs...
     *
     * @param message [Message].
     */
    public suspend fun onMessageEditRequest(message: Message)

    /**
     * Method called when a request for message edition return. Use it to update database, update messages or to present
     * an error to the user.
     *
     * @param result the result of the API call.
     */
    public suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>)
}
