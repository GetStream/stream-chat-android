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
interface ThreadController {
    val threadId: String
    val messages: LiveData<List<Message>>
    val loadingOlderMessages: LiveData<Boolean>
    val endOfOlderMessages: LiveData<Boolean>
    fun getMessagesSorted(): List<Message>
}
