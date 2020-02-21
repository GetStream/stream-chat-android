package com.getstream.sdk.chat.model;

import java.io.File;

import io.getstream.chat.android.client.models.Attachment;

public class AttachmentData {

    public Attachment uploaded;

    public boolean isSelected;
    public int videoLength;
    public int progress;
    public File file;
    public String type;
    public String mimeType;
    public String title;

    public boolean isUploaded() {
        return uploaded != null;
    }

}