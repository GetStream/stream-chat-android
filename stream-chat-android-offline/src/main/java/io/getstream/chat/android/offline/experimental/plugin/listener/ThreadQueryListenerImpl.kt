package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.ThreadQueryListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

internal class ThreadQueryListenerImpl(private val logic: LogicRegistry) : ThreadQueryListener {

    override fun onGetRepliesPrecondition(messageId: String, limit: Int): Result<Unit> =
        logic.thread(messageId).onGetRepliesPrecondition(messageId, limit)

    override fun onGetRepliesRequest(messageId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesRequest(messageId, limit)

    override fun onGetRepliesResult(result: Result<List<Message>>, messageId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesResult(result, messageId, limit)

    override fun onGetRepliesMorePrecondition(messageId: String, firstId: String, limit: Int): Result<Unit> =
        logic.thread(messageId).onGetRepliesMorePrecondition(messageId, firstId, limit)

    override fun onGetRepliesMoreRequest(messageId: String, firstId: String, limit: Int): Unit =
        logic.thread(messageId).onGetRepliesMoreRequest(messageId, firstId, limit)

    override fun onGetRepliesMoreResult(
        result: Result<List<Message>>,
        messageId: String,
        firstId: String,
        limit: Int,
    ): Unit = logic.thread(messageId).onGetRepliesMoreResult(result, messageId, firstId, limit)
}
