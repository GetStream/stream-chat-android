package io.getstream.chat.android.client.interceptor.message

import io.getstream.chat.android.client.interceptor.Interceptor
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

public interface PrepareMessageInterceptor: Interceptor {

    public fun prepareMessage(message: Message, channelId: String, channelType: String, user: User): Message
}
