package io.getstream.chat.example.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.adapter.MessageListItem;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;

import io.getstream.chat.example.R;

public class AttachmentViewHolderCustom extends BaseAttachmentViewHolder {
    private PorterShapeImageView iv_media_thumb;

    public AttachmentViewHolderCustom(int resId, ViewGroup parent) {
        super(resId, parent);

        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
    }

    @Override
    public void bind(Context context,
                     MessageListItem messageListItem,
                     Attachment attachment,
                     MessageListView.AttachmentClickListener clickListener,
                     MessageListView.MessageLongClickListener longClickListener) {
        super.bind(context, messageListItem, attachment,clickListener, longClickListener);

        Drawable background = bubbleHelper.getDrawableForAttachment(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions(), attachment);
        iv_media_thumb.setShape(context, background);
        iv_media_thumb.setOnClickListener(this);
        iv_media_thumb.setOnLongClickListener(this);

        Glide.with(context)
                .load(StreamChat.getInstance(context).getUploadStorage().signGlideUrl(attachment.getThumbURL()))
                .into(iv_media_thumb);
    }
}
