package com.getstream.sdk.chat.navigation

import com.getstream.sdk.chat.navigation.destinations.ChatDestination

interface ChatNavigator {
    fun setHandler(handler: ChatNavigationHandler)
    fun navigate(destination: ChatDestination)
}
