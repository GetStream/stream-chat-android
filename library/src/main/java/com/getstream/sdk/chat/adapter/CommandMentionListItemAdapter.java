package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.StreamItemCommandBinding;
import com.getstream.sdk.chat.databinding.StreamItemMentionBinding;
import com.getstream.sdk.chat.model.Command;
import com.getstream.sdk.chat.rest.User;

import java.util.List;

public class CommandMentionListItemAdapter extends BaseAdapter {

    private final String TAG = CommandMentionListItemAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private List<Object> commands;
    private boolean isCommand;

    public CommandMentionListItemAdapter(Context context, List commands, boolean isCommand) {
        this.layoutInflater = LayoutInflater.from(context);
        this.commands = commands;
        this.isCommand = isCommand;
    }

    @Override
    public int getCount() {
        return commands.size();
    }

    @Override
    public Object getItem(int position) {
        return commands.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewDataBinding binding;
        if (convertView == null) {
            convertView = layoutInflater.inflate(isCommand ? R.layout.stream_item_command : R.layout.stream_item_mention, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (ViewDataBinding) convertView.getTag();
        }

        if (isCommand) {
            Command command = (Command) commands.get(position);
            ((StreamItemCommandBinding) binding).setCommand(command);
        } else {
            User user = (User) commands.get(position);
            ((StreamItemMentionBinding) binding).setUser(user);
        }
        return binding.getRoot();
    }
}
