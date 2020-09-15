package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message

interface NotificationLoadDataListener {

    fun onLoadSuccess(channel: Channel, message: Message)

    fun onLoadFail(messageId: String, error: ChatError)
}
