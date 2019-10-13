package com.getstream.sdk.chat.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.Map;


public class ReactionDialogAdapter extends RecyclerView.Adapter<ReactionDialogAdapter.MyViewHolder> {

    private static final String TAG = ReactionDialogAdapter.class.getSimpleName();

    private Channel channel;
    private Message message;
    private View.OnClickListener clickListener;
    private Map<String, String> reactionTypes;
    private MessageListViewStyle style;

    public ReactionDialogAdapter(Channel channel,
                                 Message message,
                                 MessageListViewStyle style,
                                 View.OnClickListener clickListener) {
        this.channel = channel;
        this.message = message;
        this.reactionTypes = channel.getReactionTypes();
        this.style = style;
        this.clickListener = clickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stream_item_dialog_reaction, parent, false);

        MyViewHolder vh = new MyViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        applyStyle(holder);
        String key = (String) reactionTypes.keySet().toArray()[position];
        String value = reactionTypes.get(key);
        holder.tv_emoji.setText(value);

        if (message.getReactionCounts() == null) return;
        if (message.getReactionCounts().containsKey(key)) {
            holder.tv_count.setText(String.valueOf(message.getReactionCounts().get(key)));
        } else
            holder.tv_count.setText("");
    }

    private void applyStyle(final MyViewHolder holder) {
        holder.tv_emoji.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getReactionInputEmojiSize());
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.tv_emoji.getLayoutParams();
        params.leftMargin = style.getReactionInputEmojiMargin();
        params.rightMargin = style.getReactionInputEmojiMargin();
        params.topMargin = style.getReactionInputEmojiMargin();
        params.bottomMargin = style.getReactionInputEmojiMargin();
        holder.tv_emoji.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return reactionTypes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tv_emoji, tv_count;

        public MyViewHolder(View view) {
            super(view);
            tv_emoji = view.findViewById(R.id.tv_emoji);
            tv_count = view.findViewById(R.id.tv_count);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            String type = (String) reactionTypes.keySet().toArray()[getLayoutPosition()];

            boolean isReactioned = false;
            for (Reaction reaction : message.getLatestReactions()) {
                if (reaction.getType().equals(type)) {
                    User user = reaction.getUser();
                    if (user.getId().equals(StreamChat.getInstance(v.getContext()).getUserId())) {
                        isReactioned = true;
                        break;
                    }
                }
            }
            if (isReactioned)
                deleteReaction(v, type);
            else
                sendReaction(v, type);

        }

        private void sendReaction(final View view, String type) {
            channel.sendReaction(message.getId(), type, null, new MessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    clickListener.onClick(view);
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    clickListener.onClick(view);
                }
            });
        }

        private void deleteReaction(final View view, String type) {
            channel.deleteReaction(message.getId(), type, new MessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    clickListener.onClick(view);
                }

                @Override
                public void onError(String errMsg, int errCode) {
                    clickListener.onClick(view);
                }
            });
        }
    }

}
