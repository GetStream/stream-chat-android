package io.getstream.chat.android.offline.experimental.channel.thread

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadState
import io.getstream.chat.android.offline.experimental.extensions.state
import io.getstream.chat.android.offline.experimental.plugin.query.QueryReference

@InternalStreamChatApi
/** Reference for the [ChatClient.getReplies] request. */
public class RepliesQueryReference(
    private val messageId: String,
    private val limit: Int,
    private val chatClient: ChatClient,
) : QueryReference<List<Message>, ThreadState> {

    /** Returns a call instance representing output of the [ChatClient.getReplies] request. */
    override fun get(): Call<List<Message>> {
        return chatClient.getReplies(messageId, limit)
    }

    /**
     * Returns [ThreadState] for the thread replies of a message with id equal to [messageId]. And fill it by data from
     * [ChatClient.getReplies]. We use a coroutine instead of a simple enqueue() call, to have control over the
     * lifecycle of the request.
     *
     * @param scope Coroutine scope where initial data filling action is being invoked.
     */
    override fun asState(): ThreadState {
        get().enqueue()
        return chatClient.state.thread(messageId)
    }
}
