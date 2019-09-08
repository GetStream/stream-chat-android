package com.getstream.sdk.chat.view.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;

import com.getstream.sdk.chat.adapter.CommandListItemAdapter;
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.view.MessageListViewStyle;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import java.util.List;


public class ReadUsersDialog extends Dialog {

    List<ChannelUserRead>reads;
    ChannelViewModel viewModel;
    MessageListViewStyle style;

    public ReadUsersDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
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


        RecyclerView rv_read_user = findViewById(R.id.rv_read_user);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        rv_read_user.setLayoutManager(mLayoutManager);
        CommandListItemAdapter reactionAdapter = new CommandListItemAdapter(getContext(), reads, false);
//        rv_read_user.setAdapter(reactionAdapter);
    }
}
