package com.getstream.sdk.chat.navigation;

import com.getstream.sdk.chat.navigation.destinations.ChatDestination;

public class StreamChatNavigatorImpl implements StreamChatNavigator {

    private ChatNavigationHandler handler;

    @Override
    public void setHandler(ChatNavigationHandler handler) {
        this.handler = handler;
    }

    @Override
    public void navigate(ChatDestination destination) {
        boolean handled = false;
        if (handler != null) handled = handler.navigate(destination);
        if (!handled) defaultNavigation(destination);
    }

    private void defaultNavigation(ChatDestination destination) {
        destination.navigate();
    }

}