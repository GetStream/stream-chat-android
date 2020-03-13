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

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.Reaction;


public class ReactionDialogAdapter extends RecyclerView.Adapter<ReactionDialogAdapter.ReactionViewHolder> {

    private static final String TAG = ReactionDialogAdapter.class.getSimpleName();

    private Message message;
    private View.OnClickListener clickListener;
    private Map<String, String> reactionTypes;
    private MessageListViewStyle style;

    public ReactionDialogAdapter(Message message,
                                 MessageListViewStyle style,
                                 View.OnClickListener clickListener) {
        this.message = message;
        this.reactionTypes = LlcMigrationUtils.getReactionTypes();
        this.style = style;
        this.clickListener = clickListener;
    }

    @Override
    public ReactionViewHolder onCreateViewHolder(ViewGroup parent,
                                                 int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stream_item_dialog_reaction, parent, false);

        return new ReactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ReactionViewHolder holder, int position) {
        applyStyle(holder);
        String key = (String) reactionTypes.keySet().toArray()[position];
        holder.bind(key, position);
    }

    private void applyStyle(final ReactionViewHolder holder) {
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

    public class ReactionViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_emoji, tv_count;
        private String reactionKey;
        private int position;

        private ReactionViewHolder(View view) {
            super(view);
            tv_emoji = view.findViewById(R.id.tv_emoji);
            tv_count = view.findViewById(R.id.tv_count);
            view.setOnClickListener(this);
        }

        void bind(String key, int position) {
            this.reactionKey = key;
            this.position = position;
            String value = reactionTypes.get(key);
            tv_emoji.setText(value);

            if (message.getReactionCounts().containsKey(key)) {
                tv_count.setText(String.valueOf(message.getReactionCounts().get(key)));
            } else
                tv_count.setText("");
        }

        @Override
        public void onClick(View v) {

            String type = (String) reactionTypes.keySet().toArray()[getLayoutPosition()];
            List<Reaction> ownReactions = message.getOwnReactions();
            boolean isReacted = false;

            for (Reaction ownReaction : ownReactions) {
                String reactionType = ownReaction.getType();

                if (type.equals(reactionType)) {
                    isReacted = true;
                    break;
                }
            }

            if (isReacted)
                deleteReaction(v, type);
            else
                sendReaction(v, type);

        }

        private void sendReaction(final View view, String type) {

            StreamChat.getInstance().sendReaction(message.getId(), type).enqueue(reactionResult -> {


                if (reactionResult.isSuccess()) {
                    addReaction(reactionResult.data());
                    notifyItemChanged(position);
                } else {

                }


                return null;
            });
        }

        private void deleteReaction(final View view, String type) {

            StreamChat.getInstance().deleteReaction(message.getId(), type).enqueue(messageResult -> {

                if (messageResult.isSuccess()) {
                    removeReaction(type);
                    notifyItemChanged(position);
                } else {

                }

                return null;
            });
        }

        private void addReaction(Reaction reaction) {
            Map<String, Integer> reactionCounts = message.getReactionCounts();

            message.getOwnReactions().add(reaction);

            if (reactionCounts.containsKey(reactionKey)) {
                reactionCounts.put(reactionKey, reactionCounts.get(reactionKey) + 1);
            } else {
                reactionCounts.put(reactionKey, 1);
            }
        }

        private void removeReaction(String type) {
            List<Reaction> ownReactions = message.getOwnReactions();
            Iterator<Reaction> iterator = ownReactions.iterator();

            while (iterator.hasNext()) {
                Reaction reaction = iterator.next();
                if (reaction.getType().equals(type)) {
                    iterator.remove();
                    break;
                }
            }

            Map<String, Integer> reactionCounts = message.getReactionCounts();

            if (reactionCounts.containsKey(reactionKey)) {
                int count = reactionCounts.get(reactionKey);
                if (count == 1) {
                    reactionCounts.remove(reactionKey);
                } else {
                    reactionCounts.put(reactionKey, count - 1);
                }
            }
        }
    }

}
