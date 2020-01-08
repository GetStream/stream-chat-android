package com.getstream.sdk.chat.navigation;

import com.getstream.sdk.chat.navigation.destinations.ChatDestination;

public interface ChatNavigationHandler {
    boolean navigate(ChatDestination destination);
}
