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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.ShuffleGiphyListener
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.result.Result

/**
 * [ShuffleGiphyListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles updating the state.
 *
 * @param logic [LogicRegistry]
 */
internal class ShuffleGiphyListenerState(private val logic: LogicRegistry) : ShuffleGiphyListener {

    /**
     * Added a new message to the state if request was successful.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param result The API call result.
     */
    override suspend fun onShuffleGiphyResult(cid: String, result: Result<Message>) {
        if (result is Result.Success) {
            val processedMessage = result.value.copy(syncStatus = SyncStatus.COMPLETED)
            logic.channelFromMessage(processedMessage)?.upsertMessage(processedMessage)
            logic.getActiveQueryThreadsLogic().forEach { it.upsertMessage(processedMessage) }
            logic.threadFromMessage(processedMessage)?.upsertMessage(processedMessage)
        }
    }
}
