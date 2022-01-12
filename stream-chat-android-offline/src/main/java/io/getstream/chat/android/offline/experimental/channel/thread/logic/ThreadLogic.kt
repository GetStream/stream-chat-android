package io.getstream.chat.android.offline.experimental.channel.thread.logic

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState

/** Logic class for thread state management. Implements [ThreadQueryListener] as listener for LLC requests. */
@ExperimentalStreamChatApi
internal class ThreadLogic(private val mutableState: ThreadMutableState, private val channelLogic: ChannelLogic) :
    ThreadQueryListener {

    private val logger = ChatLogger.get("ThreadLogic")

    /** Runs precondition for loading more messages for thread. */
    internal fun precondition(): Result<Unit> {
        return if (mutableState.loadingOlderMessages.value) {
            val errorMsg = "already loading messages for this thread, ignoring the load more requests."
            logger.logI(errorMsg)
            Result(ChatError(errorMsg))
        } else {
            Result.success(Unit)
        }
    }

    /** Runs side effect when a request is going to be launched. */
    internal fun onRequest() {
        mutableState._loadingOlderMessages.value = true
    }

    /** Runs side effect when a result is obtained. */
    internal fun onResult(result: Result<List<Message>>, limit: Int) {
        if (result.isSuccess) {
            // Note that we don't handle offline storage for threads at the moment.
            val newMessages = result.data()
            channelLogic.upsertMessages(newMessages)
            mutableState._endOfOlderMessages.value = newMessages.size < limit
            mutableState._oldestInThread.value =
                newMessages.sortedBy { it.createdAt }.firstOrNull() ?: mutableState._oldestInThread.value
        }

        mutableState._loadingOlderMessages.value = false
    }

    override fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> = precondition()

    override fun onGetRepliesRequest(messageId: String, limit: Int) = onRequest()

    override fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int) =
        onResult(result, limit)

    override fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int) = precondition()

    override fun onGetRepliesMoreRequest(messageId: String, firstId: String, limit: Int) = onRequest()

    override fun onGetRepliesMoreResult(result: Result<List<Message>>, messageId: String, firstId: String, limit: Int) =
        onResult(result, limit)
}
