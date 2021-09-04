package io.getstream.chat.android.compose.handlers

import android.content.ClipData
import android.content.ClipboardManager
import io.getstream.chat.android.client.models.Message

/**
 * Abstraction over the [ClipboardHandlerImpl] that allows users to copy messages.
 */
public interface ClipboardHandler {

    /**
     * @param message The message to copy.
     */
    public fun copyMessage(message: Message)
}

/**
 * A simple implementation that relies on the [clipboardManager] to copy messages.
 *
 * @param clipboardManager System service that allows for clipboard operations, such as putting
 * new data on the clipboard.
 */
public class ClipboardHandlerImpl(private val clipboardManager: ClipboardManager) : ClipboardHandler {

    /**
     * Allows users to copy the message text.
     *
     * @param message Message to copy the text from.
     */
    override fun copyMessage(message: Message) {
        clipboardManager.setPrimaryClip(ClipData.newPlainText("message", message.text))
    }
}
