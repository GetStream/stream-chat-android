package com.getstream.sdk.chat.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getstream.sdk.chat.utils.Utils;

import java.io.File;

import io.getstream.chat.android.client.models.Attachment;

public class AttachmentMetaData {
    public boolean isSelected;
    public long videoLength;
    public long size;
    @Nullable
    public Uri uri;
    public String type;
    public String mimeType;
    public String title;
    @Nullable
    public File file;

    public AttachmentMetaData(Attachment attachment) {
        this.type = attachment.getType();
        this.mimeType = attachment.getMimeType();
        this.title = attachment.getTitle();
    }

    public AttachmentMetaData(@NonNull File file) {
        this.file = file;
        this.uri = Uri.fromFile(file);
        this.mimeType = Utils.getMimeType(file);
    }

    public AttachmentMetaData(@NonNull Uri uri, String mimeType) {
        this.uri = uri;
        this.mimeType = mimeType;
    }
}
