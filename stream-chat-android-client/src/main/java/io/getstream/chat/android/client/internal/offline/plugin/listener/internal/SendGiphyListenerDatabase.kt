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

package io.getstream.chat.android.client.internal.offline.plugin.listener.internal

import io.getstream.chat.android.client.extensions.internal.users
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.persistance.repository.UserRepository
import io.getstream.chat.android.client.plugin.listeners.SendGiphyListener
import io.getstream.chat.android.client.utils.message.isEphemeral
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.result.Result

/**
 * [SendGiphyListener] implementation for [io.getstream.chat.android.client.internal.offline.plugin.OfflinePlugin].
 * Replaces the stored ephemeral preview with the sent message, so the database doesn't wait for `message.new`.
 *
 * @param userRepository [UserRepository]
 * @param messageRepository [MessageRepository]
 */
internal class SendGiphyListenerDatabase(
    private val userRepository: UserRepository,
    private val messageRepository: MessageRepository,
) : SendGiphyListener {

    override suspend fun onGiphySendResult(cid: String, result: Result<Message>) {
        if (result !is Result.Success) return
        // Only the preview needs replacing; a server copy already stored (e.g. from message.new) may be newer
        val stored = messageRepository.selectMessage(result.value.id)
        if (stored != null && !stored.isEphemeral()) return
        val message = result.value.copy(syncStatus = SyncStatus.COMPLETED)
        userRepository.insertUsers(message.users())
        messageRepository.insertMessage(message)
    }
}
