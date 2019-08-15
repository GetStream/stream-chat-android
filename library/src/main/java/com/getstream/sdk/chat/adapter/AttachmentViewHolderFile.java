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
import com.getstream.sdk.chat.model.MessageTagModel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.SelectAttachmentModel;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.StringUtility;
import com.getstream.sdk.chat.utils.roundedImageView.PorterShapeImageView;
import com.getstream.sdk.chat.view.AttachmentListView;
import com.getstream.sdk.chat.view.MessageListViewStyle;

import java.util.ArrayList;
import java.util.List;

public class AttachmentViewHolderFile extends BaseAttachmentViewHolder {

    private Context context;
    private Message message;
    private Attachment attachment;
    private MessageListViewStyle style;

    // Attachment
    private ConstraintLayout cl_attachment;

    // Action
    private AttachmentListView.AttachmentClickListener clickListener;
    private AttachmentListView.AttachmentClickListener longClickListener;


    final String TAG = AttachmentViewHolder.class.getSimpleName();

    public AttachmentViewHolderFile(int resId, ViewGroup parent) {
        super(resId, parent);
        // Attach
        cl_attachment = itemView.findViewById(R.id.cl_attachment);
    }

    @Override
    public void bind(Context context, Message message, Attachment attachment, AttachmentListView.AttachmentClickListener clickListener, AttachmentListView.AttachmentClickListener longClickListener) {
        this.context = context;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
        this.message = message;
        this.attachment = attachment;

        configFileAttach();
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
        configAttachViewBackground(cl_attachment);




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
