package com.getstream.sdk.chat.adapter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class AttachmentViewHolderFile extends BaseAttachmentViewHolder {

    final String TAG = AttachmentViewHolder.class.getSimpleName();
    // Attachment
    private ConstraintLayout cl_attachment;
    private ImageView iv_file_thumb;
    private TextView tv_file_size, tv_file_title;


    public AttachmentViewHolderFile(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        cl_attachment = itemView.findViewById(R.id.attachmentview);
        iv_file_thumb = itemView.findViewById(R.id.iv_file_thumb);
        tv_file_size = itemView.findViewById(R.id.tv_file_size);
        tv_file_title = itemView.findViewById(R.id.tv_file_title);
    }

    @Override
    public void bind(Context context,
                     MessageListItem messageListItem,
                     Attachment attachment,
                     MessageListView.AttachmentClickListener clickListener,
                     MessageListView.MessageLongClickListener longClickListener) {
        super.bind(context, messageListItem, attachment, clickListener, longClickListener);
        applyStyle();
        configAttachment();
    }

    private void applyStyle() {

        if(getMessageListItem().isMine()) {
            style.attachmentTitleTextMine.apply(tv_file_title);
            style.attachmentFileSizeTextMine.apply(tv_file_size);
        } else {
            style.attachmentTitleTextTheirs.apply(tv_file_title);
            style.attachmentFileSizeTextTheirs.apply(tv_file_size);
        }
    }

    private void configAttachment() {
        tv_file_size.setText(attachment.getFileSizeHumanized());
        // update the icon nicely
        iv_file_thumb.setImageResource(attachment.getIcon());
        tv_file_title.setText(attachment.getTitle());

        Drawable background = getBubbleHelper().getDrawableForAttachment(getMessageListItem().getMessage(),
                getMessageListItem().isMine(),
                getMessageListItem().getPositions(),
                attachment);
        cl_attachment.setBackground(background);

        cl_attachment.setOnClickListener(this);
        cl_attachment.setOnLongClickListener(this);
    }
}

