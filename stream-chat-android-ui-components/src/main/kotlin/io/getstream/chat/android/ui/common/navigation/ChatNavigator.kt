package io.getstream.chat.android.ui.common.navigation

import com.getstream.sdk.chat.navigation.ChatNavigationHandler
import com.getstream.sdk.chat.navigation.destinations.ChatDestination

public class ChatNavigator(private val handler: ChatNavigationHandler = EMPTY_HANDLER) {
    public fun navigate(destination: ChatDestination) {
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
