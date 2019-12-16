package io.getstream.chat.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.request.HideChannelRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.utils.ResultCallback;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;


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

        setContentView(R.layout.dialog_channel_moreaction);
        setCanceledOnTouchOutside(true);
        LinearLayout ll_hide = findViewById(R.id.ll_hide);
        LinearLayout ll_edit = findViewById(R.id.ll_edit);

        ll_edit.setVisibility(canEditOrDeleteChannel() ? View.VISIBLE : View.GONE);

        ll_hide.setOnClickListener(view -> hideChannel());
        ll_edit.setOnClickListener(view -> createNewChannelDialog());
    }

    private boolean canEditOrDeleteChannel() {
        return channel.getCreatedByUser() != null && channel.getCreatedByUser().getId().equals(channel.getClient().getUserId());
    }

    private void hideChannel() {
        dismiss();
        viewModel.hideChannel(channel, new HideChannelRequest(true), new ResultCallback<Void, String>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utils.showMessage(context, context.getString(R.string.channel_action_hide_alert));
            }

            @Override
            public void onError(String s) {
                Utils.showMessage(context, s);
            }
        });
    }

    private void showChannel() {
        dismiss();
        viewModel.showChannel(channel, new ResultCallback<Void, String>() {
            @Override
            public void onSuccess(Void aVoid) {
                Utils.showMessage(context, context.getString(R.string.channel_action_show_alert));
            }

            @Override
            public void onError(String s) {
                Utils.showMessage(context, s);
            }
        });
    }

    private BottomSheetDialog editChannelDialog;

    private void createNewChannelDialog() {
        dismiss();

        final View eidtChannelLayout = getLayoutInflater().inflate(R.layout.dialog_update_channel, null);
        EditText et_channel_name = eidtChannelLayout.findViewById(R.id.et_channel_name);
        EditText et_update_message = eidtChannelLayout.findViewById(R.id.et_update_message);
        CheckBox checkBox = eidtChannelLayout.findViewById(R.id.checkbox);

        (eidtChannelLayout.findViewById(R.id.button_close)).setOnClickListener(view -> editChannelDialog.dismiss());
        (eidtChannelLayout.findViewById(R.id.button_ok)).setOnClickListener(view -> {
            if (et_channel_name.getText().toString().isEmpty()) {
                et_channel_name.setError(context.getString(R.string.channel_update_name_error));
                return;
            }
            if (checkBox.isChecked() && et_update_message.getText().toString().isEmpty()) {
                et_update_message.setError(context.getString(R.string.channel_update_message_error));
                return;
            }
            editChannel(et_channel_name.getText().toString(), "https://i.imgur.com/1Oe1TDf.jpg", et_update_message.getText().toString());
            editChannelDialog.dismiss();
        });

        checkBox.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) ->
                et_update_message.setVisibility(b ? View.VISIBLE : View.GONE)
        );
        editChannelDialog = new BottomSheetDialog(context, R.style.BottomSheetDialogTheme);
        editChannelDialog.setContentView(eidtChannelLayout);
        editChannelDialog.show();
    }

    private void editChannel(String channelName, String image, String updateMessage) {
        channel.setName(channelName);
        channel.setImage(image);

        Message message = null;
        if (!TextUtils.isEmpty(updateMessage)) {
            message = new Message();
            message.setText(updateMessage);
        }
        channel.update(message, new ChannelCallback() {
            @Override
            public void onSuccess(ChannelResponse response) {
                Utils.showMessage(context, context.getString(R.string.channel_action_update_alert));
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
