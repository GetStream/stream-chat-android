package io.getstream.chat.android.offline.experimental.channel.thread.state

import io.getstream.chat.android.client.models.Message
import kotlinx.coroutines.flow.StateFlow

/** State container with reactive data of a thread.*/
public interface ThreadState {

    /** The message id for the parent of this thread. */
    public val parentId: String

    /** The sorted list of messages for this thread. */
    public val messages: StateFlow<List<Message>>

    /** If we are currently loading older messages. */
    public val loadingOlderMessages: StateFlow<Boolean>

    /** If we've reached the earliest point in this thread. */
    public val endOfOlderMessages: StateFlow<Boolean>

    /** The oldest message available in this thread state. It's null when we haven't loaded any messages in thread yet. */
    public val oldestInThread: StateFlow<Message?>
}
