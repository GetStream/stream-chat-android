package com.getstream.sdk.chat.navigation

import com.getstream.sdk.chat.navigation.destinations.ChatDestination

public interface ChatNavigator {
    public fun setHandler(handler: ChatNavigationHandler)
    public fun navigate(destination: ChatDestination)
}
