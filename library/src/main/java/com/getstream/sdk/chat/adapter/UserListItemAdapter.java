package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.StreamItemUserBinding;

import java.util.List;

import io.getstream.chat.android.client.models.User;

public class UserListItemAdapter extends BaseAdapter {

    private final String TAG = UserListItemAdapter.class.getSimpleName();
    public List<User> selectUsers;
    public boolean groupChatMode = false;
    private LayoutInflater layoutInflater;
    private Context context;
    private List<User> users;
    private View.OnClickListener checkedChangeListener;

    public UserListItemAdapter(Context context, List users, View.OnClickListener checkedChangeListener) {
        this.context = context;
        this.users = users;
        this.layoutInflater = LayoutInflater.from(context);
        this.checkedChangeListener = checkedChangeListener;
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
        final StreamItemUserBinding binding;
        StreamItemUserBinding binding1;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.stream_item_user, null);
            binding1 = StreamItemUserBinding.bind(convertView);
            convertView.setTag(binding1);
        } else {
            try {
                binding1 = (StreamItemUserBinding) convertView.getTag();
            } catch (Exception e) {
                convertView = layoutInflater.inflate(R.layout.stream_item_user, null);
                binding1 = StreamItemUserBinding.bind(convertView);
            }
        }

        binding = binding1;
        User user = users.get(position);

        configUIs(binding, user);
        return binding.getRoot();
    }

    private void configUIs(StreamItemUserBinding binding, User user) {
        binding.tvName.setText(user.getExtraValue("name", ""));

        if (user.getOnline())
            binding.ivActiveMark.setVisibility(View.VISIBLE);
        else
            binding.ivActiveMark.setVisibility(View.GONE);

        if (selectUsers == null) {
            binding.checkbox.setChecked(false);
        } else {
            binding.checkbox.setChecked(selectUsers.contains(user));
        }

        binding.tvId.setText("id: " + user.getId());
        binding.checkbox.setVisibility(groupChatMode ? View.VISIBLE : View.GONE);
        binding.checkbox.setOnClickListener(
                (View view) -> {
                    view.setTag(user);
                    this.checkedChangeListener.onClick(view);
                }
        );
    }
}
