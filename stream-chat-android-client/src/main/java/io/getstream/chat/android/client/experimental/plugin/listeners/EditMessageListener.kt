package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

public interface EditMessageListener {
    public suspend fun onMessageEditRequest(message: Message)

    public fun onMessageEditResult(result: Result<Message>)
}
