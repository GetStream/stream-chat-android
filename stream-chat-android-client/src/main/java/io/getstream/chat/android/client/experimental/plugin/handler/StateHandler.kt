package io.getstream.chat.android.client.experimental.plugin.handler

/**
 * Handles the state of stateful classes in the SDK. It can be used to clear the state of objects when a certain event
 * occurs.
 */
public interface StateHandler {

    /**
     * Register a function to clear state of objects.
     *
     * @param listener the function to be executed in a future moment to clear state.
     */
    public fun registerClearStateListener(listener: () -> Unit)

    /**
     * Notifies that state clear is necessary.
     */
    public fun clearState()
}
