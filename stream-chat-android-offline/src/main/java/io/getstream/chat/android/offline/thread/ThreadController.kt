package io.getstream.chat.android.offline.thread

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.thread.logic.ThreadLogic
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalStreamChatApi::class)
public class ThreadController internal constructor(
    private val threadMutableState: ThreadMutableState,
    private val threadLogic: ThreadLogic,
    private val client: ChatClient,
) {
    public val threadId: String by threadMutableState::parentId

    /** the sorted list of messages for this thread */
    public val messages: StateFlow<List<Message>> by threadMutableState::messages

    /** if we are currently loading older messages */
    public val loadingOlderMessages: StateFlow<Boolean> by threadMutableState::loadingOlderMessages

    /** if we've reached the earliest point in this thread */
    public val endOfOlderMessages: StateFlow<Boolean> by threadMutableState::endOfOlderMessages

    public fun getMessagesSorted(): List<Message> = messages.value

    internal suspend fun loadOlderMessages(limit: Int = 30): Result<List<Message>> {
        // TODO: offline storage for thread load more
        val preconditionResult = threadLogic.precondition()
        if (preconditionResult.isError) {
            return Result(preconditionResult.error())
        }
        threadLogic.onRequest()

        val result = doLoadMore(limit, threadMutableState.oldestInThread.value)

        threadLogic.onResult(result, limit)

        return result
    }

    private suspend fun doLoadMore(
        limit: Int,
        firstMessage: Message? = null,
    ): Result<List<Message>> = if (firstMessage != null) {
        client.getRepliesMoreInternal(threadId, firstMessage.id, limit).await()
    } else {
        client.getRepliesInternal(threadId, limit).await()
    }
}
