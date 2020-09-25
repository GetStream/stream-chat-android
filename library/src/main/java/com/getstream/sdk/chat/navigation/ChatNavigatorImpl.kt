package com.getstream.sdk.chat.navigation

import com.getstream.sdk.chat.navigation.destinations.ChatDestination

class ChatNavigatorImpl : ChatNavigator {
    private var handler: ChatNavigationHandler? = null

    override fun setHandler(handler: ChatNavigationHandler) {
        this.handler = handler
    }

    override fun navigate(destination: ChatDestination) {
        val handler = handler
        if (handler == null || !handler.navigate(destination)) {
            performDefaultNavigation(destination)
        }
    }

    private fun performDefaultNavigation(destination: ChatDestination) {
        destination.navigate()
    }
}
