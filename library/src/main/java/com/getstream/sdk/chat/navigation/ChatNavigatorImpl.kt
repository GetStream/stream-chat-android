package com.getstream.sdk.chat.navigation

import com.getstream.sdk.chat.navigation.destinations.ChatDestination

internal class ChatNavigatorImpl : ChatNavigator {
    private object EMPTY_HANDLER : ChatNavigationHandler {
        override fun navigate(destination: ChatDestination) = false
    }

    private var handler: ChatNavigationHandler = EMPTY_HANDLER

    override fun setHandler(handler: ChatNavigationHandler) {
        this.handler = handler
    }

    override fun navigate(destination: ChatDestination) {
        val handled = handler.navigate(destination)
        if (!handled) {
            performDefaultNavigation(destination)
        }
    }

    private fun performDefaultNavigation(destination: ChatDestination) {
        destination.navigate()
    }
}
