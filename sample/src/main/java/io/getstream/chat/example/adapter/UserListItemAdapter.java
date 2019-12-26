package io.getstream.chat.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.getstream.sdk.chat.StreamChat;

import java.util.List;

import io.getstream.chat.example.R;
import io.getstream.chat.example.databinding.ItemUserBinding;
import io.getstream.chat.example.utils.UserConfig;

public class UserListItemAdapter extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<UserConfig> users;


    public UserListItemAdapter(Context context, List<UserConfig> users) {
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
        ItemUserBinding binding;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_user, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (ItemUserBinding) convertView.getTag();
        }
        UserConfig user = users.get(position);
        binding.setUser(user);
        return binding.getRoot();
    }
}
