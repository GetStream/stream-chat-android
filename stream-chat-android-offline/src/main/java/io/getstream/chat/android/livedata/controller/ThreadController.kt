package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Message

/**
 * The threadController exposes livedata for a thread
 *
 * - threadId (the id of the current thread)
 * - loadingOlderMessages (if we're currently loading older messages)
 * - endOfOlderMessages (if you've reached the end of older messages)
 */
public sealed interface ThreadController {
    public val threadId: String
    public val messages: LiveData<List<Message>>
    public val loadingOlderMessages: LiveData<Boolean>
    public val endOfOlderMessages: LiveData<Boolean>
    public fun getMessagesSorted(): List<Message>
}
