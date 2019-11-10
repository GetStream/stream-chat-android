package io.getstream.chat.example.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.getstream.sdk.chat.rest.User;

import java.util.List;

import io.getstream.chat.example.R;
import io.getstream.chat.example.databinding.ItemUserBinding;

public class UserListItemAdapter extends BaseAdapter {

    private final String TAG = UserListItemAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private List<Object> users;


    public UserListItemAdapter(Context context, List<Object> users) {
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
        ViewDataBinding binding;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_user, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (ViewDataBinding) convertView.getTag();
        }
        User user = (User) users.get(position);
        ItemUserBinding userBinding = (ItemUserBinding) binding;
        userBinding.setUser(user);

        return binding.getRoot();
    }
}
