package io.getstream.chat.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.utils.ResultCallback;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

import java.util.Random;

public class ChannelMoreActionDialog extends Dialog {

    private static final String TAG = ChannelMoreActionDialog.class.getSimpleName();

    private Channel channel;
    private ChannelListViewModel viewModel;
    private Context context;
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
                Utils.showMessage(context, context.getString(R.string.stream_channel_action_hide_alert));
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
                Utils.showMessage(context, context.getString(R.string.stream_channel_action_show_alert));
            }

            @Override
            public void onError(String s) {
                Utils.showMessage(context, s);
            }
        });
    }

    private void editChannel(){
        channel.setName("Updated " + new Random().nextInt(100));
        channel.setImage("https://i.imgur.com/1Oe1TDf.jpg");
        Message message = new Message();
        message.setText(context.getString(R.string.stream_channel_update_message));
        updateChannel();
        dismiss();
    }

    private void updateChannel(Message message){
        channel.update(message, new ChannelCallback() {
            @Override
            public void onSuccess(ChannelResponse response) {
                Utils.showMessage(context, context.getString(R.string.stream_channel_action_update_alert));
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Utils.showMessage(context, errMsg);
                Log.d(TAG, "Channel Update Error: " + errMsg);
            }
        });
    }

    private void updateChannel(){
        channel.update(new ChannelCallback() {
            @Override
            public void onSuccess(ChannelResponse response) {
                Utils.showMessage(context, context.getString(R.string.stream_channel_action_update_alert));
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Utils.showMessage(context, errMsg);
                Log.d(TAG, "Channel Update Error: " + errMsg);
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return false;
    }

}
