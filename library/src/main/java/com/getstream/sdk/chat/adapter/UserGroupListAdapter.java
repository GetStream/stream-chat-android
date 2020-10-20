package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.databinding.StreamItemGroupUserBinding;

import java.util.List;

import io.getstream.chat.android.client.models.User;

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
        StreamItemGroupUserBinding itemBinding =
                StreamItemGroupUserBinding.inflate(layoutInflater, parent, false);
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
        private final StreamItemGroupUserBinding binding;

        public MyViewHolder(StreamItemGroupUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user, final View.OnClickListener clickListener) {
            binding.btnClose.setOnClickListener((View v) -> {
                v.setTag(user);
                clickListener.onClick(v);
            });
        }
    }
}
