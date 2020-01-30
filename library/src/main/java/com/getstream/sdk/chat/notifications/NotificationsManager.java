package com.getstream.sdk.chat.notifications;

import android.content.Context;

import com.getstream.sdk.chat.model.Event;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

/*
 * Created by Anton Bevza on 2019-11-14.
 */
public interface NotificationsManager {

    void setFirebaseToken(@NotNull String firebaseToken, @NotNull Context context);

    void onReceiveFirebaseMessage(@NotNull RemoteMessage remoteMessage, @NotNull Context context);

    void onReceiveWebSocketEvent(@NotNull Event event, @NotNull Context context);

    void handleRemoteMessage(Context context, RemoteMessage remoteMessage);

    void handleEvent(Context context, Event event);

    void setFailMessageListener(NotificationMessageLoadListener failMessageListener);
}
