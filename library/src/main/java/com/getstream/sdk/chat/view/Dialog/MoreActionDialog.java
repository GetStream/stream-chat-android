package com.getstream.sdk.chat.view.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class MoreActionDialog extends Dialog {
    Channel channel;
    Message message;
    MessageListViewStyle style;

    public MoreActionDialog(@NonNull Context context) {
        super(context);
    }

    public MoreActionDialog setChannel(Channel channel) {
        this.channel = channel;
        init();
        return this;
    }

    public MoreActionDialog setMessage(Message message) {
        this.message = message;
        init();
        return this;
    }

    public MoreActionDialog setStyle(MessageListViewStyle style) {
        this.style = style;
        init();
        return this;
    }

    public void init() {
        if (channel == null || message == null || style == null)
            return;

        if (!message.getUserId().equals(StreamChat.getInstance(getContext()).getUserId()))
            setContentView(com.getstream.sdk.chat.R.layout.dialog_moreaction_incoming);
        else {
            setContentView(com.getstream.sdk.chat.R.layout.dialog_moreaction_outgoing);
            TextView tv_edit = findViewById(com.getstream.sdk.chat.R.id.tv_edit);
            TextView tv_delete = findViewById(com.getstream.sdk.chat.R.id.tv_delete);
            tv_edit.setOnClickListener(view -> {

                dismiss();
            });
            tv_delete.setOnClickListener(view -> {
                channel.deleteMessage(message,
                        new MessageCallback() {
                            @Override
                            public void onSuccess(MessageResponse response) {
                                Utils.showMessage(getContext(), "Deleted Successfully");
                                dismiss();
                            }

                            @Override
                            public void onError(String errMsg, int errCode) {
                                Utils.showMessage(getContext(), errMsg);
                                dismiss();
                            }
                        });
            });
        }


        RecyclerView rv_reaction = findViewById(com.getstream.sdk.chat.R.id.rv_reaction);
        TextView tv_reply = findViewById(com.getstream.sdk.chat.R.id.tv_reply);
        TextView tv_cancel = findViewById(com.getstream.sdk.chat.R.id.tv_cancel);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(channel,
                message,
                false,
                style,
                (View v) -> dismiss());
        rv_reaction.setAdapter(reactionAdapter);

        tv_reply.setOnClickListener((View v) -> {

            dismiss();
        });
        tv_cancel.setOnClickListener((View v) -> dismiss());

        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;

        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }
}
