package com.getstream.sdk.chat.utils;

import com.getstream.sdk.chat.model.AttachmentMetaData;
import com.getstream.sdk.chat.model.ModelType;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.utils.ProgressCallback;

class UploadManager {

    private Channel channel;
    private List<AttachmentMetaData> queue = new ArrayList<>();

    public UploadManager(Channel channel) {
        this.channel = channel;
    }

    public void uploadFile(AttachmentMetaData data, ProgressCallback fileListener) {

        queue.add(data);

        String type = channel.getType();
        String id = channel.getId();

        ProgressCallback progressCallback = new ProgressCallback() {
            @Override
            public void onSuccess(@NotNull String path) {

                Attachment attachment = new Attachment();

                attachment.setMimeType(data.mimeType);
                attachment.setFileSize((int) data.file.length());
                attachment.setName(data.file.getName());
                attachment.setType(data.type);
                attachment.setUrl(path);

                if (data.type.equals(ModelType.attach_image)) {
                    attachment.setImageUrl(path);
                    attachment.setFallback(data.file.getName());
                } else {
                    attachment.setAssetUrl(path);
                }

                data.attachment = attachment;

                queue.remove(data);
                fileListener.onSuccess(path);
            }

            @Override
            public void onError(@NotNull ChatError error) {
                queue.remove(data);
                fileListener.onError(error);
            }

            @Override
            public void onProgress(long progress) {
                fileListener.onProgress(progress);
            }
        };

        if (data.isImage()) {
            ChatClient.instance().sendImage(type, id, data.file, progressCallback);
        } else {
            ChatClient.instance().sendFile(type, id, data.file, progressCallback);
        }


    }

    public void removeFromQueue(AttachmentMetaData file) {
        queue.remove(file);
    }

    public boolean isUploadingFile() {
        return !queue.isEmpty();
    }

    //
    public void resetQueue() {
        this.queue.clear();
    }
//

}
