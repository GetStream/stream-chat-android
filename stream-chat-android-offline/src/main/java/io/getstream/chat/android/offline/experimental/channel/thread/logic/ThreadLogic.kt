package io.getstream.chat.android.offline.experimental.channel.thread.logic

import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState

/** Logic class for thread state management. Implements [ThreadQueryListener] as listener for LLC requests. */
@ExperimentalStreamChatApi
internal class ThreadLogic(private val threadMutableState: ThreadMutableState) : ThreadQueryListener {

    override fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> {
        return super.onGetRepliesPrecondition(messageId, limit)
    }

    override fun onGetRepliesRequest(messageId: String, limit: Int) {
        super.onGetRepliesRequest(messageId, limit)
    }

    override fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int) {
        super.onGetRepliesResult(result, messageId, limit)
    }

    override fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int): Result<Unit> {
        return super.onGetRepliesMorePrecondition(messageId, firstId, limit)
    }

    override fun onGetRepliesMoreRequest(messageId: String, firstId: String, limit: Int) {
        super.onGetRepliesMoreRequest(messageId, firstId, limit)
    }

    override fun onGetRepliesMoreResult(result: Result<List<Message>>, messageId: String, firstId: String, limit: Int) {
        super.onGetRepliesMoreResult(result, messageId, firstId, limit)
    }
}
