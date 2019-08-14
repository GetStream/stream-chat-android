package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.annotation.NonNull;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.utils.ChannelListDiffCallback;
import com.getstream.sdk.chat.utils.MessageListDiffCallback;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class MessageListItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public enum EntityType {
        DATE_SEPARATOR, MESSAGE, TYPING
    }
    public class Entity {
        private EntityType type;
        private Message message;
        private Date date;
        private List<User> users;

        public void Entity(Date date) {
            this.type = EntityType.DATE_SEPARATOR;
            this.date = date;
        }

        public void Entity(Message message) {
            this.type = EntityType.MESSAGE;
            this.message = message;
        }

        public void Entity(List<User> users) {
            this.type = EntityType.TYPING;
            this.users = users;
        }

        public Message getMessage() {
            return message;
        }

        public Date getDate() {
            return date;
        }

        public List<User> getUsers() {
            return users;
        }

        public EntityType getType() {
            return type;
        }
    }

    private final String TAG = MessageListItemAdapter.class.getSimpleName();

    private ChannelState channelState;
    private View.OnClickListener clickListener;
    private View.OnLongClickListener longClickListener;
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

    public void replaceMessages(List<Message> newMessages) {
        final DiffUtil.DiffResult result = DiffUtil.calculateDiff(
                new MessageListDiffCallback(messageList, newMessages), true);
        messageList = newMessages;
        // only update those rows that change...
        result.dispatchUpdatesTo(this);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            Entity entity = entityList.get(position);
            // TODO: determine position and mine/theirs
            return viewHolderFactory.getEntityViewType(entity, true, MessageViewHolderFactory.Position.BOTTOM);
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
        ((BaseMessageListItemViewHolder) holder).bind(this.context, this.channelState, entity, position, isThread, clickListener, longClickListener);

    }

    @Override
    public int getItemCount() {
        if (messageList != null) {
            return messageList.size() + 1;
        } else {
            return 0;
        }
    }
}
