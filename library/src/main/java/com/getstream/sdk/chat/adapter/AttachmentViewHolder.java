package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;

public class AttachmentViewHolder extends BaseAttachmentViewHolder {

    final String TAG = AttachmentViewHolder.class.getSimpleName();
    // Attachment
    private ConstraintLayout cl_attachment_media, cl_des;
    private PorterShapeImageView iv_media_thumb;
    private ListView lv_attachment_file;
    private TextView tv_media_title, tv_media_play, tv_media_des;

    private Context context;
    private MessageListItem messageListItem;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;
    private MessageListView.BubbleHelper bubbleHelper;
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.MessageLongClickListener longClickListener;

    public AttachmentViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        cl_attachment_media = itemView.findViewById(R.id.cl_attachment_media);
        cl_des = itemView.findViewById(R.id.cl_des);
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
        lv_attachment_file = itemView.findViewById(R.id.lv_attachment_file);
        tv_media_title = itemView.findViewById(R.id.tv_media_title);
        tv_media_play = itemView.findViewById(R.id.tv_media_play);
        tv_media_des = itemView.findViewById(R.id.tv_media_des);
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
    }

    private void configAttachment() {

        boolean hasFile = false;
        boolean hasMedia = false;
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType().equals(ModelType.attach_unknown)) continue;
            if (attachment.getType().equals(ModelType.attach_file)) {
                hasFile = true;
            } else {
                hasMedia = true;
            }
        }

        if (hasMedia) {
            cl_attachment_media.setVisibility(View.VISIBLE);
            configMediaAttach();
        } else {
            cl_attachment_media.setVisibility(View.GONE);
        }

        if (hasFile) {
            lv_attachment_file.setVisibility(View.VISIBLE);
            configFileAttach();
        } else {
            lv_attachment_file.setVisibility(View.GONE);
        }

        if (!hasMedia && !hasFile) {
            iv_media_thumb.setVisibility(View.GONE);
            if (!TextUtils.isEmpty(attachment.getTitle())) {
                cl_attachment_media.setVisibility(View.VISIBLE);
                tv_media_title.setVisibility(View.VISIBLE);
                tv_media_title.setText(attachment.getTitle());
            }else
                tv_media_title.setVisibility(View.GONE);

            if (!TextUtils.isEmpty(attachment.getText())) {
                cl_attachment_media.setVisibility(View.VISIBLE);
                tv_media_des.setVisibility(View.VISIBLE);
                tv_media_des.setText(attachment.getText());
            }else
                tv_media_des.setVisibility(View.GONE);
        }
    }

    private void configImageThumbBackground() {
        Drawable background = bubbleHelper.getDrawableForAttachment(message, messageListItem.isMine(), messageListItem.getPositions(), attachment);
        iv_media_thumb.setShape(context, background);
        if (tv_media_des.getVisibility() == View.VISIBLE || tv_media_title.getVisibility() == View.VISIBLE) {
            background = bubbleHelper.getDrawableForAttachmentDescription(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions());
            cl_des.setBackground(background);
        }
    }

    private void configAttachViewBackground(View view) {
        if (style.getMessageBubbleDrawable(messageListItem.isMine()) != -1)
            view.setBackground(context.getDrawable(style.getMessageBubbleDrawable(messageListItem.isMine())));
    }


    private void configFileAttach() {
        configAttachViewBackground(lv_attachment_file);
        List<Attachment> attachments = new ArrayList<>();
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType().equals(ModelType.attach_unknown)) continue;
            if (attachment.getType().equals(ModelType.attach_file)) {
                attachments.add(attachment);
            }
        }
        AttachmentListAdapter attachAdapter = new AttachmentListAdapter(context, attachments, false, false);

        lv_attachment_file.setAdapter(attachAdapter);
        lv_attachment_file.setOnItemClickListener((AdapterView<?> parent, View view,
                                                   int position, long id) -> {
            StreamChat.getLogger().logD(this,"Attach onMessageClick: " + position);
            if (clickListener != null)
                clickListener.onAttachmentClick(message, attachment);
        });

        lv_attachment_file.setOnItemLongClickListener((AdapterView<?> parent, View view, int position_, long id) -> {
            if (longClickListener != null)
                longClickListener.onMessageLongClick(message);
            return true;
        });

        float height = context.getResources().getDimension(R.dimen.stream_attach_file_height);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) lv_attachment_file.getLayoutParams();
        params.height = (int) height * attachments.size();
        lv_attachment_file.setLayoutParams(params);
    }

    private void configMediaAttach() {
        List<Attachment> attachments = new ArrayList<>();
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType().equals(ModelType.attach_unknown)) continue;
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
            cl_attachment_media.setVisibility(View.GONE);
            return;
        }
        cl_attachment_media.setVisibility(View.VISIBLE);
        configAttachViewBackground(cl_attachment_media);
        configImageThumbBackground();
        configClickListeners();

        if (!attachUrl.contains("https:"))
            attachUrl = "https:" + attachUrl;
        Glide.with(context)
                .load(StreamChat.getInstance(context).getUploadStorage().signGlideUrl(attachUrl))
                .into(iv_media_thumb);
        if (!message.getType().equals(ModelType.message_ephemeral))
            tv_media_title.setText(attachments.get(0).getTitle());
        tv_media_des.setText(attachments.get(0).getText());

        if (TextUtils.isEmpty(attachments.get(0).getText()))
            tv_media_des.setVisibility(View.GONE);
        else
            tv_media_des.setVisibility(View.VISIBLE);

        if (TextUtils.isEmpty(attachments.get(0).getTitle()))
            tv_media_title.setVisibility(View.GONE);
        else
            tv_media_title.setVisibility(View.VISIBLE);

        if (type.equals(ModelType.attach_video))
            tv_media_play.setVisibility(View.VISIBLE);
        else
            tv_media_play.setVisibility(View.GONE);
    }

    private void applyStyle() {
        if(messageListItem.isMine()) {
            style.attachmentTitleTextMine.apply(tv_media_title);
            style.attachmentDescriptionTextMine.apply(tv_media_des);
        } else {
            style.attachmentTitleTextTheirs.apply(tv_media_title);
            style.attachmentDescriptionTextTheirs.apply(tv_media_des);
        }
    }

    private void configClickListeners(){
        cl_attachment_media.setOnClickListener(view -> {
            if (clickListener != null)
                clickListener.onAttachmentClick(message, attachment);
        });

        cl_attachment_media.setOnLongClickListener(view -> {
            if (longClickListener != null)
                longClickListener.onMessageLongClick(message);
            return true;
        });
    }
}
