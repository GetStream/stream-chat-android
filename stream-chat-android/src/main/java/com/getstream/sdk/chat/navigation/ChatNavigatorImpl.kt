package com.getstream.sdk.chat.navigation

import com.getstream.sdk.chat.navigation.destinations.ChatDestination
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class ChatNavigatorImpl(private val handler: ChatNavigationHandler) : ChatNavigator {

    override fun navigate(destination: ChatDestination) {
        val handled = handler.navigate(destination)
        if (!handled) {
            performDefaultNavigation(destination)
        }
    }

    private fun performDefaultNavigation(destination: ChatDestination) {
        destination.navigate()
    }

    public companion object {

        @JvmField
        public val EMPTY_HANDLER: ChatNavigationHandler = object : ChatNavigationHandler {
            override fun navigate(destination: ChatDestination) = false
        }
    }
}
