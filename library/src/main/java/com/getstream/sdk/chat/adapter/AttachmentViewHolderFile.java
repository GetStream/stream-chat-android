package com.getstream.sdk.chat.adapter;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class AttachmentViewHolderFile extends BaseAttachmentViewHolder {

    final String TAG = AttachmentViewHolder.class.getSimpleName();
    // Attachment
    private ConstraintLayout cl_attachment;
    private ImageView iv_file_thumb;
    private TextView tv_file_size, tv_file_title;

    private Context context;
    private MessageListItem messageListItem;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;
    private MessageListView.BubbleHelper bubbleHelper;
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.MessageLongClickListener longClickListener;

    public AttachmentViewHolderFile(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        cl_attachment = itemView.findViewById(R.id.attachmentview);
        iv_file_thumb = itemView.findViewById(R.id.iv_file_thumb);
        tv_file_size = itemView.findViewById(R.id.tv_file_size);
        tv_file_title = itemView.findViewById(R.id.tv_file_title);
    }

    @Override
    public void bind(@NonNull Context context,
                     @NonNull MessageListItem messageListItem,
                     @NonNull Message message,
                     @NonNull Attachment attachment,
                     @NonNull MessageListViewStyle style,
                     @NonNull MessageListView.BubbleHelper bubbleHelper,
                     @Nullable MessageListView.AttachmentClickListener clickListener,
                     @Nullable MessageListView.MessageLongClickListener longClickListener) {

        this.context = context;
        this.messageListItem = messageListItem;
        this.message = message;
        this.attachment = attachment;
        this.style = style;
        this.bubbleHelper = bubbleHelper;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;

        applyStyle();
        configAttachment();
        configClickListeners();
    }

    private void applyStyle() {
        if(messageListItem.isMine()) {
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

        Drawable background = bubbleHelper.getDrawableForAttachment(messageListItem.getMessage(),
                messageListItem.isMine(),
                messageListItem.getPositions(),
                attachment);
        cl_attachment.setBackground(background);

    }

    private void configClickListeners(){
        cl_attachment.setOnClickListener(view -> {
            if (clickListener != null)
                clickListener.onAttachmentClick(message, attachment);
        });

        cl_attachment.setOnLongClickListener(view -> {
            if (longClickListener != null)
                longClickListener.onMessageLongClick(message);
            return true;
        });
    }
}

