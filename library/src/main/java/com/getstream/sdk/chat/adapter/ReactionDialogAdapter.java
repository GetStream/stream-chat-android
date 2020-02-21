package com.getstream.sdk.chat.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.utils.LlcMigrationUtils;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.Map;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.Reaction;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.Result;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;


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
        this.reactionTypes = LlcMigrationUtils.getReactionTypes();
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

                    User currentUser = StreamChat.getInstance().getCurrentUser();
                    String id = currentUser.getId();

                    if (user.getId().equals(id)) {
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

            StreamChat.getInstance().sendReaction(message.getId(), type).enqueue(new Function1<Result<Reaction>, Unit>() {
                @Override
                public Unit invoke(Result<Reaction> reactionResult) {

                    if (reactionResult.isSuccess()) {
                        clickListener.onClick(view);
                    } else {
                        clickListener.onClick(view);
                    }

                    return null;
                }
            });
        }

        private void deleteReaction(final View view, String type) {

            StreamChat.getInstance().deleteReaction(message.getId(), type).enqueue(new Function1<Result<Message>, Unit>() {
                @Override
                public Unit invoke(Result<Message> messageResult) {

                    if (messageResult.isSuccess()) {
                        clickListener.onClick(view);
                    } else {
                        clickListener.onClick(view);
                    }

                    return null;
                }
            });
        }
    }

}
