package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.model.message.Reaction;
import com.getstream.sdk.chat.rest.apimodel.request.ReactionRequest;
import com.getstream.sdk.chat.rest.apimodel.response.MessageResponse;
import com.getstream.sdk.chat.rest.controller.RestController;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;

import java.util.Arrays;
import java.util.List;


public class ReactionDialogAdapter extends RecyclerView.Adapter<ReactionDialogAdapter.MyViewHolder> {

    private final String TAG = ReactionDialogAdapter.class.getSimpleName();

    private Context context;
    private Message message;
    private RestController mRestController;
    private View.OnClickListener clickListener;
    private boolean showAvatar;
    private List<String> types = Arrays.asList("like", "love", "haha", "wow", "sad", "angry");

    public ReactionDialogAdapter(Context context, Message message, RestController mRestController, boolean showAvatar, View.OnClickListener clickListener) {
        this.context = context;
        this.message = message;
        this.mRestController = mRestController;
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
        holder.tv_emoji.setText(Reaction.Type.valueOf(type).get());

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
                        if (user.getId().equals(Global.streamChat.getUser().getId())) {
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
            ReactionRequest request = new ReactionRequest(type);
            RestController.SendMessageCallback callback = new RestController.SendMessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    Log.d(TAG, "Reaction Send!");
                    clickListener.onClick(view);
                }
            };
            mRestController.sendReaction(message.getId(), request, callback, new RestController.ErrCallback() {
                @Override
                public void onError(String errMsg, int errCode) {
                    Log.d(TAG, "Send Reaction Failed!" + errMsg);
                    clickListener.onClick(view);
                }
            });
        }

        private void deleteReaction(final View view, String type) {
            RestController.SendMessageCallback callback = new RestController.SendMessageCallback() {
                @Override
                public void onSuccess(MessageResponse response) {
                    Log.d(TAG, "Reaction Deleted!");
                    clickListener.onClick(view);

                }
            };
            mRestController.deleteReaction(message.getId(), type, callback, new RestController.ErrCallback() {
                @Override
                public void onError(String errMsg, int errCode) {
                    Log.d(TAG, "Send Reaction Failed!");
                    clickListener.onClick(view);
                }
            });
        }
    }
}
