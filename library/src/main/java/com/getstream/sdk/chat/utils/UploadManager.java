package com.getstream.sdk.chat.utils;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;
import com.getstream.sdk.chat.rest.response.UploadFileResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadManager {

    private static List<Attachment> queue;

    public static void uploadFile(Channel channel, List<Attachment> attachments,
                                  Attachment attachment,
                                  boolean isMedia) {
        if (queue == null)
            queue = new ArrayList<>();

        queue.add(attachment);

        UploadFileCallback callback = getUploadFileCallBack(attachments, attachment, isMedia);
        if (isMedia && attachment.getType().equals(ModelType.attach_image))
            channel.sendImage(attachment.config.getFilePath(), "image/jpeg", callback);
        else
            channel.sendFile(attachment.config.getFilePath(), attachment.getMime_type(), callback);
    }

    public static UploadFileCallback getUploadFileCallBack(List<Attachment> attachments,
                                                           Attachment attachment,
                                                           boolean isMedia) {
        return new UploadFileCallback<UploadFileResponse, Integer>() {
            @Override
            public void onSuccess(UploadFileResponse response) {
                fileUploadSuccess(attachment, response, isMedia);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                fileUploadFailed(attachment, errMsg);
            }

            @Override
            public void onProgress(Integer percentage) {
                fileUploading(attachment, percentage);
            }
        };
    }

    public static void fileUploadSuccess(Attachment attachment,
                                         UploadFileResponse response,
                                         boolean isMedia) {

        queue.remove(attachment);
        if (!attachment.config.isSelected())
            return;

        if (isMedia && attachment.getType().equals(ModelType.attach_image)) {
            File file = new File(attachment.config.getFilePath());
            attachment.setImageURL(response.getFileUrl());
            attachment.setFallback(file.getName());
        } else {
            attachment.setAssetURL(response.getFileUrl());
        }

        attachment.config.setUploaded(true);
    }

    public static void fileUploadFailed(Attachment attachment,
                                        String errMsg) {
        queue.remove(attachment);
        attachment.config.setSelected(false);
        Utils.showMessage(StreamChat.getContext(), errMsg);
    }

    public static void fileUploading(Attachment attachment,
                                     Integer percentage) {
        attachment.config.setProgress(percentage);
    }


    public static void cancelUploadingAttachment(Attachment attachment) {
        attachment.config.setSelected(false);
        if (queue.contains(attachment))
            queue.remove(attachment);
    }
}
