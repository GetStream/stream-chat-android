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

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.utils.Result

/**
 * This class act as an composition of multiple SendReactionListener. This is only necessary
 * along StatePlugin lives inside OfflinePlugin. When both plugins are separated, this class can
 * and should be deleted.
 */
internal class SendReactionListenerComposite(
    private val sendReactionListenerList: List<SendReactionListener>,
) : SendReactionListener {

    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) {
        sendReactionListenerList.forEach { sendMessageListener ->
            sendMessageListener.onSendReactionRequest(cid, reaction, enforceUnique, currentUser)
        }
    }

    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        sendReactionListenerList.forEach { sendMessageListener ->
            sendMessageListener.onSendReactionResult(cid, reaction, enforceUnique, currentUser, result)
        }
    }

    override fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit> {
        return sendReactionListenerList.map { listener ->
            listener.onSendReactionPrecondition(currentUser, reaction)
        }.fold(Result.success(Unit)) { acc, result ->
            if (acc.isError) acc else result
        }
    }
}
