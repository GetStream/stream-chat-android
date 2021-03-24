package com.getstream.sdk.chat.navigation

import com.getstream.sdk.chat.navigation.destinations.ChatDestination

public fun interface ChatNavigator {
    public fun navigate(destination: ChatDestination)
}
