package com.getstream.sdk.chat.view.Dialog;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.interfaces.FlagCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.FlagResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.MessageListViewStyle;
import com.getstream.sdk.chat.viewmodel.ChannelViewModel;

import top.defaults.drawabletoolbox.DrawableBuilder;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MoreActionDialog extends Dialog {

    Message message;
    ChannelViewModel viewModel;
    MessageListViewStyle style;

    public MoreActionDialog(@NonNull Context context) {
        super(context, R.style.DialogTheme);
    }

    public MoreActionDialog setChannelViewModel(ChannelViewModel viewModel) {
        this.viewModel = viewModel;
        init();
        return this;
    }


    public MoreActionDialog setMessage(Message message) {
        this.message = message;
        init();
        return this;
    }

    public MoreActionDialog setStyle(MessageListViewStyle style) {
        this.style = style;
        init();
        return this;
    }



    public void init() {
        if (viewModel == null || message == null || style == null)
            return;

        setContentView(com.getstream.sdk.chat.R.layout.stream_dialog_moreaction);

        RelativeLayout rl_wrap = findViewById(R.id.rl_wrap);
        LinearLayout ll_thread = findViewById(R.id.ll_thread);
        LinearLayout ll_copy = findViewById(R.id.ll_copy);
        LinearLayout ll_flag = findViewById(R.id.ll_flag);
        LinearLayout ll_edit = findViewById(R.id.ll_edit);
        LinearLayout ll_delete = findViewById(R.id.ll_delete);

        rl_wrap.setBackground(new DrawableBuilder()
                .rectangle()
                .solidColor(Color.BLACK)
                .cornerRadii(Utils.dpToPx(25), Utils.dpToPx(25), 0, 0)
                .build());

        if (!message.getUserId().equals(StreamChat.getInstance(getContext()).getUserId())){
            ll_edit.setVisibility(View.GONE);
            ll_delete.setVisibility(View.GONE);
            ll_flag.setOnClickListener(view -> {
                viewModel.getChannel().flagMessage(message.getId(), new FlagCallback() {
                    @Override
                    public void onSuccess(FlagResponse response) {
                        Utils.showMessage(getContext(), "Message has been succesfully flagged");
                        dismiss();
                    }

                    @Override
                    public void onError(String errMsg, int errCode) {
                        Utils.showMessage(getContext(), errMsg);
                        dismiss();
                    }
                });

            });
        }else {
            ll_flag.setVisibility(View.GONE);

            ll_edit.setOnClickListener(view -> {
                viewModel.setEditMessage(message);
                dismiss();
            });
            ll_delete.setOnClickListener(view -> {
                viewModel.getChannel().deleteMessage(message,
                        new MessageCallback() {
                            @Override
                            public void onSuccess(MessageResponse response) {
                                Utils.showMessage(getContext(), "Deleted Successfully");
                                dismiss();
                            }

                            @Override
                            public void onError(String errMsg, int errCode) {
                                Utils.showMessage(getContext(), errMsg);
                                dismiss();
                            }
                        });
            });
        }


        RecyclerView rv_reaction = findViewById(com.getstream.sdk.chat.R.id.rv_reaction);
        RecyclerView.LayoutManager mLayoutManager;
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rv_reaction.setLayoutManager(mLayoutManager);
        ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(viewModel.getChannel(),
                message,
                true,
                style,
                (View v) -> dismiss());
        rv_reaction.setAdapter(reactionAdapter);

        ll_thread.setOnClickListener((View v) -> {

            dismiss();
        });
        ll_copy.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", message.getText());
            clipboard.setPrimaryClip(clip);
            dismiss();
        });
    }
}
