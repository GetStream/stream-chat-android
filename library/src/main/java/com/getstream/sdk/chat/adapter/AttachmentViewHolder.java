package com.getstream.sdk.chat.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.getstream.sdk.chat.BaseAttachmentViewHolder;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.SelectAttachmentModel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.MessageListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;

public class AttachmentViewHolder extends BaseAttachmentViewHolder {

    private Context context;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;

    // Attachment
    private ConstraintLayout cl_attachment, cl_attachment_media;
    private PorterShapeImageView iv_media_thumb;
    private ListView lv_attachment_file;
    private ImageView iv_media_more;
    private TextView tv_more;
    private TextView tv_media_title, tv_media_play, tv_media_des;
    // Action
    private MessageListView.BubbleHelper bubbleHelper;
    private MessageListView.AttachmentClickListener clickListener;
    private MessageListView.MessageLongClickListener longClickListener;
    private MessageListItem messageListItem;


    final String TAG = AttachmentViewHolder.class.getSimpleName();

    public AttachmentViewHolder(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        cl_attachment = itemView.findViewById(R.id.cl_attachment);
        cl_attachment_media = itemView.findViewById(R.id.cl_attachment_media);
        iv_media_thumb = itemView.findViewById(R.id.iv_media_thumb);
        lv_attachment_file = itemView.findViewById(R.id.lv_attachment_file);
        iv_media_more = itemView.findViewById(R.id.iv_media_more);
        tv_more = itemView.findViewById(R.id.tv_more);
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
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.messageListItem = messageListItem;
        this.message = messageListItem.getMessage();
        this.attachment = attachment;

        configAttachment();

        //configMediaAttach();
        //configParamsAttachment();
    }

    private void configAttachment() {
//        if (tv_deleted.getVisibility() == View.VISIBLE || ll_send_failed.getVisibility() == View.VISIBLE) {
//            cl_attachment.setVisibility(View.GONE);
//            return;
//        }

        boolean hasFile = false;
        boolean hasMedia = false;
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType() == null) continue;
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
    }

    private void configImageThumbBackground(Attachment attachment) {
        Drawable background = getBubbleHelper().getDrawableForAttachment(messageListItem.getMessage(), messageListItem.isMine(), messageListItem.getPositions(), attachment);
        iv_media_thumb.setShape(context, background);
    }

    private void configAttachViewBackground(View view) {
        Drawable background;
        // TODO: fix this somehow
        if (true) {
            background = style.getMessageBubbleDrawableTheirs();
        } else {
            background = style.getMessageBubbleDrawableMine();
        }
        view.setBackground(background);
    }


    private void configFileAttach() {
        configAttachViewBackground(lv_attachment_file);
        List<Attachment> attachments = new ArrayList<>();
        for (Attachment attachment : message.getAttachments()) {
            if (attachment.getType() == null) continue;
            if (attachment.getType().equals(ModelType.attach_file)) {
                attachments.add(attachment);
            }
        }
        AttachmentListAdapter attachAdapter = new AttachmentListAdapter(context, attachments, false, false);

        lv_attachment_file.setAdapter(attachAdapter);
        lv_attachment_file.setOnItemClickListener((AdapterView<?> parent, View view,
                                                   int position, long id) -> {
            Log.d(TAG, "Attach onMessageClick: " + position);

                SelectAttachmentModel attachmentModel = new SelectAttachmentModel();
                attachmentModel.setAttachmentIndex(position);
                attachmentModel.setAttachments(attachments);

                view.setTag(attachmentModel);
                triggerClick(message, attachment);

        });
        lv_attachment_file.setOnItemLongClickListener((AdapterView<?> parent, View view, int position_, long id) -> {
            triggerLongClick(message);
            return true;
        });

        float height = context.getResources().getDimension(R.dimen.attach_file_height);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) lv_attachment_file.getLayoutParams();
        params.height = (int) height * attachments.size();
        lv_attachment_file.setLayoutParams(params);
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
            cl_attachment_media.setVisibility(View.GONE);
            return;
        }
        cl_attachment_media.setVisibility(View.VISIBLE);
        configAttachViewBackground(cl_attachment_media);
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
        cl_attachment_media.setOnClickListener((View v) -> {
                if (attachments.size() > 0) {
                    SelectAttachmentModel attachmentModel = new SelectAttachmentModel();
                    attachmentModel.setAttachmentIndex(0);
                    attachmentModel.setAttachments(attachments);
                    Log.d(TAG, "Attachments set : " + attachmentModel.getAttachments().size());
                    v.setTag(attachmentModel);
                }
                this.triggerClick(message, attachment);


        });


        cl_attachment_media.setOnLongClickListener((View v) -> {
            this.triggerLongClick(message);
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

    private void triggerLongClick(Message message) {
        if (this.longClickListener != null) {
            this.longClickListener.onMessageLongClick(message);
        }
    }
    private void triggerClick(Message message, Attachment attachment) {
        if (this.clickListener != null) {
            this.clickListener.onAttachmentClick(message, attachment);
        }
    }

    private void configParamsAttachment() {
        if (cl_attachment.getVisibility() != View.VISIBLE) return;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) cl_attachment.getLayoutParams();
        ConstraintLayout.LayoutParams paramsLv = (ConstraintLayout.LayoutParams) lv_attachment_file.getLayoutParams();
        if (lv_attachment_file.getVisibility() == View.VISIBLE) {
            // TODO; fix this somehow
            if (true) {
                params.horizontalBias = 0f;
                paramsLv.horizontalBias = 0f;
            } else {
                params.horizontalBias = 1f;
                paramsLv.horizontalBias = 1f;
            }
            params.width = ConstraintLayout.LayoutParams.WRAP_CONTENT;
            lv_attachment_file.setLayoutParams(paramsLv);
            cl_attachment.setLayoutParams(params);
        } else {
            params.width = ConstraintLayout.LayoutParams.MATCH_PARENT;
        }
        cl_attachment.setLayoutParams(params);

        lv_attachment_file.setBackgroundColor(context.getResources().getColor(R.color.black));
    }

    public void setStyle(MessageListViewStyle style) {
        this.style = style;
    }
}
