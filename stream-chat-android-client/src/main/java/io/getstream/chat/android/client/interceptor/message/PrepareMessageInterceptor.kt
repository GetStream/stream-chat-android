package io.getstream.chat.android.client.interceptor.message

import io.getstream.chat.android.client.models.Message

public interface PrepareMessageInterceptor {

    public fun prepareMessage(message: Message): Message
}
