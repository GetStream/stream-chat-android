package io.getstream.chat.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.utils.ResultCallback;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;

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
        ll_edit.setOnClickListener(view-> createNewChannelDialog());
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

    private void createNewChannelDialog() {
        final EditText inputName = new EditText(context);
        inputName.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        inputName.setHint(context.getString(R.string.stream_channel_update_hint));
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.stream_channel_update_Title))
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.setView(inputName);
        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                String channelName = inputName.getText().toString();
                if (TextUtils.isEmpty(channelName)) {
                    inputName.setError(context.getString(R.string.stream_channel_update_name_error));
                    return;
                }
                editChannel(channelName);
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
        dismiss();
    }

    private void editChannel(String channelName){
        channel.setName(channelName);
        channel.setImage("https://i.imgur.com/1Oe1TDf.jpg");
        Message message = new Message();
        message.setText(context.getString(R.string.stream_channel_update_message));
//        channel.update(message, new ChannelCallback() {
//            @Override
//            public void onSuccess(ChannelResponse response) {
//                Utils.showMessage(context, context.getString(R.string.stream_channel_action_update_alert));
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                Utils.showMessage(context, errMsg);
//                Log.d(TAG, "Channel Update Error: " + errMsg);
//            }
//        });
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
