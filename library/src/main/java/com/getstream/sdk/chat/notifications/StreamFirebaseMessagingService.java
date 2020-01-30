package com.getstream.sdk.chat.notifications;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.StreamChat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class StreamFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        StreamChat.getNotificationsManager().onReceiveFirebaseMessage(remoteMessage, this);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        StreamChat.getNotificationsManager().setFirebaseToken(token, this);
    }
}
