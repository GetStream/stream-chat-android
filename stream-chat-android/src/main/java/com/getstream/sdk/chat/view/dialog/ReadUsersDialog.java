package com.getstream.sdk.chat.view.dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.CommandMentionListItemAdapter;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.client.models.ChannelUserRead;
import io.getstream.chat.android.client.models.User;


public class ReadUsersDialog extends Dialog {

    List<ChannelUserRead> reads;
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

    public ReadUsersDialog setStyle(MessageListViewStyle style) {
        this.style = style;
        init();
        return this;
    }


    public void init() {
        if (reads == null || style == null)
            return;
        setContentView(R.layout.stream_dialog_read_users);
        ListView lv_read_user = findViewById(R.id.lvReadUser);
        List<User> users = new ArrayList<>();
        for (ChannelUserRead read : reads){
            users.add(read.getUser());
        }
        CommandMentionListItemAdapter reactionAdapter = new CommandMentionListItemAdapter(getContext(), users, style);
        lv_read_user.setAdapter(reactionAdapter);
    }
}
