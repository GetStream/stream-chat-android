package com.getstream.sdk.chat.utils;

import android.content.Context;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.UploadAttachment;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.ModelType;
import io.getstream.chat.android.client.utils.ProgressCallback;

public class UploadManager {

    private Channel channel;
    private Context context;
    private List<UploadAttachment> queue = new ArrayList<>();

    public UploadManager(Channel channel, Context context) {
        this.channel = channel;
        this.context = context;
    }

    public void uploadFile(UploadAttachment attachment, ProgressCallback fileListener) {

        queue.add(attachment);

        String type = channel.getType();
        String id = channel.getId();

        StreamChat.getInstance().sendFile(type, id, attachment.file, attachment.mimeType, new ProgressCallback() {
            @Override
            public void onSuccess(@NotNull String path) {

                Attachment a = new Attachment();

                if (attachment.type.equals(ModelType.attach_image)) {
                    //File file = new File(attachment.config.getFilePath());
                    //attachment.setImageURL(response.getFileUrl());
                    //attachment.setFallback(file.getName());
                } else {
                    //attachment.setAssetURL(response.getFileUrl());
                }

                attachment.isUploaded = true;
                attachment.uploadedUrl = path;
                queue.remove(attachment);
                fileListener.onSuccess(path);
            }

            @Override
            public void onError(@NotNull ChatError error) {
                queue.remove(attachment);
                fileListener.onError(error);
            }

            @Override
            public void onProgress(long progress) {
                fileListener.onProgress(progress);
            }
        });
    }

    public void removeFromQueue(UploadAttachment file) {
        queue.remove(file);
    }

    //    private void fileUploadSuccess(Attachment attachment,
//                                   UploadFileResponse response,
//                                   boolean isMedia,
//                                   UploadFileListener fileListener) {
//
//        updateQueue(attachment, false);
//        if (!attachment.config.isSelected())
//            return;
//
//        if (isMedia && attachment.getType().equals(ModelType.attach_image)) {
//            File file = new File(attachment.config.getFilePath());
//            attachment.setImageURL(response.getFileUrl());
//            attachment.setFallback(file.getName());
//        } else {
//            attachment.setAssetURL(response.getFileUrl());
//        }
//
//        attachment.config.setUploaded(true);
//        fileListener.onSuccess(attachment);
//    }
//
//    private void fileUploadFailed(Attachment attachment, String errMsg, UploadFileListener fileListener) {
//        Utils.showMessage(context, errMsg);
//        fileListener.onFailed(attachment);
//    }
//
//    private void fileUploading(com.getstream.sdk.chat.model.Attachment.AttachmentConfig attachment,
//                               Integer percentage,
//                               UploadFileListener fileListener) {
//        attachment.progress = percentage;
//        fileListener.onProgress(attachment);
//    }
//
//
    public boolean isUploadingFile() {
        return !queue.isEmpty();
    }

    //
    public void resetQueue() {
        this.queue.clear();
    }
//

}
