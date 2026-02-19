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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Message
import io.getstream.result.Result

/**
 * Listener for requests of message deletion and for message deletion results.
 */
@InternalStreamChatApi
public interface DeleteMessageListener {

    /**
     * Runs precondition check for [ChatClient.deleteMessage].
     * The request will be run if the method returns [Result.Success] and won't be made if it returns [Result.Failure].
     *
     * @param messageId The message id to be deleted.
     *
     * @return [Result.Success] if the precondition is fulfilled, [Result.Failure] otherwise.
     */
    public suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit>

    /**
     * Method called when a request to delete a message in the API happens
     *
     * @param messageId
     */
    public suspend fun onMessageDeleteRequest(messageId: String)

    /**
     * Method called when a request for message deletion return. Use it to update database, update messages or to
     * present an error to the user.
     *
     * @param result the result of the API call.
     */
    public suspend fun onMessageDeleteResult(originalMessageId: String, result: Result<Message>)
}
