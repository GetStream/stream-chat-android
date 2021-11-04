package io.getstream.chat.android.offline.thread

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalStreamChatApi::class)
public class ThreadController internal constructor(
    private val threadMutableState: ThreadMutableState,
    private val channelController: ChannelController,
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
        val result = channelController.loadOlderThreadMessages(threadId, limit, threadMutableState.firstMessage)
        if (result.isSuccess) {
            threadMutableState._endOfOlderMessages.value = result.data().size < limit
            threadMutableState.firstMessage =
                result.data().sortedBy { it.createdAt }.firstOrNull() ?: threadMutableState.firstMessage
        }

        threadMutableState._loadingOlderMessages.value = false
        return result
    }
}
