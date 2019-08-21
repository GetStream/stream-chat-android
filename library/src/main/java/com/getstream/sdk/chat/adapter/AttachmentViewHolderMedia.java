package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;


public class AttachmentViewHolderMedia extends BaseAttachmentViewHolder {

    private Context context;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;

    // Attachment
    private PorterShapeImageView iv_media_thumb;
    private ImageView iv_media_more;
    private TextView tv_more;
    private TextView tv_media_title, tv_media_play, tv_media_des;
    private ConstraintLayout cl_video;
    // Action
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.MessageLongClickListener longClickListener;
    private MessageListItem messageListItem;

    final String TAG = AttachmentViewHolder.class.getSimpleName();


    public AttachmentViewHolderMedia(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
        iv_media_more = itemView.findViewById(R.id.iv_media_more);
        tv_more = itemView.findViewById(R.id.tv_more);
        tv_media_title = itemView.findViewById(R.id.tv_media_title);
        tv_media_play = itemView.findViewById(R.id.tv_media_play);
        tv_media_des = itemView.findViewById(R.id.tv_media_des);
        cl_video = itemView.findViewById(R.id.cl_video);
    }

    @Override
    public void bind(Context context,
                     MessageListItem messageListItem,
                     Attachment attachment,
                     MessageListView.AttachmentClickListener clickListener,
                     MessageListView.MessageLongClickListener longClickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.messageListItem = messageListItem;
        this.message = messageListItem.getMessage();
        this.attachment = attachment;

        configMediaAttach();
    }


    private void configImageThumbBackground(Attachment attachment) {
        Drawable background = getBubbleHelper().getDrawableForAttachment(messageListItem.getMessage(),
                messageListItem.isMine(),
                messageListItem.getPositions(),
                attachment);
        iv_media_thumb.setShape(context, background);
    }

    private void configMediaAttach() {
        List<Attachment> attachments = new ArrayList<>();
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType() == null) continue;
            if (!attachment.getType().equals(ModelType.attach_file)) {
                attachments.add(attachment);
            }
        }

        final String type = attachments.get(0).getType();

        String attachUrl = attachments.get(0).getImageURL();
        if (attachments.get(0).getType().equals(ModelType.attach_image)) {
            attachUrl = attachments.get(0).getImageURL();
        } else if (attachments.get(0).getType().equals(ModelType.attach_giphy)) {
            attachUrl = attachments.get(0).getThumbURL();
        } else if (attachments.get(0).getType().equals(ModelType.attach_video)) {
            attachUrl = attachments.get(0).getThumbURL();
        } else {
            if (attachUrl == null) attachUrl = attachments.get(0).getImage();
        }
        if (TextUtils.isEmpty(attachUrl)) {
            cl_video.setVisibility(View.GONE);
            return;
        }
        configImageThumbBackground(attachments.get(0));
        // More
        if (attachments.size() > 1) {
            iv_media_more.setVisibility(View.VISIBLE);
            tv_more.setText(attachments.size() - 1 + " more");
        } else {
            iv_media_more.setVisibility(View.GONE);
        }
        tv_more.setVisibility(iv_media_more.getVisibility());


        // Set Click Listener
        iv_media_thumb.setOnClickListener(view -> {
            this.triggerClick(message, attachment);
        });
        iv_media_thumb.setOnLongClickListener(view -> {
            if (longClickListener != null)
                longClickListener.onMessageLongClick(message);
            return true;
        });
        if (!attachUrl.contains("https:"))
            attachUrl = "https:" + attachUrl;
        Glide.with(context)
                .load(attachUrl)
                .into(iv_media_thumb);
        if (!message.getType().equals(ModelType.message_ephemeral))
            tv_media_title.setText(attachments.get(0).getTitle());
        tv_media_des.setText(attachments.get(0).getText());

        if (StringUtility.isNullOrEmpty(attachments.get(0).getText()))
            tv_media_des.setVisibility(View.GONE);
        else
            tv_media_des.setVisibility(View.VISIBLE);

        if (StringUtility.isNullOrEmpty(attachments.get(0).getTitle()))
            tv_media_title.setVisibility(View.GONE);
        else
            tv_media_title.setVisibility(View.VISIBLE);

        if (type.equals(ModelType.attach_video))
            tv_media_play.setVisibility(View.VISIBLE);
        else
            tv_media_play.setVisibility(View.GONE);
    }

    private void triggerClick(Message message, Attachment attachment) {
        if (this.clickListener != null) {
            this.clickListener.onAttachmentClick(message, attachment);
        }
    }

    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }
}
