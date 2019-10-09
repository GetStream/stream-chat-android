package com.getstream.sdk.chat.rest;

class EventHandlerThread extends Thread {
    EventHandler mHandler;

    public EventHandlerThread(WebSocketService webSocketService) {
        mHandler = new EventHandler(webSocketService);
    }


}
