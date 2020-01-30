package io.getstream.chat.android.core.poc.library.notifications

import io.getstream.chat.android.core.poc.library.Message

interface NotificationMessageLoadListener {

    fun onLoadMessageSuccess(message: Message)

    fun onLoadMessageFail(messageId: String)
}