package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.http.HEAD;


public class AttachmentViewHolderMedia extends BaseAttachmentViewHolder  {
    // Attachment
    private PorterShapeImageView iv_media_thumb;
    private TextView tv_media_title, tv_media_play, tv_media_des;

    private ConstraintLayout cl_video;

    final String TAG = AttachmentViewHolder.class.getSimpleName();

    public AttachmentViewHolderMedia(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
        tv_media_title = itemView.findViewById(R.id.tv_media_title);
        tv_media_play = itemView.findViewById(R.id.tv_media_play);
        tv_media_des = itemView.findViewById(R.id.tv_media_des);
    }

    @Override
    public void bind(Context context,
                     MessageListItem messageListItem,
                     Attachment attachment,
                     MessageListView.AttachmentClickListener clickListener,
                     MessageListView.MessageLongClickListener longClickListener) {

        super.bind(context, messageListItem, attachment, clickListener, longClickListener);
        configMediaAttach();
    }


    private void configImageThumbBackground(Attachment attachment) {
        Drawable background = getBubbleHelper().getDrawableForAttachment(getMessage(),
                getMessageListItem().isMine(),
                getMessageListItem().getPositions(),
                attachment);
        iv_media_thumb.setShape(getContext(), background);
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
        iv_media_thumb.setOnClickListener(this);
        iv_media_thumb.setOnLongClickListener(this);
        if (!attachUrl.contains("https:"))
            attachUrl = "https:" + attachUrl;
        Glide.with(getContext())
                .load(attachUrl)
                .into(iv_media_thumb);
        if (!getMessage().getType().equals(ModelType.message_ephemeral))
            tv_media_title.setText(getAttachment().getTitle());
        tv_media_des.setText(getAttachment().getText());


        if (StringUtility.isNullOrEmpty(getAttachment().getText()))
            tv_media_des.setVisibility(View.GONE);
        else
            tv_media_des.setVisibility(View.VISIBLE);

        if (StringUtility.isNullOrEmpty(getAttachment().getTitle()))
            tv_media_title.setVisibility(View.GONE);
        else
            tv_media_title.setVisibility(View.VISIBLE);

        if (type.equals(ModelType.attach_video))
            tv_media_play.setVisibility(View.VISIBLE);
        else
            tv_media_play.setVisibility(View.GONE);
    }
}
