package io.getstream.chat.example;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

public class AttachmentViewHolderImgur extends BaseAttachmentViewHolder {

    private PorterShapeImageView iv_media_thumb;

    private Context context;
    private MessageListItem messageListItem;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;
    private MessageListView.BubbleHelper bubbleHelper;
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.MessageLongClickListener longClickListener;

    public AttachmentViewHolderImgur(int resId, ViewGroup parent) {
        super(resId, parent);
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
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

        Drawable background = bubbleHelper.getDrawableForAttachment(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions(), attachment);
        iv_media_thumb.setShape(context, background);
        configClickListeners();

        Glide.with(context)
                .load(StreamChat.getInstance(context).getUploadStorage().signGlideUrl(attachment.getThumbURL()))
                .into(iv_media_thumb);
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
