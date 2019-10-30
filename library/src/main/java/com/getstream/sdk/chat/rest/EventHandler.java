package com.getstream.sdk.chat.rest;

import android.os.Handler;
import android.os.Message;

import com.getstream.sdk.chat.model.Event;


public class EventHandler extends Handler {
    private WebSocketService webSocketService;

    public EventHandler(WebSocketService webSocketService) {
        this.webSocketService = webSocketService;
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        webSocketService.getWebSocketListener().onWSEvent((Event) msg.obj);
    }
}
