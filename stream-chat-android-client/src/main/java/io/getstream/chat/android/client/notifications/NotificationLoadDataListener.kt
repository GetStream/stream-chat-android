package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message

@Deprecated(
    message = "This class is not used anymore",
    level = DeprecationLevel.ERROR,
)
public interface NotificationLoadDataListener {

    public fun onLoadSuccess(channel: Channel, message: Message)

    public fun onLoadFail(messageId: String, error: ChatError)
}
