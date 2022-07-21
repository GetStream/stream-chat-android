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

package io.getstream.chat.android.offline.plugin.logic.channel.thread.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.state.channel.thread.internal.ThreadMutableState
import io.getstream.logging.StreamLog

/** Logic class for thread state management. Implements [ThreadQueryListener] as listener for LLC requests. */
internal class ThreadLogic(
    private val repos: RepositoryFacade,
    private val mutableState: ThreadMutableState,
    private val channelLogic: ChannelLogic
) : ThreadQueryListener {

    private val logger = StreamLog.getLogger("Chat:ThreadLogic")

    /** Runs precondition for loading more messages for thread. */
    private fun precondition(): Result<Unit> {
        return if (mutableState.loadingOlderMessages.value) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.i { errorMsg }
            Result(ChatError(errorMsg))
        } else {
            Result.success(Unit)
        }
    }

    /** Runs side effect when a request is going to be launched. */
    @Suppress("UnusedPrivateMember")
    private suspend fun onRequest(messageId: String, firstId: String?, limit: Int) {
        mutableState._loadingOlderMessages.value = true
        // TODO implement thread replies loading from DB
    }

    /** Runs side effect when a result is obtained. */
    private suspend fun onResult(result: Result<List<Message>>, limit: Int) {
        if (result.isSuccess) {
            // Note that we don't handle offline storage for threads at the moment.
            val newMessages = result.data()
            channelLogic.upsertMessages(newMessages)
            mutableState._endOfOlderMessages.value = newMessages.size < limit
            mutableState._oldestInThread.value =
                newMessages.sortedBy { it.createdAt }.firstOrNull() ?: mutableState._oldestInThread.value
        }

        mutableState._loadingOlderMessages.value = false

        result.onSuccessSuspend {
            repos.insertMessages(it)
        }
    }

    override suspend fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> = precondition()

    override suspend fun onGetRepliesRequest(messageId: String, limit: Int) = onRequest(messageId, null, limit)

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int) =
        onResult(result, limit)

    override suspend fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int) = precondition()

    override suspend fun onGetRepliesMoreRequest(
        messageId: String,
        firstId: String,
        limit: Int
    ) = onRequest(messageId, firstId, limit)

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int
    ) = onResult(result, limit)
}
