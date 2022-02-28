package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.utils.Result
import java.util.Date

public interface SendEventListener {

    public fun onSendEventPrecondition(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    ): Result<Unit>

    public fun onSendEventRequest(
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    )

    public fun onSendEventResult(
        result: Result<ChatEvent>,
        eventType: String,
        channelType: String,
        channelId: String,
        extraData: Map<Any, Any>,
        eventTime: Date,
    )
}
