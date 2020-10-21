package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.databinding.ViewDataBinding;
import androidx.viewbinding.ViewBinding;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.databinding.StreamItemCommandBinding;
import com.getstream.sdk.chat.databinding.StreamItemMentionBinding;
import com.getstream.sdk.chat.view.BaseStyle;
import com.getstream.sdk.chat.view.MessageListViewStyle;
import com.getstream.sdk.chat.view.messageinput.MessageInputStyle;

import java.util.List;

import io.getstream.chat.android.client.models.Command;
import io.getstream.chat.android.client.models.User;

public class CommandMentionListItemAdapter<STYLE extends BaseStyle> extends BaseAdapter {

    private LayoutInflater layoutInflater;
    private List<Object> commands;
    private boolean isCommand;
    private STYLE style;

    public CommandMentionListItemAdapter(Context context, List<Object> commands, STYLE style, boolean isCommand) {
        this.layoutInflater = LayoutInflater.from(context);
        this.commands = commands;
        this.style = style;
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
        ViewBinding binding;
        if (convertView == null) {
            if (isCommand) {
                convertView = layoutInflater.inflate(R.layout.stream_item_command, null);
                binding = StreamItemCommandBinding.bind(convertView);
            } else {
                convertView = layoutInflater.inflate(R.layout.stream_item_mention, null);
                binding = StreamItemMentionBinding.bind(convertView);
            }
            convertView.setTag(binding);
        } else {
            binding = (ViewDataBinding) convertView.getTag();
        }

        if (isCommand) {
            configCommands((StreamItemCommandBinding) binding, position);
        } else {
            configMentions((StreamItemMentionBinding) binding, position);
        }
        return binding.getRoot();
    }

    public void configCommands(StreamItemCommandBinding binding, int position) {
        Command command = (Command) commands.get(position);
        binding.tvCommand.setText(command.getName());
        binding.tvArg.setText(command.getArgs());
        binding.tvDes.setText(command.getDescription());
        if (style instanceof MessageInputStyle) {
            MessageInputStyle style_ = (MessageInputStyle) style;

            style_.inputBackgroundText.apply(binding.tvCommand);
            style_.inputBackgroundText.apply(binding.tvDes);
            style_.inputBackgroundText.apply(binding.tvArg);
        }
    }

    public void configMentions(StreamItemMentionBinding binding, int position) {
        User user = (User) commands.get(position);
        binding.avatar.setUser(user, style);
        if (style instanceof MessageInputStyle) {
            MessageInputStyle style_ = (MessageInputStyle) style;
            style_.inputBackgroundText.apply(binding.tvUsername);
            style_.inputBackgroundText.apply(binding.tvYou);
        } else if (style instanceof MessageListViewStyle) {

        }
    }

    public void setCommands(List<Object> commands) {
        this.commands = commands;
    }

    public void setCommand(boolean command) {
        isCommand = command;
    }
}
