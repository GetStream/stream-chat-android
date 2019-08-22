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
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class MoreActionDialog extends Dialog {
    public MoreActionDialog(@NonNull Context context, Channel channel, Message message, MessageListViewStyle style) {
        super(context);
        init(context, channel, message, style);
    }

    public void init(Context context, Channel channel, Message message, MessageListViewStyle style) {
        if (!message.getUserId().equals(StreamChat.getInstance(context).getUserId()))
            setContentView(com.getstream.sdk.chat.R.layout.dialog_moreaction_incoming);
        else {
            setContentView(com.getstream.sdk.chat.R.layout.dialog_moreaction_outgoing);
            TextView tv_edit = this.findViewById(com.getstream.sdk.chat.R.id.tv_edit);
            TextView tv_delete = this.findViewById(com.getstream.sdk.chat.R.id.tv_delete);
            tv_edit.setOnClickListener((View v) -> {
                v.setTag(Constant.TAG_MOREACTION_EDIT);
                dismiss();
            });
            tv_delete.setOnClickListener((View v) -> {
                v.setTag(Constant.TAG_MOREACTION_DELETE);
                dismiss();
            });
        }


        RecyclerView rv_reaction = findViewById(com.getstream.sdk.chat.R.id.rv_reaction);
        TextView tv_reply = findViewById(com.getstream.sdk.chat.R.id.tv_reply);
        TextView tv_cancel = findViewById(com.getstream.sdk.chat.R.id.tv_cancel);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(channel,
                message,
                false,
                style,
                (View v) -> dismiss());
        rv_reaction.setAdapter(reactionAdapter);

        tv_reply.setOnClickListener((View v) -> {
            v.setTag(Constant.TAG_MOREACTION_REPLY);
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
