package com.getstream.sdk.chat.navigation;

import com.getstream.sdk.chat.navigation.destinations.ChatDestination;

public interface StreamChatNavigator {
    void setHandler(ChatNavigationHandler handler);

    void navigate(ChatDestination destination);
}
