package com.getstream.sdk.chat.notifications;

import android.app.Notification;
import android.content.Context;

import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.notifications.options.NotificationOptions;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

/*
 * Created by Anton Bevza on 2019-11-14.
 */
public interface NotificationsManager {

    void setFirebaseToken(@NotNull String firebaseToken, @NotNull Context context);

    void onReceiveFirebaseMessage(@NotNull RemoteMessage remoteMessage, @NotNull Context context);

    void onReceiveWebSocketEvent(@NotNull Event event, @NotNull Context context);

    void showNotification(@NotNull Notification notification, @NotNull Context context);

    NotificationOptions getNotificationsOptions();
}
