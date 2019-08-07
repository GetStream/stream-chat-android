package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ListItemMentionBinding;
import com.getstream.sdk.chat.rest.User;

import java.util.List;

public class MentionListItemAdapter extends BaseAdapter {

    private final String TAG = MentionListItemAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private Context context;
    private List<Object> users;

    public MentionListItemAdapter(Context context, List users) {
        this.context = context;
        this.layoutInflater = LayoutInflater.from(context);
        this.users = users;

    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ListItemMentionBinding binding;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_command, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (ListItemMentionBinding) convertView.getTag();
        }

        User user = (User) users.get(position);
        binding.setUser(user);

        return binding.getRoot();
    }
}
