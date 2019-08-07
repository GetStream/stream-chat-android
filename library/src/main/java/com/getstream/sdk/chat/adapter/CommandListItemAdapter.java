package com.getstream.sdk.chat.adapter;

import android.content.Context;
import androidx.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.ListItemCommandBinding;
import com.getstream.sdk.chat.model.Command;
import com.getstream.sdk.chat.rest.User;

import java.util.List;

public class CommandListItemAdapter extends BaseAdapter {

    private final String TAG = CommandListItemAdapter.class.getSimpleName();
    private LayoutInflater layoutInflater;
    private Context context;
    private List<Object> commands;
    private boolean isCommand;

    public CommandListItemAdapter(Context context, List commands, boolean isCommand) {
        this.context = context;
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
        ListItemCommandBinding binding;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_command, null);
            binding = DataBindingUtil.bind(convertView);
            convertView.setTag(binding);
        } else {
            binding = (ListItemCommandBinding) convertView.getTag();
        }

        binding.setIsCommand(this.isCommand);
        if (isCommand){
            Command command = (Command) commands.get(position);
            binding.setCommand(command);
        }else{
            User user = (User) commands.get(position);
            binding.setUser(user);
        }
        return binding.getRoot();
    }
}
