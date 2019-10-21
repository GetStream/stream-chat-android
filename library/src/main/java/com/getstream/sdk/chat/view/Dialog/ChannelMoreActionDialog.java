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
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
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

        ll_edit.setVisibility(canEditOrDeleteChannel() ? View.VISIBLE : View.GONE);
        ll_delete.setVisibility(canEditOrDeleteChannel() ? View.VISIBLE : View.GONE);

        ll_hide.setOnClickListener(view-> moreAction(MoreActionType.HIDE));
        ll_edit.setOnClickListener(view-> moreAction(MoreActionType.EDIT));
        ll_delete.setOnClickListener(view-> moreAction(MoreActionType.DELET));
    }

    private boolean canEditOrDeleteChannel() {
        return channel.getCreatedByUser().getId().equals(channel.getClient().getUserId());
    }

    private void hideChannel(){
        channel.hide(new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                Utils.showMessage(getContext(), "Hidden successfully!");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Utils.showMessage(getContext(), errMsg);
            }
        });
    }

    private void showChannel(){
        channel.show(new CompletableCallback() {
            @Override
            public void onSuccess(CompletableResponse response) {
                Utils.showMessage(getContext(), "Shown successfully!");
            }

            @Override
            public void onError(String errMsg, int errCode) {
                Utils.showMessage(getContext(), errMsg);
            }
        });
    }

    private void editChannel(){

    }

    private void deleteChannel(){
        final AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setTitle("Do you want to delete this channel?")
                .setMessage("Deleting this channel will permanently remove your messages!")
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        alertDialog.setOnShowListener(dialog -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                channel.delete(new ChannelCallback() {
                    @Override
                    public void onSuccess(ChannelResponse response) {
                        Utils.showMessage(getContext(), "Deleted successfully!");
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        Utils.showMessage(getContext(), errMsg);
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
