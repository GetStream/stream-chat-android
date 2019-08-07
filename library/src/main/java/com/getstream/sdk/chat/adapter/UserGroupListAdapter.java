package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ListItemGroupUserBinding;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.Utils;

import java.util.List;

public class UserGroupListAdapter extends RecyclerView.Adapter<UserGroupListAdapter.MyViewHolder> {

    private final String TAG = UserGroupListAdapter.class.getSimpleName();


    private Context context;
    private List<User> users;
    private View.OnClickListener clickListener;

    public UserGroupListAdapter(Context context, List<User> users, View.OnClickListener clickListener) {
        this.context = context;
        this.users = users;
        this.clickListener = clickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        // create a new view
        LayoutInflater layoutInflater =
                LayoutInflater.from(parent.getContext());
        ListItemGroupUserBinding itemBinding =
                ListItemGroupUserBinding.inflate(layoutInflater, parent, false);
        return new MyViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        holder.bind(users.get(position), this.clickListener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private final ListItemGroupUserBinding binding;

        public MyViewHolder(ListItemGroupUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user, final View.OnClickListener clickListener) {
            TextView tv_initials = binding.viewUserAvator.findViewById(R.id.tv_initials);
            ImageView cv_avatar = binding.viewUserAvator.findViewById(R.id.cv_avatar);
            binding.tvName.setText(user.getName());
            tv_initials.setText(user.getUserInitials());
            Utils.circleImageLoad(cv_avatar, user.getImage());
            Utils.circleImageLoad(cv_avatar, user.getImage());

            binding.tvClose.setOnClickListener((View v) -> {
                v.setTag(user);
                clickListener.onClick(v);
            });
            binding.executePendingBindings();
        }
    }
}
