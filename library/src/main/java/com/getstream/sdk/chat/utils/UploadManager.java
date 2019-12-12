package com.getstream.sdk.chat.utils;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.adapter.AttachmentListAdapter;
import com.getstream.sdk.chat.adapter.MediaAttachmentAdapter;
import com.getstream.sdk.chat.adapter.MediaAttachmentSelectedAdapter;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;
import com.getstream.sdk.chat.rest.response.UploadFileResponse;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadManager {
    private Channel channel;
    private MediaAttachmentAdapter mediaAttachmentAdapter;
    private MediaAttachmentSelectedAdapter selectedMediaAttachmentAdapter;
    private AttachmentListAdapter fileAttachmentAdapter;
    private AttachmentListAdapter selectedFileAttachmentAdapter;
    private List<Attachment> queue;

    public UploadManager(Channel channel,
                         MediaAttachmentAdapter mediaAttachmentAdapter,
                         MediaAttachmentSelectedAdapter selectedMediaAttachmentAdapter,
                         AttachmentListAdapter fileAttachmentAdapter,
                         AttachmentListAdapter selectedFileAttachmentAdapter) {
        this.channel = channel;
        this.mediaAttachmentAdapter = mediaAttachmentAdapter;
        this.selectedMediaAttachmentAdapter = selectedMediaAttachmentAdapter;
        this.fileAttachmentAdapter = fileAttachmentAdapter;
        this.selectedFileAttachmentAdapter = selectedFileAttachmentAdapter;
    }

    public void uploadFile(Attachment attachment,
                           boolean isMedia) {
        if (queue == null)
            queue = new ArrayList<>();

        queue.add(attachment);

        UploadFileCallback callback = getUploadFileCallBack(attachment, isMedia);
        if (isMedia && attachment.getType().equals(ModelType.attach_image))
            channel.sendImage(attachment.config.getFilePath(), "image/jpeg", callback);
        else
            channel.sendFile(attachment.config.getFilePath(), attachment.getMime_type(), callback);
    }

    private UploadFileCallback getUploadFileCallBack(Attachment attachment,
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

    private void fileUploadSuccess(Attachment attachment,
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

    private void fileUploadFailed(Attachment attachment,
                                        String errMsg) {
        queue.remove(attachment);
        attachment.config.setSelected(false);
        Utils.showMessage(StreamChat.getContext(), errMsg);
    }

    private void fileUploading(Attachment attachment,
                                     Integer percentage) {
        attachment.config.setProgress(percentage);
    }


    public void cancelUploadingAttachment(Attachment attachment) {
        attachment.config.setSelected(false);
        if (queue.contains(attachment))
            queue.remove(attachment);
    }

    public static boolean isOverMaxUploadFileSize(File file, boolean showErrorToast){
        if (file.length() > Constant.MAX_UPLOAD_FILE_SIZE) {
            if (showErrorToast)
                Utils.showMessage(StreamChat.getContext(), StreamChat.getStrings().get(R.string.stream_large_size_file_error));
            return true;
        }
        return false;
    }
}
