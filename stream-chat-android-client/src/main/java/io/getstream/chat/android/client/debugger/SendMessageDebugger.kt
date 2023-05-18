package io.getstream.chat.android.client.debugger

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

public interface SendMessageDebugger {
    public fun onStart(message: Message) {}
    public fun onInterceptionStart(message: Message) {}
    public fun onInterceptionUpdate(message: Message) {}
    public fun onInterceptionStop(result: Result<Message>) {}
    public fun onSendStart(message: Message) {}
    public fun onSendStop(result: Result<Message>) {}
    public fun onStop(result: Result<Message>) {}
}

internal object StubSendMessageDebugger : SendMessageDebugger