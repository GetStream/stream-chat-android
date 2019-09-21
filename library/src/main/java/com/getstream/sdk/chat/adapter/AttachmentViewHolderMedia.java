package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.enums.GiphyAction;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import top.defaults.drawabletoolbox.DrawableBuilder;

public class AttachmentViewHolderMedia extends BaseAttachmentViewHolder {

    final String TAG = AttachmentViewHolder.class.getSimpleName();
    // Attachment
    private PorterShapeImageView iv_media_thumb;
    private TextView tv_media_title, tv_media_play, tv_media_des;
    private ImageView iv_command_logo;

    private ConstraintLayout cl_action;
    private TextView tv_action_send, tv_action_shuffle, tv_action_cancel;
    private MessageListView.GiphySendListener giphySendListener;

    public AttachmentViewHolderMedia(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
        tv_media_title = itemView.findViewById(R.id.tv_media_title);
        tv_media_play = itemView.findViewById(R.id.tv_media_play);
        tv_media_des = itemView.findViewById(R.id.tv_media_des);
        iv_command_logo = itemView.findViewById(R.id.iv_command_logo);
        // Giphy
        cl_action = itemView.findViewById(R.id.cl_action);
        tv_action_send = itemView.findViewById(R.id.tv_action_send);
        tv_action_shuffle = itemView.findViewById(R.id.tv_action_shuffle);
        tv_action_cancel = itemView.findViewById(R.id.tv_action_cancel);
    }

    @Override
    public void bind(Context context,
                     MessageListItem messageListItem,
                     Attachment attachment,
                     MessageListViewStyle style,
                     MessageListView.AttachmentClickListener clickListener,
                     MessageListView.MessageLongClickListener longClickListener) {

        super.bind(context, messageListItem, attachment, style, clickListener, longClickListener);
        applyStyle();
        configMediaAttach();
        configAction();
    }


    private void configImageThumbBackground(Attachment attachment) {
        Drawable background = getBubbleHelper().getDrawableForAttachment(getMessage(),
                getMessageListItem().isMine(),
                getMessageListItem().getPositions(),
                attachment);
        iv_media_thumb.setShape(getContext(), background);
//        iv_media_thumb.setBackgroundDrawable(background);
    }

    private void configAction() {

        if (getMessage().getType().equals(ModelType.message_ephemeral)
                && getMessage().getCommand() != null
                && getMessage().getCommand().equals(ModelType.attach_giphy)) {
            // set Background
            tv_action_send.setBackground(new DrawableBuilder()
                    .rectangle()
                    .rounded()
                    .strokeColor(Color.WHITE)
                    .strokeWidth(Utils.dpToPx(2))
                    .solidColor(getContext().getResources().getColor(R.color.stream_input_message_send_button))
                    .solidColorPressed(Color.LTGRAY)
                    .build());
            tv_action_shuffle.setBackground(new DrawableBuilder()
                    .rectangle()
                    .rounded()
                    .strokeColor(getContext().getResources().getColor(R.color.stream_message_stroke))
                    .strokeWidth(Utils.dpToPx(2))
                    .solidColor(Color.WHITE)
                    .solidColorPressed(Color.LTGRAY)
                    .build());
            tv_action_cancel.setBackground(new DrawableBuilder()
                    .rectangle()
                    .rounded()
                    .strokeColor(getContext().getResources().getColor(R.color.stream_message_stroke))
                    .strokeWidth(Utils.dpToPx(2))
                    .solidColor(Color.WHITE)
                    .solidColorPressed(Color.LTGRAY)
                    .build());

            cl_action.setVisibility(View.VISIBLE);

            tv_action_send.setOnClickListener((View v) -> {
                if (giphySendListener != null)
                    giphySendListener.onGiphySend(getMessage(), GiphyAction.SEND);
            });

            tv_action_shuffle.setOnClickListener((View v) -> {
                if (giphySendListener != null)
                    giphySendListener.onGiphySend(getMessage(), GiphyAction.SHUFFLE);
            });
            tv_action_cancel.setOnClickListener((View v) -> {
                if (giphySendListener != null)
                    giphySendListener.onGiphySend(getMessage(), GiphyAction.CANCEL);
            });
        } else {
            cl_action.setVisibility(View.GONE);
        }
    }

    private void configMediaAttach() {
        Attachment attachment = getAttachment();

        final String type = attachment.getType();

        String attachUrl = attachment.getImageURL();
        if (attachment.getType().equals(ModelType.attach_image)) {
            attachUrl = attachment.getImageURL();
        } else if (attachment.getType().equals(ModelType.attach_giphy)) {
            attachUrl = attachment.getThumbURL();
        } else if (attachment.getType().equals(ModelType.attach_video)) {
            attachUrl = attachment.getThumbURL();
        } else {
            if (attachUrl == null) attachUrl = getAttachment().getImage();
        }

        configImageThumbBackground(getAttachment());

        // Set Click Listener
//        iv_media_thumb.setOnClickListener(this);
//        iv_media_thumb.setOnLongClickListener(this);
        if (!TextUtils.isEmpty(attachUrl) && !attachUrl.contains("https:"))
            attachUrl = "https:" + attachUrl;

        Glide.with(getContext())
                .load(attachUrl)
                .placeholder(R.drawable.stream_placeholder)
                .into(iv_media_thumb);

        if (!getMessage().getType().equals(ModelType.message_ephemeral))
            tv_media_title.setText(getAttachment().getTitle());
        tv_media_des.setText(getAttachment().getText());


        if (attachment.getType().equals(ModelType.attach_giphy))
            iv_command_logo.setVisibility(View.VISIBLE);
        else
            iv_command_logo.setVisibility(View.GONE);

        if (TextUtils.isEmpty(getAttachment().getText()))
            tv_media_des.setVisibility(View.GONE);
        else
            tv_media_des.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(getAttachment().getTitle()))
            tv_media_title.setVisibility(View.GONE);
        else
            tv_media_title.setVisibility(View.VISIBLE);

        if (type.equals(ModelType.attach_video))
            tv_media_play.setVisibility(View.VISIBLE);
        else
            tv_media_play.setVisibility(View.GONE);

    }

    private void applyStyle() {
        tv_media_title.setTextSize(TypedValue.COMPLEX_UNIT_PX, getStyle().getAttachmentTitleTextSize());
        tv_media_title.setTextColor(getStyle().getAttachmentTitleTextColor());
        tv_media_title.setTypeface(Typeface.DEFAULT_BOLD, getStyle().getAttachmentTitleTextStyle());

        tv_media_des.setTextSize(TypedValue.COMPLEX_UNIT_PX, getStyle().getAttachmentDescriptionTextSize());
        tv_media_des.setTextColor(getStyle().getAttachmentDescriptionTextColor());
        tv_media_des.setTypeface(Typeface.DEFAULT_BOLD, getStyle().getAttachmentDescriptionTextStyle());
    }

    public void setGiphySendListener(MessageListView.GiphySendListener giphySendListener) {
        this.giphySendListener = giphySendListener;
    }
}
