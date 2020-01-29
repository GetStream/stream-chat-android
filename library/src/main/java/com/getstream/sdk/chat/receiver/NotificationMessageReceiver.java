package com.getstream.sdk.chat.receiver;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.RemoteInput;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Config;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;

import org.jetbrains.annotations.NotNull;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationMessageReceiver extends BroadcastReceiver {

    public static final String ACTION_READ = "com.getstream.sdk.chat.READ";
    public static final String ACTION_REPLY = "com.getstream.sdk.chat.REPLY";
    public static final String KEY_NOTIFICATION_ID = "notification_id";
    public static final String KEY_CHANNEL_ID = "id";
    public static final String KEY_CHANNEL_TYPE = "type";
    public static final String KEY_TEXT_REPLY = "text_reply";

    private final static String TAG = NotificationMessageReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action != null) {
            switch (intent.getAction()) {
                case ACTION_READ:
                    markAsRead(context, intent.getStringExtra(KEY_CHANNEL_ID), intent.getStringExtra(KEY_CHANNEL_TYPE));
                    break;
                case ACTION_REPLY:
                    Bundle results = RemoteInput.getResultsFromIntent(intent);
                    if (results != null) {
                        replyText(context, intent.getStringExtra(KEY_CHANNEL_ID), intent.getStringExtra(KEY_CHANNEL_TYPE), results.getCharSequence(KEY_TEXT_REPLY));
                    }
                    break;
                default:
                    break;
            }
        }
        cancelNotification(context, intent.getIntExtra(KEY_NOTIFICATION_ID, 0));
    }

    private void replyText(@NotNull Context context, String id, String type, CharSequence messageChars) {
        if (id == null || type == null || id.isEmpty() || type.isEmpty()) {
            Log.e(TAG, "Invalid replyText  parameters: id: " +  id + " type: " + type);
            return;
        }

        if (messageChars == null || messageChars.toString().isEmpty()) {
            Log.e(TAG, "replyText: messageChars is empty or null: " + messageChars);
            return;
        }

        Channel channel = new Channel(StreamChat.getInstance(context), type, id);
        Config config = new Config();
        config.setReadEvents(true);
        channel.setConfig(config);

        Message msg = new Message();
        msg.setText(messageChars.toString());

        channel.sendMessage(msg, new MessageCallback() {
            @Override
            public void onSuccess(MessageResponse response) {
                Log.i(TAG, "Reply message sent success.");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "Cant send reply. Error: " + errMsg + " Code: " + errCode);
            }
        });
    }

    private void markAsRead(@NotNull Context context, String id, String type) {
        if (id == null || type == null || id.isEmpty() || type.isEmpty()) {
            Log.e(TAG, "Invalid replyText  parameters: id:" + id + " type:" + type);
            return;
        }

        Channel channel = new Channel(StreamChat.getInstance(context), type, id);
        Config config = new Config();
        config.setReadEvents(true);
        channel.setConfig(config);

        channel.markRead(new EventCallback() {
            @Override
            public void onSuccess(EventResponse response) {
                Log.i(TAG, "Channel marked as read");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Log.e(TAG, "Cant mark as read. Error: " + errMsg + " Code: " + errCode);
            }
        });
    }

    private void cancelNotification(@NotNull Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        } else {
            Log.w(TAG, "Can\'t get Notification Manager. notificationManager is null");
        }
    }
}
