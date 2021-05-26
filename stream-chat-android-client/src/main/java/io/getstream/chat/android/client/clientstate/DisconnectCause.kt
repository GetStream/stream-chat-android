package io.getstream.chat.android.client.clientstate

import io.getstream.chat.android.client.errors.ChatNetworkError

/**
 * Sealed class represents possible cause of disconnection.
 */
public sealed class DisconnectCause {

    /**
     * Happens when networks is not available anymore.
     */
    public object NetworkNotAvailable : DisconnectCause()

    /**
     * Happens when some non critical error occurs.
     * @param error Instance of [ChatNetworkError] as a reason of it.
     */
    public class Error(public val error: ChatNetworkError?) : DisconnectCause()

    /**
     * Happens when a critical error occurs. Connection can't be restored after such disconnection.
     * @param error Instance of [ChatNetworkError] as a reason of it.
     */
    public class UnrecoverableError(public val error: ChatNetworkError?) : DisconnectCause()

    /**
     * Happens when disconnection has been done intentionally. E.g. we release connection when app went to background.
     */
    public object ConnectionReleased : DisconnectCause()
}
