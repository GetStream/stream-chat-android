package com.getstream.sdk.chat.rest;

import android.os.Looper;

class EventHandlerThread extends Thread {
    EventHandler mHandler;
    private WebSocketService webSocketService;

    public EventHandlerThread(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    public void run() {
        Looper.prepare();
        mHandler = new EventHandler(webSocketService);
        Looper.loop();
    }
}
