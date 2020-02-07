package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.models.Message

interface NotificationMessageLoadListener {

    fun onLoadMessageSuccess(message: Message)

    fun onLoadMessageFail(messageId: String)
}