package io.getstream.chat.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.utils.ResultCallback;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

public class ChannelMoreActionDialog extends Dialog {

    Channel channel;
    ChannelListViewModel viewModel;
    Context context;
    public ChannelMoreActionDialog(@NonNull Context context) {        
        super(context, R.style.DialogTheme);
        this.context = context;
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

        ll_edit.setVisibility(canEditOrDeleteChannel() ? View.VISIBLE : View.GONE);

        ll_hide.setOnClickListener(view-> hideChannel());
        ll_edit.setOnClickListener(view-> editChannel());
    }

    private boolean canEditOrDeleteChannel() {
        return channel.getCreatedByUser() != null && channel.getCreatedByUser().getId().equals(channel.getClient().getUserId());
    }

    private void hideChannel(){
        dismiss();
        viewModel.hideChannel(channel, new ResultCallback<Void, String>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utils.showMessage(context, "Hidden successfully!");
            }

            @Override
            public void onError(String s) {
                Utils.showMessage(context, s);
            }
        });
    }

    private void showChannel(){
        dismiss();
        viewModel.showChannel(channel, new ResultCallback<Void, String>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utils.showMessage(context, "Shown up successfully!");
            }

            @Override
            public void onError(String s) {
                Utils.showMessage(context, s);
            }
        });
    }

    private void editChannel(){
        dismiss();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return false;
    }

}
