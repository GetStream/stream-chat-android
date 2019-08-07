package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.ReactionEmoji;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReactionListItemAdapter extends RecyclerView.Adapter<ReactionListItemAdapter.MyViewHolder>{

    private final String TAG = ReactionListItemAdapter.class.getSimpleName();

    private Context context;
    private List<String>reactions = new ArrayList<>();
    private int reactionCount;
    public ReactionListItemAdapter(Context context, Map<String,Integer> reactionCountMap) {
        this.context = context;
        Set keys = reactionCountMap.keySet();
        reactionCount = 0;
        for(Object key: keys){
            this.reactions.add(key.toString());
            reactionCount += reactionCountMap.get(key);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_reaction, parent, false);

        MyViewHolder vh = new MyViewHolder(itemView);
        return vh;
    }
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        String emoji;
        if (position == reactions.size()){
            emoji = String.valueOf(reactionCount);
        }else {
            String reaction = reactions.get(position);
            emoji = ReactionEmoji.valueOf(reaction).get();
        }
        holder.tv_emoji.setText(emoji);
    }

    @Override
    public int getItemCount() {
        return reactions.size() + 1;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tv_emoji;

        public MyViewHolder(View view) {
            super(view);
            tv_emoji = view.findViewById(R.id.tv_emoji);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

        }
    }
}
