package com.getstream.sdk.chat.navigation

import com.getstream.sdk.chat.navigation.destinations.ChatDestination

interface ChatNavigationHandler {
    /**
     * Attempt to navigate to the given [destination].
     *
     * @return true if navigation was successfully handled.
     */
    fun navigate(destination: ChatDestination): Boolean
}
