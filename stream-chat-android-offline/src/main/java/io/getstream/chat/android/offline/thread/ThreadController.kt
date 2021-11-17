package io.getstream.chat.android.offline.thread

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalStreamChatApi::class)
public class ThreadController internal constructor(
    private val threadMutableState: ThreadMutableState,
    private val client: ChatClient,
    private val channelLogic: ChannelLogic,
) {
    public val threadId: String by threadMutableState::parentId

    private val logger = ChatLogger.get("ThreadController")

    /** the sorted list of messages for this thread */
    public val messages: StateFlow<List<Message>> by threadMutableState::messages

    /** if we are currently loading older messages */
    public val loadingOlderMessages: StateFlow<Boolean> by threadMutableState::loadingOlderMessages

    /** if we've reached the earliest point in this thread */
    public val endOfOlderMessages: StateFlow<Boolean> by threadMutableState::endOfOlderMessages

    public fun getMessagesSorted(): List<Message> = messages.value

    internal suspend fun loadOlderMessages(limit: Int = 30): Result<List<Message>> {
        // TODO: offline storage for thread load more
        if (loadingOlderMessages.value) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.logI(errorMsg)
            return Result(ChatError(errorMsg))
        }
        threadMutableState._loadingOlderMessages.value = true
        val result = doLoadMore(limit, threadMutableState.firstMessage)
        if (result.isSuccess) {
            // Note that we don't handle offline storage for threads at the moment.
            val newMessages = result.data()
            channelLogic.upsertMessages(newMessages)
            threadMutableState._endOfOlderMessages.value = newMessages.size < limit
            threadMutableState.firstMessage =
                newMessages.sortedBy { it.createdAt }.firstOrNull() ?: threadMutableState.firstMessage
        }

        threadMutableState._loadingOlderMessages.value = false
        return result
    }

    private suspend fun doLoadMore(
        limit: Int,
        firstMessage: Message? = null,
    ): Result<List<Message>> = if (firstMessage != null) {
        client.getRepliesMore(threadId, firstMessage.id, limit).await()
    } else {
        client.getReplies(threadId, limit).await()
    }
}
