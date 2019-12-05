package com.getstream.sdk.chat.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.response.EventResponse;

import java.util.List;

public class NotificationMessageReceiver extends BroadcastReceiver {

    public static final String ACTION_READ = "com.getstream.sdk.chat.READ";
    public static final String ACTION_REPLY = "com.getstream.sdk.chat.REPLY";
    public static final String KEY_CHANNEL_ID = "id";
    public static final String KEY_CHANNEL_TYPE = "type";

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
                    //replyText = results.getCharSequence(EXTRA_TEXT_REPLY);
                    break;
                default:
                    break;
            }
        }
    }

    private void markAsRead(Context context, String id, String type) {
        Channel channel = new Channel(StreamChat.getInstance(context), type, id);

        //if (channel != null) {
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
        /*} else {
            Log.w(TAG, "Can't find channel with cid:" + cid);
        }*/
    }
}
