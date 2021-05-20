package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.errors.ChatNetworkError

public sealed class DisconnectCause {
    public object NetworkNotAvailable : DisconnectCause()
    public class Error(public val error: ChatNetworkError?) : DisconnectCause()
    public class UnrecoverableError(public val error: ChatNetworkError?) : DisconnectCause()
}
