package com.getstream.sdk.chat.view.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.MessageListViewStyle;
import com.getstream.sdk.chat.view.ReactionDlgView;

public class ReactionDialog extends Dialog {
    public ReactionDialog(@NonNull Context context, Channel channel, Message message, int position, RecyclerView recyclerView, MessageListViewStyle style) {
        super(context);
        init(context, channel, message, position, recyclerView, style);
    }

    public void init(Context context, Channel channel, Message message, int position, RecyclerView recyclerView, MessageListViewStyle style) {
        int firstListItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        final int lastListItemPosition = firstListItemPosition + recyclerView.getChildCount() - 1;
        int childIndex;
        if (position < firstListItemPosition || position > lastListItemPosition) {
            childIndex = position;
        } else {
            childIndex = position - firstListItemPosition;
        }
        int originY = recyclerView.getChildAt(childIndex).getBottom();

        ReactionDlgView reactionDlgView = new ReactionDlgView(context);

        reactionDlgView.setMessagewithStyle(channel,
                message,
                style,
                view -> dismiss()
        );

        setContentView(reactionDlgView);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.x = 0;
        int screenHeight = Utils.getScreenResolution(context);
        wlp.y = originY - screenHeight / 2;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }

}
