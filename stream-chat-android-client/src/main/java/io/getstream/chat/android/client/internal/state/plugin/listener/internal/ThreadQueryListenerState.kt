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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.thread.internal.ThreadLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.models.Message
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import kotlin.math.roundToInt

/**
 * ThreadQueryListenerState handles the thread state updates in the SDK.
 *
 * @param logic [LogicRegistry] to access the thread state to be updated.
 */
internal class ThreadQueryListenerState(
    private val logic: LogicRegistry,
) : ThreadQueryListener {

    private val logger by taggedLogger("Chat:ThreadQueryListener")

    override suspend fun onGetRepliesPrecondition(parentId: String): Result<Unit> {
        val loadingMoreMessage = logic.thread(parentId).isLoadingMessages()

        return if (loadingMoreMessage) {
            val errorMsg = "already loading messages for this thread, ignoring the load requests."
            logger.i { errorMsg }
            Result.Failure(Error.GenericError(errorMsg))
        } else {
            Result.Success(Unit)
        }
    }

    override suspend fun onGetRepliesRequest(parentId: String, limit: Int) {
        val threadLogic = logic.thread(parentId)

        threadLogic.setLoading(true)
    }

    override suspend fun onGetRepliesResult(result: Result<List<Message>>, parentId: String, limit: Int) {
        val threadLogic = logic.thread(parentId)
        threadLogic.setLoading(false)
        // The initial page contains the newest replies, so the newer end is already loaded.
        result.onSuccess { threadLogic.setEndOfNewerMessages(true) }
        onResult(threadLogic, result, limit)
    }

    override suspend fun onGetRepliesMoreRequest(parentId: String, firstId: String, limit: Int) {
        logic.thread(parentId).setLoading(true)
    }

    override suspend fun onGetNewerRepliesRequest(parentId: String, limit: Int, lastId: String?) {
        logic.thread(parentId).setLoading(true)
    }

    override suspend fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        parentId: String,
        firstId: String,
        limit: Int,
    ) {
        val threadLogic = logic.thread(parentId)

        threadLogic.setLoading(false)
        onResult(threadLogic, result, limit)
    }

    override suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    ) {
        val threadLogic = logic.thread(parentId)
        result.onSuccess { messages ->
            threadLogic.updateNewestMessageInThread(messages)
            threadLogic.setEndOfNewerMessages(messages.size < limit)
            threadLogic.upsertMessages(messages)
        }
        threadLogic.setLoading(false)
    }

    override suspend fun onGetRepliesAroundRequest(parentId: String, aroundId: String, limit: Int) {
        val threadLogic = logic.thread(parentId)
        threadLogic.setLoading(true)
        // Jumping to a mid-page means the newest page may no longer be loaded.
        threadLogic.setEndOfNewerMessages(false)
    }

    override suspend fun onGetRepliesAroundResult(
        result: Result<List<Message>>,
        parentId: String,
        aroundId: String,
        limit: Int,
    ) {
        val threadLogic = logic.thread(parentId)
        threadLogic.setLoading(false)
        result.onSuccess { messages ->
            threadLogic.updateOldestMessageInThread(messages)
            threadLogic.updateNewestMessageInThread(messages)
            setEndFlagsFromAroundPage(threadLogic, messages, aroundId)
            if (messages.size < limit) {
                threadLogic.setEndOfOlderMessages(true)
                threadLogic.setEndOfNewerMessages(true)
            }
            threadLogic.upsertMessages(messages)
        }
    }

    /**
     * The position of [aroundId] inside the page tells which end of the thread the page covers:
     * in the middle - both older and newer pages remain, in the first half - the oldest page is
     * loaded, in the second half - the newest page is loaded. When [aroundId] is not in the page,
     * the jump targeted the parent message, so the page starts at the oldest replies.
     * An even-sized page has two central positions and a centered response can place the target
     * on either of them, so both count as the middle.
     */
    private fun setEndFlagsFromAroundPage(threadLogic: ThreadLogic, messages: List<Message>, aroundId: String) {
        if (messages.isEmpty()) return
        val midPoint = (messages.size / 2.0).roundToInt() - 1
        val middleSize = if (messages.size % 2 == 0) 2 else 1
        val middleEnd = (midPoint + middleSize).coerceAtMost(messages.size)
        val middle = messages.subList(midPoint, middleEnd)
        val secondHalf = messages.subList(middleEnd, messages.size)
        when {
            middle.any { it.id == aroundId } -> {
                threadLogic.setEndOfOlderMessages(false)
                threadLogic.setEndOfNewerMessages(false)
            }
            secondHalf.any { it.id == aroundId } -> threadLogic.setEndOfNewerMessages(true)
            else -> threadLogic.setEndOfOlderMessages(true)
        }
    }

    private fun onResult(threadLogic: ThreadLogic, result: Result<List<Message>>, limit: Int) {
        if (result is Result.Success) {
            val newMessages = result.value
            threadLogic.updateOldestMessageInThread(newMessages)
            threadLogic.setEndOfOlderMessages(newMessages.size < limit)
            threadLogic.upsertMessages(newMessages)
        }
    }
}
