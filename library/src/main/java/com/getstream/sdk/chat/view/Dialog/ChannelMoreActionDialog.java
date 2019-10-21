package com.getstream.sdk.chat.view.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

public class ChannelMoreActionDialog extends Dialog {

    Channel channel;
    ChannelListViewModel viewModel;

    public ChannelMoreActionDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
        Utils.hideSoftKeyboard((Activity) context);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
    }

    public ChannelMoreActionDialog setChannelListViewModel(ChannelListViewModel viewModel) {
        this.viewModel = viewModel;
        init();
        return this;
    }


    public ChannelMoreActionDialog setChannel(Channel channel) {
        this.channel = channel;
        init();
        return this;
    }


    public void init() {
        if (viewModel == null || channel == null)
            return;

        setContentView(R.layout.stream_dialog_channel_moreaction);
        setCanceledOnTouchOutside(true);
        LinearLayout ll_hide = findViewById(R.id.ll_hide);
        LinearLayout ll_edit = findViewById(R.id.ll_edit);
        LinearLayout ll_delete = findViewById(R.id.ll_delete);


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return false;
    }
}
