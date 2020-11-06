package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.viewbinding.ViewBinding;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.StreamItemMentionBinding;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.List;

import io.getstream.chat.android.client.models.User;

public class CommandMentionListItemAdapter<T> extends BaseAdapter {

    private final LayoutInflater layoutInflater;
    private final MessageListViewStyle style;

    private List<T> items;

    public CommandMentionListItemAdapter(Context context, List<T> items, MessageListViewStyle style) {
        this.layoutInflater = LayoutInflater.from(context);
        this.items = items;
        this.style = style;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewBinding binding;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.stream_item_mention, null);
            binding = StreamItemMentionBinding.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (ViewBinding) convertView.getTag();
        }
        configMentions((StreamItemMentionBinding) binding, position);
        return binding.getRoot();
    }

    public void configMentions(StreamItemMentionBinding binding, int position) {
        User user = (User) items.get(position);
        binding.avatar.setUser(user, style.getAvatarStyle());
    }
}
