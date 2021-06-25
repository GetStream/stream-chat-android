package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.getstream.chat.android.client.logger.ChatLogger;

public class ReactionListItemAdapter extends RecyclerView.Adapter<ReactionListItemAdapter.MyViewHolder> {

    private final String TAG = ReactionListItemAdapter.class.getSimpleName();

    private Context context;
    private List<String> reactions = new ArrayList<>();
    private int reactionCount;
    private Map<String, String> reactionTypes;
    private MessageListViewStyle style;

    public ReactionListItemAdapter(Context context, Map<String, Integer> reactionCountMap, Map<String, String> reactionTypes, MessageListViewStyle style) {
        this.context = context;
        this.reactionTypes = reactionTypes;
        this.style = style;
        Set keys = reactionCountMap.keySet();
        reactionCount = 0;
        for (Object key : keys) {
            this.reactions.add(key.toString());
            reactionCount += reactionCountMap.get(key);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stream_item_reaction, parent, false);

        MyViewHolder vh = new MyViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        String emoji = "";
        if (position == reactions.size()) {
            emoji = String.valueOf(reactionCount);
        } else {
            String reaction = reactions.get(position);
            try {
                emoji = reactionTypes.get(reaction);
            } catch (Exception e) {
                ChatLogger.Companion.getInstance().logE("ReactionListItemAdapter", e);
            }
        }
        holder.tv_emoji.setText(emoji);
        // set Style
        holder.tv_emoji.setTextSize(TypedValue.COMPLEX_UNIT_PX, style.getReactionViewEmojiSize());
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) holder.tv_emoji.getLayoutParams();
        params.leftMargin = style.getReactionViewEmojiMargin();
        params.rightMargin = style.getReactionViewEmojiMargin();
        params.topMargin = style.getReactionViewEmojiMargin();
        params.bottomMargin = style.getReactionViewEmojiMargin();
        holder.tv_emoji.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return reactions.size() + 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tv_emoji;

        public MyViewHolder(View view) {
            super(view);
            tv_emoji = view.findViewById(R.id.tvEmoji);
        }

    }
}
