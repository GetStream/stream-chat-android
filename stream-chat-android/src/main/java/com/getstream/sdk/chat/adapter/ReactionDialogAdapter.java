package com.getstream.sdk.chat.adapter;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.utils.UiUtils;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.Reaction;
import io.getstream.chat.android.livedata.ChatDomain;
import kotlin.Unit;

public class ReactionDialogAdapter extends RecyclerView.Adapter<ReactionDialogAdapter.ReactionViewHolder> {

    private static final String TAG = ReactionDialogAdapter.class.getSimpleName();

    private Message message;
    private String cid = "";
    private View.OnClickListener clickListener;
    private Map<String, String> reactionTypes;
    private MessageListViewStyle style;

    public ReactionDialogAdapter(Message message,
                                 MessageListViewStyle style,
                                 View.OnClickListener clickListener) {
        this.message = message;
        this.reactionTypes = UiUtils.getReactionTypes();
        this.style = style;
        this.clickListener = clickListener;
        if (!message.getCid().equals("")) {
            cid = message.getCid();
        }
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

            Map<String, Integer> reactionCounts = message.getReactionCounts();

            if (reactionCounts != null && reactionCounts.containsKey(key)) {
                tv_count.setText(String.valueOf(reactionCounts.get(key)));
            } else {
                tv_count.setText("");
            }
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

            // TODO: this seems like it should be handled by the viewModel, not here
            clickListener.onClick(view);
            Reaction reaction = new Reaction();
            reaction.setMessageId(message.getId());
            reaction.setType(type);
            if (!cid.equals("")) {
                ChatDomain.instance().getUseCases().getSendReaction().invoke(cid, reaction, false).enqueue(reactionResult -> Unit.INSTANCE);
            }

        }

        private void deleteReaction(final View view, String type) {
            clickListener.onClick(view);
            Reaction reaction = new Reaction();
            reaction.setMessageId(message.getId());
            reaction.setType(type);
            if (!cid.equals("")) {
                ChatDomain.instance().getUseCases().getDeleteReaction().invoke(cid, reaction).enqueue(reactionResult -> Unit.INSTANCE);
            }
        }

    }

}
