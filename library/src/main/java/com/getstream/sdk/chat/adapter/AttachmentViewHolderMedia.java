package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.GiphyAction;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;

import androidx.constraintlayout.widget.ConstraintLayout;
import top.defaults.drawabletoolbox.DrawableBuilder;

public class AttachmentViewHolderMedia extends BaseAttachmentViewHolder {

    final String TAG = AttachmentViewHolder.class.getSimpleName();
    // Attachment
    private PorterShapeImageView iv_media_thumb;
    private TextView tv_media_title, tv_media_play, tv_media_des;
    private ImageView iv_command_logo;

    private ConstraintLayout cl_des, cl_action;
    private TextView tv_action_send, tv_action_shuffle, tv_action_cancel;
    private ProgressBar progressBar;

    private MessageListView.GiphySendListener giphySendListener;
    private Client client;
    private MessageCallback sendGiphyMessageCallback = new MessageCallback() {
        @Override
        public void onSuccess(MessageResponse response) {
            enableSendGiphyButtons(true);
        }

        @Override
        public void onError(String errMsg, int errCode) {
            enableSendGiphyButtons(true);
            Utils.showMessage(context, errMsg);
        }
    };


    public AttachmentViewHolderMedia(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
        tv_media_title = itemView.findViewById(R.id.tv_media_title);
        tv_media_play = itemView.findViewById(R.id.tv_media_play);
        tv_media_des = itemView.findViewById(R.id.tv_media_des);
        iv_command_logo = itemView.findViewById(R.id.iv_command_logo);
        cl_des = itemView.findViewById(R.id.cl_des);
        progressBar = itemView.findViewById(R.id.progressBar);
        // Giphy
        cl_action = itemView.findViewById(R.id.cl_action);
        tv_action_send = itemView.findViewById(R.id.tv_action_send);
        tv_action_shuffle = itemView.findViewById(R.id.tv_action_shuffle);
        tv_action_cancel = itemView.findViewById(R.id.tv_action_cancel);
        client = StreamChat.getInstance(context);
    }

    @Override
    public void bind(Context context,
                     MessageListItem messageListItem,
                     Attachment attachment,
                     MessageListView.AttachmentClickListener clickListener,
                     MessageListView.MessageLongClickListener longClickListener) {

        super.bind(context, messageListItem, attachment, clickListener, longClickListener);
        applyStyle();
        configMediaAttach();
        configAction();
    }


    private void configImageThumbBackground() {
        Drawable background = getBubbleHelper().getDrawableForAttachment(message,
                getMessageListItem().isMine(),
                getMessageListItem().getPositions(),
                attachment);
        iv_media_thumb.setShape(context, background);
    }

    private void configAction() {
        if (message.getType().equals(ModelType.message_ephemeral)
                && message.getCommand() != null
                && message.getCommand().equals(ModelType.attach_giphy)) {
            // set Background
            tv_action_send.setBackground(new DrawableBuilder()
                    .rectangle()
                    .rounded()
                    .strokeColor(Color.WHITE)
                    .strokeWidth(Utils.dpToPx(2))
                    .solidColor(context.getResources().getColor(R.color.stream_input_message_send_button))
                    .solidColorPressed(Color.LTGRAY)
                    .build());
            tv_action_shuffle.setBackground(new DrawableBuilder()
                    .rectangle()
                    .rounded()
                    .strokeColor(context.getResources().getColor(R.color.stream_message_stroke))
                    .strokeWidth(Utils.dpToPx(2))
                    .solidColor(Color.WHITE)
                    .solidColorPressed(Color.LTGRAY)
                    .build());
            tv_action_cancel.setBackground(new DrawableBuilder()
                    .rectangle()
                    .rounded()
                    .strokeColor(context.getResources().getColor(R.color.stream_message_stroke))
                    .strokeWidth(Utils.dpToPx(2))
                    .solidColor(Color.WHITE)
                    .solidColorPressed(Color.LTGRAY)
                    .build());

            cl_action.setVisibility(View.VISIBLE);

            tv_action_send.setOnClickListener(view -> {
                if (giphySendListener != null){
                    enableSendGiphyButtons(false);
                    giphySendListener.onGiphySend(message, GiphyAction.SEND, sendGiphyMessageCallback);
                }
            });

            tv_action_shuffle.setOnClickListener((View v) -> {
                if (giphySendListener != null){
                    enableSendGiphyButtons(false);
                    giphySendListener.onGiphySend(message, GiphyAction.SHUFFLE, sendGiphyMessageCallback);
                }
            });
            tv_action_cancel.setOnClickListener((View v) -> {
                if (giphySendListener != null){
                    giphySendListener.onGiphySend(message, GiphyAction.CANCEL, sendGiphyMessageCallback);
                }
            });
        } else {
            cl_action.setVisibility(View.GONE);
        }
    }

    private void configMediaAttach() {
        final String type = attachment.getType();
        configImageThumbBackground();

        Glide.with(context)
                .load(client.getUploadStorage().signGlideUrl(ModelType.getAssetUrl(attachment)))
                .placeholder(R.drawable.stream_placeholder)
                .into(iv_media_thumb);

        if (!message.getType().equals(ModelType.message_ephemeral))
            tv_media_title.setText(attachment.getTitle());
        tv_media_des.setText(attachment.getText());

        tv_media_title.setVisibility(!TextUtils.isEmpty(attachment.getTitle()) ? View.VISIBLE : View.GONE);
        tv_media_des.setVisibility(!TextUtils.isEmpty(attachment.getText()) ? View.VISIBLE : View.GONE);
        tv_media_play.setVisibility(type.equals(ModelType.attach_video) ? View.VISIBLE : View.GONE);
        iv_command_logo.setVisibility(type.equals(ModelType.attach_giphy) ? View.VISIBLE : View.GONE);

        if (tv_media_des.getVisibility() == View.VISIBLE || tv_media_title.getVisibility() == View.VISIBLE) {
            Drawable background = getBubbleHelper().getDrawableForAttachmentDescription(getMessageListItem().getMessage(), getMessageListItem().isMine(), getMessageListItem().getPositions());
            cl_des.setBackground(background);
        }
    }

    private void applyStyle() {

        if (getMessageListItem().isMine()) {
            style.attachmentTitleTextMine.apply(tv_media_title);
            style.attachmentDescriptionTextMine.apply(tv_media_des);
        } else {
            style.attachmentTitleTextTheirs.apply(tv_media_title);
            style.attachmentDescriptionTextTheirs.apply(tv_media_des);
        }
    }

    private void enableSendGiphyButtons(boolean isEnable){
        progressBar.setVisibility(isEnable ? View.GONE : View.VISIBLE);
        tv_action_send.setEnabled(isEnable);
        tv_action_shuffle.setEnabled(isEnable);
        tv_action_cancel.setEnabled(isEnable);
    }

    public void setGiphySendListener(MessageListView.GiphySendListener giphySendListener) {
        this.giphySendListener = giphySendListener;
    }
}
