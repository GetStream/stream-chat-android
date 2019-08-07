package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ListItemUserBinding;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.Utils;

import java.util.List;

public class UserListItemAdapter extends BaseAdapter {

    private final String TAG = UserListItemAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private Context context;
    private List<User> users;
    public List<User> selectUsers;
    public boolean groupChatMode = false;
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
        final ListItemUserBinding binding;
        ListItemUserBinding binding1;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_user, null);
            binding1 = DataBindingUtil.bind(convertView);
            convertView.setTag(binding1);
        } else {
            try {
                binding1 = (ListItemUserBinding) convertView.getTag();
            } catch (Exception e) {
                convertView = layoutInflater.inflate(R.layout.list_item_user, null);
                binding1 = DataBindingUtil.bind(convertView);
            }
        }

        binding = binding1;
        User user = users.get(position);

        configUIs(binding, user);
        return binding.getRoot();
    }

    private void configUIs(ListItemUserBinding binding, User user) {
        TextView tv_initials = binding.viewUserAvator.findViewById(R.id.tv_initials);
        ImageView cv_avatar = binding.viewUserAvator.findViewById(R.id.cv_avatar);
        if (StringUtility.isValidImageUrl(user.getImage())) {
            Utils.circleImageLoad(binding.viewUserAvator.findViewById(R.id.cv_avatar), user.getImage());
            cv_avatar.setVisibility(View.VISIBLE);
            tv_initials.setVisibility(View.INVISIBLE);
        } else {
            tv_initials.setText(user.getUserInitials());
            tv_initials.setVisibility(View.VISIBLE);
            cv_avatar.setVisibility(View.INVISIBLE);
        }
        binding.tvName.setText(user.getName());

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
