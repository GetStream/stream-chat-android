package com.getstream.sdk.chat.view.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.annotation.NonNull;
import com.getstream.sdk.chat.R;

import com.getstream.sdk.chat.adapter.CommandMentionListItemAdapter;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.view.MessageListViewStyle;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.ArrayList;
import java.util.List;


public class ReadUsersDialog extends Dialog {

    List<ChannelUserRead>reads;
    ChannelViewModel viewModel;
    MessageListViewStyle style;

    public ReadUsersDialog(@NonNull Context context) {
        super(context);

        Window window = getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;

        wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        window.setAttributes(wlp);
    }

    public ReadUsersDialog setReads(List<ChannelUserRead> reads) {
        this.reads = reads;
        init();
        return this;
    }

    public ReadUsersDialog setChannelViewModel(ChannelViewModel viewModel) {
        this.viewModel = viewModel;
        init();
        return this;
    }

    public ReadUsersDialog setStyle(MessageListViewStyle style) {
        this.style = style;
        init();
        return this;
    }


    public void init() {
        if (viewModel == null || reads == null || style == null)
            return;
        setContentView(R.layout.stream_dialog_read_users);
        ListView lv_read_user = findViewById(R.id.lv_read_user);
        List<User> users = new ArrayList<>();
        for (ChannelUserRead read : reads){
            users.add(read.getUser());
        }
        CommandMentionListItemAdapter reactionAdapter = new CommandMentionListItemAdapter(getContext(), users, style, false);
        lv_read_user.setAdapter(reactionAdapter);
    }
}
