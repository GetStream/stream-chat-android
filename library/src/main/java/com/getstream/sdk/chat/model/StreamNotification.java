package com.getstream.sdk.chat.model;

import android.app.PendingIntent;

import androidx.annotation.Nullable;

import com.google.firebase.messaging.RemoteMessage;

public class StreamNotification {

    private int notificationId;
    private String channelName;
    private String messageText;
    private PendingIntent pendingIntent;
    private RemoteMessage remoteMessage;
    private Event event;

    public StreamNotification(int notificationId, @Nullable RemoteMessage remoteMessage, @Nullable Event event) {
        this.notificationId = notificationId;
        this.remoteMessage = remoteMessage;
        this.event = event;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getMessageText() {
        return messageText;
    }

    public PendingIntent getPendingIntent() {
        return pendingIntent;
    }

    public RemoteMessage getRemoteMessage() {
        return remoteMessage;
    }

    public Event getEvent() { return event; }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setPendingIntent(PendingIntent pendingIntent) {
        this.pendingIntent = pendingIntent;
    }
}
