package com.getstream.sdk.chat.navigation.destinations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

abstract public class ChatDestination {

    protected Context context;

    public ChatDestination(Context context) {
        this.context = context;
    }

    public abstract void navigate();

    protected void start(Intent intent) {
        context.startActivity(intent);
    }

    protected void startForResult(Intent intent, int requestCode) {
        ((Activity) context).startActivityForResult(intent, requestCode);
    }
}