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
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.utils.Result

internal class DeleteReactionListenerComposite(
    private val deleteReactionListenerList: List<DeleteReactionListener>
) : DeleteReactionListener {

    override suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    ) {
        deleteReactionListenerList.forEach { listener ->
            listener.onDeleteReactionRequest(cid, messageId, reactionType, currentUser)
        }
    }

    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) {
        deleteReactionListenerList.forEach { listener ->
            listener.onDeleteReactionResult(cid, messageId, reactionType, currentUser, result)
        }
    }

    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> {
        return deleteReactionListenerList.map { listener ->
            listener.onDeleteReactionPrecondition(currentUser)
        }.fold(Result.success(Unit)) { acc, result ->
            if (acc.isError) acc else result
        }
    }
}
