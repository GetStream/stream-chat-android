package com.getstream.sdk.chat.navigation.destinations;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.utils.Utils;
import com.getstream.sdk.chat.utils.frescoimageviewer.ImageViewer;
import com.getstream.sdk.chat.view.activity.AttachmentActivity;
import com.getstream.sdk.chat.view.activity.AttachmentDocumentActivity;
import com.getstream.sdk.chat.view.activity.AttachmentMediaActivity;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Message;

public class AttachmentDestination extends ChatDestination {

    public final Message message;
    public final Attachment attachment;
    public final String type;
    public final String url;

    public AttachmentDestination(Message message, Attachment attachment, Context context) {
        super(context);
        this.message = message;
        this.attachment = attachment;

        String url = "";
        String type = attachment.getType();

        switch (attachment.getType()) {
            case ModelType.attach_file:
            case ModelType.attach_video:
                url = attachment.getAssetUrl();
                break;
            case ModelType.attach_image:
                if (attachment.getOgUrl() != null) {
                    url = attachment.getOgUrl();
                    type = ModelType.attach_link;
                } else {
                    //multiple images case, no single url
                }
                break;
            case ModelType.attach_giphy:
                url = attachment.getThumbUrl();
                break;
            case ModelType.attach_product:
                url = attachment.getUrl();
                break;
        }

        this.url = url;
        this.type = type;
    }

    @Override
    public void navigate() {
        showAttachment(message, attachment);
    }

    public void showAttachment(Message message, Attachment attachment) {
        String url = null;
        String type = null;

        switch (attachment.getType()) {
            case ModelType.attach_file:
                loadFile(attachment);
                return;
            case ModelType.attach_image:
                if (attachment.getOgUrl() != null) {
                    url = attachment.getOgUrl();
                    type = ModelType.attach_link;
                } else {
                    List<String> imageUrls = new ArrayList<>();
                    for (Attachment a : message.getAttachments()) {
                        if (!a.getType().equals(ModelType.attach_image) || TextUtils.isEmpty(a.getImageUrl()))
                            continue;
                        imageUrls.add(a.getImageUrl());
                    }
                    if (imageUrls.isEmpty()) {
                        Utils.showMessage(context, "Invalid image(s)!");
                        return;
                    }

                    int position = message.getAttachments().indexOf(attachment);
                    if (position > imageUrls.size() - 1) position = 0;
                    new ImageViewer.Builder<>(context, imageUrls)
                            .setStartPosition(position)
                            .show();
                    return;
                }
                break;
            case ModelType.attach_video:
                url = attachment.getAssetUrl();
                break;
            case ModelType.attach_giphy:
                url = attachment.getAssetUrl();
                break;
            case ModelType.attach_product:
                url = attachment.getUrl();
                break;
        }
        if (TextUtils.isEmpty(url)) {
            Utils.showMessage(context, context.getString(R.string.stream_attachment_invalid_url));
            return;
        }

        if (type == null) type = attachment.getType();
        Intent intent = new Intent(context, AttachmentActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("url", url);
        start(intent);
    }

    private void loadFile(Attachment attachment) {

        String mimeType = attachment.getMimeType();
        String url = attachment.getAssetUrl();

        if (mimeType == null) {
            ChatLogger.Companion.getInstance().logE("AttachmentDestination", "MimeType is null for url" + url);
            Utils.showMessage(context, context.getString(R.string.stream_attachment_invalid_mime_type, attachment.getName()));
        } else {
            // Media
            if (mimeType.contains("audio") ||
                    mimeType.contains("video")) {
                Intent intent = new Intent(context, AttachmentMediaActivity.class);
                intent.putExtra(AttachmentMediaActivity.TYPE_KEY, mimeType);
                intent.putExtra(AttachmentMediaActivity.URL_KEY, url);
                start(intent);
            } else if (mimeType.equals("application/msword") ||
                    mimeType.equals(ModelType.attach_mime_txt) ||
                    mimeType.equals(ModelType.attach_mime_pdf) ||
                    mimeType.contains("application/vnd")) {

                Intent intent = new Intent(context, AttachmentDocumentActivity.class);
                intent.putExtra("url", url);
                start(intent);
            }
        }
    }
}
