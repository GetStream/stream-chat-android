package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.annotation.NonNull;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.EntityListDiffCallback;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;

public class MessageListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public enum EntityType {
        DATE_SEPARATOR, MESSAGE, TYPING
    }

    private final String TAG = MessageListItemAdapter.class.getSimpleName();

    private ChannelState channelState;
    private MessageListView.MessageClickListener messageClickListener;
    private MessageListView.AttachmentClickListener attachmentClickListener;
    private List<Entity> entityList;
    private boolean isThread;
    private MessageListViewStyle style;

    private Context context;
    private String className;
    private int itemLayoutId;
    private MessageViewHolderFactory viewHolderFactory;

    public MessageListViewStyle getStyle() {
        return style;
    }

    public void setFactory(MessageViewHolderFactory factory) {
        this.viewHolderFactory = factory;
    }

    public MessageListItemAdapter(Context context, ChannelState channelState, @NonNull List<Entity> entityList) {
        this.context = context;
        this.viewHolderFactory = new MessageViewHolderFactory();
        this.channelState = channelState;
        this.entityList = entityList;
    }


    public MessageListItemAdapter(Context context, ChannelState channelState, @NonNull List<Entity> entityList, MessageViewHolderFactory factory) {
        this.context = context;
        this.channelState = channelState;
        this.entityList = entityList;
        this.viewHolderFactory = factory;
    }


    public MessageListItemAdapter(Context context) {
        this.context = context;
        this.viewHolderFactory = new MessageViewHolderFactory();
        this.entityList = new ArrayList<>();
    }

    public void setStyle(MessageListViewStyle s) {
        style = s;
    }

    public void replaceEntities(List<Entity> newEntities) {
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new EntityListDiffCallback(entityList, newEntities), true);
        entityList = newEntities;
        // only update those rows that change...
        result.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            Entity entity = entityList.get(position);
            // TODO: determine position and mine/theirs
            return viewHolderFactory.getEntityViewType(entity, entity.isMine(), entity.getPositions());
        } catch(IndexOutOfBoundsException e) {
            return 0;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        BaseMessageListItemViewHolder holder = this.viewHolderFactory.createMessageViewHolder(this, parent, viewType);
        return holder;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Entity entity = entityList.get(position);
        ((BaseMessageListItemViewHolder) holder).bind(this.context, this.channelState, entity, position, isThread, messageClickListener, attachmentClickListener);

    }

    public void setMessageClickListener(MessageListView.MessageClickListener messageClickListener) {
        this.messageClickListener = messageClickListener;
    }

    public void setAttachmentClickListener(MessageListView.AttachmentClickListener attachmentClickListener) {
        this.attachmentClickListener = attachmentClickListener;
    }

    @Override
    public int getItemCount() {
        return entityList.size();
    }
}
