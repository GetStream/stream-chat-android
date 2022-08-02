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
import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry

/**
 * [ShuffleGiphyListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles updating the DB and state.
 *
 * @param logic [LogicRegistry]
 */
internal class ShuffleGiphyListenerImpl(private val logic: LogicRegistry) : ShuffleGiphyListener {

    /**
     * Added a new message to the DB and the state if request was successful.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param result The API call result.
     */
    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) {
        if (result.isSuccess) {
            val processedMessage = result.data().apply {
                syncStatus = SyncStatus.COMPLETED
            }
            logic.channelFromMessage(processedMessage)?.updateAndSaveMessages(listOf(processedMessage))
            logic.threadFromMessage(processedMessage)?.updateAndSaveMessages(listOf(processedMessage))
        }
    }
}
