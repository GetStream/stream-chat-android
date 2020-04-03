package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.utils.Utils;

import java.io.File;

import io.getstream.chat.android.client.models.Attachment;

public class AttachmentMetaData {

    public Attachment attachment;

    public boolean isSelected;
    public int videoLength;
    public int progress;
    public File file;
    public String type;
    public String mimeType;
    public String title;

    public AttachmentMetaData(Attachment attachment) {
        this.attachment = attachment;
        this.type = attachment.getType();
        this.mimeType = attachment.getMimeType();
        this.title = attachment.getTitle();
    }

    public AttachmentMetaData(File file) {
        this.file = file;
        this.mimeType = Utils.getMimeType(file);
    }

    public boolean isUploaded() {
        return attachment != null;
    }

    public boolean isImage() {
        return mimeType != null && mimeType.contains("image");
    }

}