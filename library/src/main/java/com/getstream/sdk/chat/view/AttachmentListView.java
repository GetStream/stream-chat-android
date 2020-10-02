package com.getstream.sdk.chat.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.adapter.AttachmentListItemAdapter;
import com.getstream.sdk.chat.adapter.AttachmentViewHolderFactory;
import com.getstream.sdk.chat.adapter.MessageListItem;

public class AttachmentListView extends RecyclerView {

    private AttachmentViewHolderFactory viewHolderFactory;
    private MessageListViewStyle style;

    public AttachmentListView(Context context) {
        super(context);
        setHasFixedSize(true);
    }

    public AttachmentListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setHasFixedSize(true);
    }

    public AttachmentListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setHasFixedSize(true);
    }

    public void init(
            @NonNull AttachmentViewHolderFactory viewHolderFactory,
            @NonNull MessageListViewStyle style
    ) {
        this.viewHolderFactory = viewHolderFactory;
        this.style = style;

        setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
    }

    public void setEntity(MessageListItem.MessageItem messageListItem) {
        if (viewHolderFactory == null) {
            throw new IllegalStateException("Please call init() before using setEntity()");
        }
        this.setAdapter(new AttachmentListItemAdapter(
                messageListItem,
                viewHolderFactory,
                style
        ));
    }
}
