package io.getstream.chat.example;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.viewmodel.ChannelListViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.HashMap;

import androidx.annotation.NonNull;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.Result;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;


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
        User createdByUser = channel.getCreatedBy();
        User currentUser = Chat.getInstance().getCurrentUser().getValue();

        if (createdByUser != null && currentUser != null) {
            return createdByUser.getId().equals(currentUser.getId());
        } else {
            return false;
        }
    }

    private void hideChannel() {
        dismiss();

        String id = channel.getId();
        String type = channel.getType();

        viewModel.hideChannel(type, id, false).enqueue(
                new Function1<Result<Unit>, Unit>() {
                    @Override
                    public Unit invoke(Result<Unit> unitResult) {

                        if (unitResult.isSuccess()) {
                            Utils.showMessage(context, context.getString(R.string.channel_action_hide_alert));
                        } else {
                            Utils.showMessage(context, unitResult.error().getMessage());
                        }

                        return null;
                    }
                }
        );
    }

    private void showChannel() {
        dismiss();
        String type = channel.getType();
        String id = channel.getId();

        viewModel.showChannel(type, id).enqueue(new Function1<Result<Unit>, Unit>() {
            @Override
            public Unit invoke(Result<Unit> unitResult) {

                if (unitResult.isSuccess()) {
                    Utils.showMessage(context, context.getString(R.string.channel_action_show_alert));
                } else {
                    Utils.showMessage(context, unitResult.error().getMessage());
                }

                return null;
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

        Message message = null;
        if (!TextUtils.isEmpty(updateMessage)) {
            message = new Message();
            message.setText(updateMessage);
        }

        String type = channel.getType();
        String id = channel.getId();

        HashMap<String, Object> data = new HashMap<>();
        data.put("image", image);
        data.put("name", channelName);

        Chat.getInstance().getClient().updateChannel(type, id, message, data).enqueue(
                channelResult -> {

                    if (channelResult.isSuccess()) {
                        Utils.showMessage(context, context.getString(R.string.channel_action_update_alert));
                    } else {
                        Utils.showMessage(context, channelResult.error().getMessage());
                    }

                    return null;
                }
        );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return false;
    }
}
