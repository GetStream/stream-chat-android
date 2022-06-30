package io.getstream.chat.android.client.interceptor.message

import io.getstream.chat.android.client.interceptor.Interceptor
import io.getstream.chat.android.client.models.Message

public interface PrepareMessageInterceptor : Interceptor {

    public fun prepareMessage(message: Message, channelId: String, channelType: String, userId: String): Message
}
