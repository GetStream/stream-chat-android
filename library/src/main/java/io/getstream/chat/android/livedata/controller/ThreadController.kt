package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import io.getstream.chat.android.client.models.Message

interface ThreadController {
    var threadId: String
    val messages: LiveData<List<Message>>
    val loadingOlderMessages: LiveData<Boolean>
    val endOfOlderMessages: LiveData<Boolean>
    fun getMessagesSorted(): List<Message>
}
