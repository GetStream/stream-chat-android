package com.getstream.sdk.chat.function;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter;
import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Utils;

public class ReactionFunction {

    public static void showReactionDialog(Context context, Message message, int originY){
        final Dialog dialog = new Dialog(context); // Context, this, etc.
        dialog.setContentView(R.layout.dialog_reaction);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        RecyclerView rv_reaction = dialog.findViewById(R.id.rv_reaction);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(context, message, Global.mRestController, true, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        rv_reaction.setAdapter(reactionAdapter);

        dialog.show();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.x = 0;
        int screenHeight = Utils.getScreenResolution(context);
        wlp.y = originY - screenHeight/2;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }

    public static void showMoreActionDialog(Context context, final Message message, final View.OnClickListener clickListener){
        final Dialog dialog = new Dialog(context);
        if (message.isIncoming())
            dialog.setContentView(R.layout.dialog_moreaction_incoming);
        else{
            dialog.setContentView(R.layout.dialog_moreaction_outgoing);
            TextView tv_edit = dialog.findViewById(R.id.tv_edit);
            TextView tv_delete = dialog.findViewById(R.id.tv_delete);
            tv_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setTag(Constant.TAG_MOREACTION_EDIT);
                    clickListener.onClick(v);
                    dialog.dismiss();
                }
            });
            tv_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setTag(Constant.TAG_MOREACTION_DELETE);
                    clickListener.onClick(v);
                    dialog.dismiss();
                }
            });
        }


        RecyclerView rv_reaction = dialog.findViewById(R.id.rv_reaction);
        TextView tv_reply = dialog.findViewById(R.id.tv_reply);
        TextView tv_cancel = dialog.findViewById(R.id.tv_cancel);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(context, message, Global.mRestController,false, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        rv_reaction.setAdapter(reactionAdapter);

        tv_reply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setTag(Constant.TAG_MOREACTION_REPLY);
                clickListener.onClick(v);
                dialog.dismiss();
            }
        });
        tv_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;

        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }
}
