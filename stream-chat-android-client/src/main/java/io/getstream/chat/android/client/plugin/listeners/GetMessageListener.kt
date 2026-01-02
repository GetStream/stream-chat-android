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
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * Listener used when fetching a single new message from the backend.
 */
public interface GetMessageListener {

    /**
     * Method called when the API call requesting a single new message has completed.
     *
     * Use it to update the database accordingly.
     *
     * @param messageId The ID of the message we are fetching.
     * @param result The result of the API call. Will contain an instance of [Message] wrapped inside [Result] if
     * the request was successful, or an instance of [Error] if the request had failed.
     */
    public suspend fun onGetMessageResult(
        messageId: String,
        result: Result<Message>,
    )
}
