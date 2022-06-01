package com.getstream.sdk.chat.utils.typing

/**
 * Design to buffer typing inputs.
 *
 * Its implementation should receive keystroke events by calling [TypingUpdatesBuffer.keystroke]
 * which it will internally buffer and send start and stop typing API calls accordingly.
 * This cuts down on unnecessary API calls.
 *
 * For the default implementation see [DefaultTypingUpdatesBuffer].
 */
public interface TypingUpdatesBuffer {

    /**
     * Should be called on every keystroke.
     */
    public fun keystroke()

    /**
     * Should clear typing updates.
     * Useful for runtime hygiene such as responding to lifecycle events.
     */
    public fun clearTypingUpdates()
}