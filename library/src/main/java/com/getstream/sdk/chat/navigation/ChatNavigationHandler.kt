package com.getstream.sdk.chat.navigation

import com.getstream.sdk.chat.navigation.destinations.ChatDestination

public interface ChatNavigationHandler {
    /**
     * Attempt to navigate to the given [destination].
     *
     * @return true if navigation was successfully handled.
     */
    public fun navigate(destination: ChatDestination): Boolean
}
