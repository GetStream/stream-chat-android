package com.getstream.sdk.chat.notifications.options;

import android.app.PendingIntent;
import android.content.Context;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.model.Event;
import com.google.firebase.messaging.RemoteMessage;

/*
 * Created by Anton Bevza on 2019-11-18.
 */
public interface NotificationIntentProvider {
    PendingIntent getIntentForFirebaseMessage(@NonNull Context context, @NonNull RemoteMessage remoteMessage);

    PendingIntent getIntentForWebSocketEvent(@NonNull Context context, @NonNull Event event);
}
