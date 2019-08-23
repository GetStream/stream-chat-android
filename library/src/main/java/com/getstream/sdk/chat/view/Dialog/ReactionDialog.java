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
    Channel channel;
    Message message;
    int messagePosition;
    RecyclerView recyclerView;
    MessageListViewStyle style;

    public ReactionDialog(@NonNull Context context) {
        super(context);
    }

    public ReactionDialog setChannel(Channel channel) {
        this.channel = channel;
        init();
        return this;
    }

    public ReactionDialog setMessage(Message message) {
        this.message = message;
        init();
        return this;
    }

    public ReactionDialog setMessagePosition(int messagePosition) {
        this.messagePosition = messagePosition;
        init();
        return this;
    }

    public ReactionDialog setRecyclerView(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        init();
        return this;
    }

    public ReactionDialog setStyle(MessageListViewStyle style) {
        this.style = style;
        init();
        return this;
    }

    public void init() {
        if (recyclerView == null ||
                channel == null ||
                message == null ||
                style == null ||
                messagePosition < 0
        )
            return;

        int firstListItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
        final int lastListItemPosition = firstListItemPosition + recyclerView.getChildCount() - 1;
        int childIndex;
        if (messagePosition < firstListItemPosition || messagePosition > lastListItemPosition) {
            childIndex = messagePosition;
        } else {
            childIndex = messagePosition - firstListItemPosition;
        }
        int originY = recyclerView.getChildAt(childIndex).getBottom();

        ReactionDlgView reactionDlgView = new ReactionDlgView(getContext());

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
        int screenHeight = Utils.getScreenResolution(getContext());
        wlp.y = originY - screenHeight / 2;
        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }

}
