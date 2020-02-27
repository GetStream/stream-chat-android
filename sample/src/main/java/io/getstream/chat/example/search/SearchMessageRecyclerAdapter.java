package io.getstream.chat.example.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import io.getstream.chat.android.client.models.Message;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.LlcMigrationUtils;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.example.R;

public class SearchMessageRecyclerAdapter extends RecyclerView.Adapter<SearchMessageRecyclerAdapter.SearchItemHolder> {

    private ArrayList<MessageResponse> items = new ArrayList<>();
    private OnSearchItemClickListener onItemListener;

    @NonNull
    @Override
    public SearchItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SearchItemHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_search_message, parent, false),
                onItemListener
        );
    }

    @Override
    public void onBindViewHolder(@NonNull SearchItemHolder holder, int position) {
        if (items != null) {
            holder.bind(items.get(position).getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    void setItems(List<MessageResponse> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    void setOnItemClickListener(OnSearchItemClickListener onItemListener) {
        this.onItemListener = onItemListener;
    }

    class SearchItemHolder extends RecyclerView.ViewHolder {
        private Context context;
        private ImageView itemSearchIv;
        private TextView itemSearchTitleTv;
        private TextView itemSearchMessageTv;
        private OnSearchItemClickListener onItemListener;

        SearchItemHolder(@NonNull View itemView, OnSearchItemClickListener onItemListener) {
            super(itemView);

            context = itemView.getContext();
            this.onItemListener = onItemListener;

            itemSearchIv = itemView.findViewById(R.id.itemSearchIv);
            itemSearchTitleTv = itemView.findViewById(R.id.itemSearchTitleTv);
            itemSearchMessageTv = itemView.findViewById(R.id.itemSearchMessageTv);
        }

        void bind(Message item) {
            Glide.with(context)
                    .load(item.getUser().getImage())
                    .into(itemSearchIv);

            String name = LlcMigrationUtils.getName(item.getChannel());

            itemSearchTitleTv.setText(name);
            itemSearchMessageTv.setText(item.getText());
            itemView.setOnClickListener(v -> onItemListener.onItemClicked(item.getChannel().getType(), item.getChannel().getId(), item.getId()));
        }
    }

    interface OnSearchItemClickListener {
        void onItemClicked(String channelType, String channelId, String messageId);
    }
}
