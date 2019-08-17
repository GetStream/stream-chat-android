package com.getstream.sdk.chat.function;

import android.app.Dialog;
import android.content.Context;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter;;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.MessageListViewStyle;
import com.getstream.sdk.chat.view.ReactionDlgView;

public class ReactionFunction {

    public static void showReactionDialog(Context context, Channel channel, Message message, MessageListViewStyle style, int originY) {
        final Dialog dialog = new Dialog(context); // Context, this, etc.
        ReactionDlgView reactionDlgView = new ReactionDlgView(context);
        reactionDlgView.setMessagewithStyle(channel, message, view -> dialog.dismiss(), style);
        dialog.setContentView(reactionDlgView);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        dialog.show();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.x = 0;
        int screenHeight = Utils.getScreenResolution(context);
        wlp.y = originY - screenHeight / 2;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }

    public static void showMoreActionDialog(Context context, Channel channel,
                                            final Message message,
                                            final View.OnClickListener clickListener) {
        final Dialog dialog = new Dialog(context);
        // TODO: fix me
        if (true)
            dialog.setContentView(R.layout.dialog_moreaction_incoming);
        else {
            dialog.setContentView(R.layout.dialog_moreaction_outgoing);
            TextView tv_edit = dialog.findViewById(R.id.tv_edit);
            TextView tv_delete = dialog.findViewById(R.id.tv_delete);
            tv_edit.setOnClickListener((View v) -> {
                v.setTag(Constant.TAG_MOREACTION_EDIT);
                clickListener.onClick(v);
                dialog.dismiss();
            });
            tv_delete.setOnClickListener((View v) -> {
                v.setTag(Constant.TAG_MOREACTION_DELETE);
                clickListener.onClick(v);
                dialog.dismiss();
            });
        }


        RecyclerView rv_reaction = dialog.findViewById(R.id.rv_reaction);
        TextView tv_reply = dialog.findViewById(R.id.tv_reply);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(channel, message, false,null, (View v) -> {
            dialog.dismiss();
        });
        rv_reaction.setAdapter(reactionAdapter);

        tv_reply.setOnClickListener((View v) -> {
            v.setTag(Constant.TAG_MOREACTION_REPLY);
            clickListener.onClick(v);
            dialog.dismiss();
        });
        tv_cancel.setOnClickListener((View v) -> {
            dialog.dismiss();
        });

        dialog.show();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;

        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }
}
