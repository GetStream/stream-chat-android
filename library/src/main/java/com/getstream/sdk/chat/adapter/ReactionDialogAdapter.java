package com.getstream.sdk.chat.adapter;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.enums.ReactionEmoji;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.interfaces.SendMessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;

import java.util.Arrays;
import java.util.List;


public class ReactionDialogAdapter extends RecyclerView.Adapter<ReactionDialogAdapter.MyViewHolder> {

    private final String TAG = ReactionDialogAdapter.class.getSimpleName();

    private Channel channel;
    private Message message;
    private View.OnClickListener clickListener;
    private boolean showAvatar;
    private List<String> types = Arrays.asList("like", "love", "haha", "wow", "sad", "angry");

    public ReactionDialogAdapter(Channel channel, Message message,
                                 boolean showAvatar,
                                 View.OnClickListener clickListener) {
        this.channel = channel;
        this.message = message;
        this.clickListener = clickListener;
        this.showAvatar = showAvatar;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_dialog_reaction, parent, false);

        MyViewHolder vh = new MyViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        String type = types.get(position);
        holder.tv_emoji.setText(ReactionEmoji.valueOf(type).get());

        if (!showAvatar) {
            holder.cv_avatar.setVisibility(View.GONE);
            holder.tv_initials.setVisibility(View.GONE);
            holder.tv_count.setVisibility(View.GONE);
        } else {
            holder.cv_avatar.setVisibility(View.INVISIBLE);
            holder.tv_initials.setVisibility(View.INVISIBLE);
            holder.tv_count.setVisibility(View.VISIBLE);
        }

        if (message.getReactionCounts() == null) return;
        if (message.getReactionCounts().containsKey(type)) {
            holder.tv_count.setText(String.valueOf(message.getReactionCounts().get(type)));

            User user = null;
            for (Reaction reaction : message.getLatestReactions()) {
                if (reaction.getType().equals(type)) {
                    user = reaction.getUser();
                    break;
                }
            }

            if (user != null && showAvatar) {
                String intials = user.getUserInitials();
                holder.tv_initials.setVisibility(View.VISIBLE);
                holder.cv_avatar.setVisibility(View.VISIBLE);
                holder.tv_initials.setText(intials);
                if (StringUtility.isValidImageUrl(user.getImage()))
                    Utils.circleImageLoad(holder.cv_avatar, user.getImage());
            }
        } else
            holder.tv_count.setText("");
    }

    @Override
    public int getItemCount() {
        return types.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tv_emoji, tv_count, tv_initials;
        public ImageView cv_avatar;

        public MyViewHolder(View view) {
            super(view);
            tv_initials = view.findViewById(R.id.tv_initials);
            tv_emoji = view.findViewById(R.id.tv_emoji);
            tv_count = view.findViewById(R.id.tv_count);
            cv_avatar = view.findViewById(R.id.cv_avatar);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "TAP : " + this.getLayoutPosition());
            String type = types.get(getLayoutPosition());
            boolean isReactioned = false;
            for (Reaction reaction : message.getLatestReactions()) {
                if (reaction.getType().equals(type)) {
                    User user = reaction.getUser();
                    try {
                        if (user.getId().equals(Global.client.user.getId())) {
                            isReactioned = true;
                            break;
                        }
                    } catch (Exception e) {
                    }

                }
            }
            if (isReactioned)
                deleteReaction(v, type);
            else
                sendReaction(v, type);

        }

        private void sendReaction(final View view, String type) {
            channel.sendReaction(message.getId(), type, new SendMessageCallback() {
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
            channel.deleteReaction(message.getId(), type, new SendMessageCallback() {
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
