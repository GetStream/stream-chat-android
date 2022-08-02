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

package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.plugin.listeners.DeleteMessageListener
import io.getstream.chat.android.client.utils.Result

/**
 * This class act as an composition of multiple DeleteMessageListener. This is only necessary
 * along StatePlugin lives inside OfflinePlugin. When both plugins are separated, this class can
 * and should be deleted.
 */
internal class DeleteMessageListenerComposite(
    private val deleteMessageListenerList: List<DeleteMessageListener>,
) : DeleteMessageListener {

    override suspend fun onMessageDeletePrecondition(messageId: String): Result<Unit> {
        return deleteMessageListenerList.map { listener ->
            listener.onMessageDeletePrecondition(messageId)
        }.fold(Result.success(Unit)) { acc, result ->
            if (acc.isError) acc else result
        }
    }

    override suspend fun onMessageDeleteRequest(messageId: String) {
        deleteMessageListenerList.forEach { deleteMessageListener ->
            deleteMessageListener.onMessageDeleteRequest(messageId)
        }
    }

    override suspend fun onMessageDeleteResult(originalMessageId: String, result: Result<Message>) {
        deleteMessageListenerList.forEach { deleteMessageListener ->
            deleteMessageListener.onMessageDeleteResult(originalMessageId, result)
        }
    }
}
