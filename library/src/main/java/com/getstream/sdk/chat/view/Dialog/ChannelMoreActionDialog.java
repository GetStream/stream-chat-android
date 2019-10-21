package com.getstream.sdk.chat.view.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

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
        LinearLayout ll_delete = findViewById(R.id.ll_delete);

        ll_edit.setVisibility(canEditOrDeleteChannel() ? View.VISIBLE : View.GONE);
        ll_delete.setVisibility(canEditOrDeleteChannel() ? View.VISIBLE : View.GONE);

        ll_hide.setOnClickListener(view-> moreAction(MoreActionType.HIDE));
        ll_edit.setOnClickListener(view-> moreAction(MoreActionType.EDIT));
        ll_delete.setOnClickListener(view-> moreAction(MoreActionType.DELET));
    }

    private boolean canEditOrDeleteChannel() {
        return channel.getCreatedByUser() != null && channel.getCreatedByUser().getId().equals(channel.getClient().getUserId());
    }

    private void hideChannel(){
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

    }

    private void deleteChannel(){
        final AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle("Do you want to delete this channel?")
                .setMessage("Deleting this channel will permanently remove your messages!")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                viewModel.deleteChannel(channel, new ResultCallback<Void, String>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Utils.showMessage(context, "Deleted successfully!");
                    }

                    @Override
                    public void onError(String s) {
                        Utils.showMessage(context, s);
                    }
                });
                alertDialog.dismiss();
            });
        });
        alertDialog.show();
    }

    private void moreAction(MoreActionType type) {
        dismiss();
        switch (type) {
            case HIDE:
                hideChannel();
                break;
            case SHOW:
                showChannel();
                break;
            case EDIT:
                editChannel();
                break;
            case DELET:
                deleteChannel();
                break;
            default:
                break;
        }
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return false;
    }

    public enum MoreActionType {
        HIDE,
        SHOW,
        EDIT,
        DELET
    }

}
