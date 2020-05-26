package com.getstream.sdk.chat.view.Dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.ReactionDialogAdapter;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Flag;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.utils.Result;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import top.defaults.drawabletoolbox.DrawableBuilder;

import static android.content.Context.CLIPBOARD_SERVICE;

public class MessageMoreActionDialog extends Dialog {
    private Message message;
    private MessageListViewStyle style;
    private Context context;
    private Channel channel;
    private MessageActionDelegate messageActionDelegate;

    public MessageMoreActionDialog(@NonNull Context context,
                                   @NonNull Channel channel,
                                   @NonNull MessageActionDelegate messageActionDelegate) {
        super(context, R.style.DialogTheme);
        this.channel = channel;
        this.context = context;
        this.messageActionDelegate = messageActionDelegate;
        Utils.hideSoftKeyboard((Activity) context);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);
    }

    public MessageMoreActionDialog setMessage(Message message) {
        this.message = message;
        init();
        return this;
    }

    public MessageMoreActionDialog setStyle(MessageListViewStyle style) {
        this.style = style;
        init();
        return this;
    }

    public void init() {
        if (message == null || style == null)
            return;

        setContentView(com.getstream.sdk.chat.R.layout.stream_dialog_message_moreaction);
        setCanceledOnTouchOutside(true);
        RelativeLayout rl_wrap = findViewById(R.id.rl_wrap);
        LinearLayout ll_thread = findViewById(R.id.ll_thread);
        LinearLayout ll_copy = findViewById(R.id.ll_copy);
        LinearLayout ll_flag = findViewById(R.id.ll_flag);
        LinearLayout ll_edit = findViewById(R.id.ll_edit);
        LinearLayout ll_delete = findViewById(R.id.ll_delete);

        ll_thread.setVisibility(canThreadOnMessage() ? View.VISIBLE : View.GONE);
        ll_copy.setVisibility(canCopyonMessage() ? View.VISIBLE : View.GONE);
        User currentUser = ChatClient.instance().getCurrentUser();
        String id = currentUser.getId();
        if (!message.getUser().getId().equals(id)) {
            ll_edit.setVisibility(View.GONE);
            ll_delete.setVisibility(View.GONE);
            ll_flag.setOnClickListener(view -> {
                ChatClient.instance().flag(message.getCid()).enqueue(new Function1<Result<Flag>, Unit>() {
                    @Override
                    public Unit invoke(Result<Flag> flagResponseResult) {

                        if (flagResponseResult.isSuccess()) {
                            Utils.showMessage(context, "Message has been succesfully flagged");
                        } else {
                            ChatError error = flagResponseResult.error();
                            Utils.showMessage(context, error.getMessage());
                        }

                        dismiss();

                        return null;
                    }
                });

            });
        } else {
            ll_flag.setVisibility(View.GONE);

            ll_edit.setOnClickListener(view -> {
                messageActionDelegate.onMessageEdit(message);
//                viewModel.setEdiftMessage(message);
                dismiss();
            });

            ll_delete.setOnClickListener(view -> {
                messageActionDelegate.onMessageDelete(message);
//                ChatClient.instance().deleteMessage(message.getId()).enqueue(new Function1<Result<Message>, Unit>() {
//                    @Override
//                    public Unit invoke(Result<Message> messageResult) {
//                        if (messageResult.isSuccess()) {
//                            Utils.showMessage(context, "Deleted Successfully");
//                            if (TextUtils.isEmpty(message.getParentId()))
//                                viewModel.resetThread();
//                        } else {
//                            ChatError error = messageResult.error();
//                            Utils.showMessage(context, error.getMessage());
//                        }
//                        dismiss();
//                        return null;
//                    }
//                });
                dismiss();
            });
        }

        // set style
        if (canReactOnMessage()) {
            rl_wrap.post(() ->
                    rl_wrap.setBackground(new DrawableBuilder()
                            .rectangle()
                            .solidColor(style.getReactionInputBgColor())
                            .cornerRadii(rl_wrap.getHeight() / 2, rl_wrap.getHeight() / 2, 0, 0)
                            .build())
            );

            RecyclerView rv_reaction = findViewById(com.getstream.sdk.chat.R.id.rv_reaction);
            RecyclerView.LayoutManager mLayoutManager;
            mLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
            rv_reaction.setLayoutManager(mLayoutManager);
            ReactionDialogAdapter reactionAdapter = new ReactionDialogAdapter(
                    message,
                    style,
                    (View v) -> dismiss());
            rv_reaction.setAdapter(reactionAdapter);
        } else {
            rl_wrap.setVisibility(View.GONE);
        }

        ll_thread.setOnClickListener(view -> {
            dismiss();
//            viewModel.setActiveThread(message);
            messageActionDelegate.onStartThread(message);
        });
        ll_copy.setOnClickListener(view -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", message.getText());
            clipboard.setPrimaryClip(clip);
            dismiss();
        });
    }

    private boolean canCopyonMessage() {
        return !(message.getDeletedAt() != null
                //TODO: llc cache
                //|| message.getSyncStatus() == Sync.LOCAL_FAILED
                || message.getType().equals(ModelType.message_error)
                || message.getType().equals(ModelType.message_ephemeral)
                || TextUtils.isEmpty(message.getText()));
    }

    private boolean canThreadOnMessage() {
        return style.isThreadEnabled()
                && channel.getConfig().isRepliesEnabled()
                && message.getParentId() == null;
    }

    private boolean canReactOnMessage() {
        return style.isReactionEnabled() && channel.getConfig().isReactionsEnabled();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        dismiss();
        return false;
    }

    public interface MessageActionDelegate {
        void onMessageDelete(Message message);
        void onStartThread(Message parent);
        void onMessageEdit(Message message);
    }
}
