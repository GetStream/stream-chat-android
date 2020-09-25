package com.getstream.sdk.chat.model;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.getstream.sdk.chat.utils.Utils;

import java.io.File;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttachmentMetaData)) return false;
        AttachmentMetaData that = (AttachmentMetaData) o;
        return Objects.equals(uri, that.uri) &&
                Objects.equals(type, that.type) &&
                Objects.equals(mimeType, that.mimeType) &&
                Objects.equals(title, that.title) &&
                Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri, type, mimeType, title, file);
    }
}
