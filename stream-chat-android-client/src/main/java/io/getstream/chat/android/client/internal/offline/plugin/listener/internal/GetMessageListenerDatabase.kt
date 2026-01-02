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

package io.getstream.chat.android.client.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.listeners.GetMessageListener
import io.getstream.chat.android.models.Message
import io.getstream.log.StreamLog
import io.getstream.result.Error
import io.getstream.result.Result

/**
 * An implementation of [GetMessageListener] used to perform database operations when making an API call
 * that fetches a single message from the backend.
 *
 * @param repositoryFacade A class that holds a collection of repositories used by the SDK and exposes
 * various repository operations as methods.
 */
internal class GetMessageListenerDatabase(
    private val repositoryFacade: RepositoryFacade,
) : GetMessageListener {

    /**
     * Stream Logger using the class name as tag in order to make tracking operations easier.
     */
    private val logger = StreamLog.getLogger("Chat: GetMessageListenerDatabase")

    /**
     * Inserts the message into the database if the API call had been successful, otherwise logs the error.
     *
     * @param messageId The ID of the message we are fetching.
     * @param result The result of the API call. Will contain an instance of [Message] wrapped inside [Result] if
     * the request was successful, or an instance of [Error] if the request had failed.
     */
    override suspend fun onGetMessageResult(
        messageId: String,
        result: Result<Message>,
    ) {
        when (result) {
            is Result.Success -> {
                repositoryFacade.insertMessage(
                    message = result.value,
                )
            }
            is Result.Failure -> {
                val error = result.value
                logger.e {
                    "[onGetMessageResult] Could not insert the message into the database. The API call " +
                        "had failed with: ${error.message}"
                }
            }
        }
    }
}
