package com.getstream.sdk.chat.adapter;


import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.getstream.sdk.chat.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class AttachmentViewHolderFile extends BaseAttachmentViewHolder {

    final String TAG = AttachmentViewHolder.class.getSimpleName();
    private Context context;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;
    // Attachment
    private ConstraintLayout cl_attachment;
    private ImageView iv_file_thumb;
    private TextView tv_file_size, tv_file_title;
    private Entity entity;
    // Action
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.AttachmentClickListener longClickListener;

    public AttachmentViewHolderFile(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        cl_attachment = itemView.findViewById(R.id.cl_attachment);
        iv_file_thumb = itemView.findViewById(R.id.iv_file_thumb);
        tv_file_size = itemView.findViewById(R.id.tv_file_size);
        tv_file_title = itemView.findViewById(R.id.tv_file_title);
    }

    @Override
    public void bind(Context context, Entity entity, Attachment attachment, MessageListView.AttachmentClickListener clickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.entity = entity;
        this.message = entity.getMessage();
        this.attachment = attachment;
        tv_file_size.setText(attachment.getFileSizeHumanized());
        // update the icon nicely
        iv_file_thumb.setImageResource(attachment.getIcon());
        tv_file_title.setText(attachment.getTitle());

        Drawable background = getBubbleHelper().getDrawableForAttachment(entity.getMessage(), entity.isMine(), entity.getPositions(), attachment);
        cl_attachment.setBackground(background);
    }

    private void triggerLongClick(Message message, Attachment attachment) {
        if (this.longClickListener != null) {
            this.longClickListener.onClick(message, attachment);
        }
    }

    private void triggerClick(Message message, Attachment attachment) {
        if (this.clickListener != null) {
            this.clickListener.onClick(message, attachment);
        }
    }

    private void configParamsAttachment() {
        ConstraintLayout.LayoutParams paramsLv = (ConstraintLayout.LayoutParams) cl_attachment.getLayoutParams();

        cl_attachment.setLayoutParams(paramsLv);


        cl_attachment.setBackgroundColor(context.getResources().getColor(R.color.black));
    }

    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }
}
