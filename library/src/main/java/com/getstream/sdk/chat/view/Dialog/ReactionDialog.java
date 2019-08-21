package com.getstream.sdk.chat.view.Dialog;

import android.app.Dialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class ReactionDialog extends Dialog {
    public ReactionDialog(@NonNull Context context, Channel channel, Message message, MessageListViewStyle style) {
        super(context);
        init(context, channel, message, style);
    }
    public void init(Context context, Channel channel, Message message, MessageListViewStyle style) {

    }
}
