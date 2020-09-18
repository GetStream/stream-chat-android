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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.GiphyAction;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Message;
import top.defaults.drawabletoolbox.DrawableBuilder;

public class AttachmentViewHolderMedia extends BaseAttachmentViewHolder {

    // Attachment
    private PorterShapeImageView iv_media_thumb;
    private TextView tv_media_title, tv_media_play, tv_media_des;
    private ImageView iv_command_logo;

    private ConstraintLayout cl_des, cl_action;
    private TextView tv_action_send, tv_action_shuffle, tv_action_cancel;
    private ProgressBar progressBar;

    private Context context;
    private Message message;
    private Attachment attachment;
    private MessageListItem.MessageItem messageListItem;
    private MessageListViewStyle style;
    private MessageListView.BubbleHelper bubbleHelper;
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.MessageLongClickListener longClickListener;
    @NonNull
    private MessageListView.GiphySendListener giphySendListener;

    public AttachmentViewHolderMedia(
            int resId,
            @NonNull ViewGroup parent,
            @NonNull MessageListView.GiphySendListener giphySendListener
    ) {
        super(resId, parent);
        this.giphySendListener = giphySendListener;

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
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull MessageListItem.MessageItem messageListItem,
                     @NonNull Message message,
                     @NonNull AttachmentListItem attachmentItem,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @Nullable MessageListView.AttachmentClickListener clickListener,
                     @Nullable MessageListView.MessageLongClickListener longClickListener) {
        this.context = context;
        this.messageListItem = messageListItem;
        this.message = message;
        this.attachment = attachmentItem.getAttachment();
        this.style = style;
        this.bubbleHelper = bubbleHelper;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        applyStyle();
        configMediaAttach();
        configAction();
        configClickListeners();
    }


    private void configImageThumbBackground() {
        Drawable background = bubbleHelper.getDrawableForAttachment(message,
                messageListItem.isMine(),
                messageListItem.getPositions(),
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
                enableSendGiphyButtons(false);
                giphySendListener.onGiphySend(message, GiphyAction.SEND);
            });

            tv_action_shuffle.setOnClickListener((View v) -> {
                enableSendGiphyButtons(false);
                giphySendListener.onGiphySend(message, GiphyAction.SHUFFLE);
            });
            tv_action_cancel.setOnClickListener((View v) -> {
                giphySendListener.onGiphySend(message, GiphyAction.CANCEL);
            });
        } else {
            cl_action.setVisibility(View.GONE);
        }
    }

    private void configMediaAttach() {
        final String type = attachment.getType();
        configImageThumbBackground();
        final String imageUrl;
        if (attachment.getThumbUrl() != null && !attachment.getThumbUrl().isEmpty()) {
            imageUrl = attachment.getThumbUrl();
        } else {
            imageUrl = attachment.getImageUrl();
        }
        Glide.with(context)
                .asDrawable()
                .load(imageUrl)
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
            Drawable background = bubbleHelper.getDrawableForAttachmentDescription(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
            cl_des.setBackground(background);
        }
    }

    private void applyStyle() {

        if (messageListItem.isMine()) {
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

    private void configClickListeners(){
        iv_media_thumb.setOnClickListener(view -> {
            if (clickListener != null)
                clickListener.onAttachmentClick(message, attachment);
        });

        iv_media_thumb.setOnLongClickListener(view -> {
            if (longClickListener != null)
                longClickListener.onMessageLongClick(message);
            return true;
        });
    }
}
