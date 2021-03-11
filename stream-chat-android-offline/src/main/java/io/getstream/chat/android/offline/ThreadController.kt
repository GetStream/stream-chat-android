package io.getstream.chat.android.offline

import io.getstream.chat.android.client.models.Message
import kotlinx.coroutines.flow.StateFlow

/**
 * The threadController exposes livedata for a thread
 *
 * - threadId (the id of the current thread)
 * - loadingOlderMessages (if we're currently loading older messages)
 * - endOfOlderMessages (if you've reached the end of older messages)
 */
public interface ThreadController {
    public val threadId: String
    public val messages: StateFlow<List<Message>>
    public val loadingOlderMessages: StateFlow<Boolean>
    public val endOfOlderMessages: StateFlow<Boolean>
    public fun getMessagesSorted(): List<Message>
}
