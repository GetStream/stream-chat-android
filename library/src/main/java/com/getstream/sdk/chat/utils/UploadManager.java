package com.getstream.sdk.chat.utils;

import android.content.Context;

import com.getstream.sdk.chat.R;
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
    private Channel channel;
    private Context context;
    private List<Attachment> queue = new ArrayList<>();

    public UploadManager(Channel channel, Context context) {
        this.channel = channel;
        this.context = context;
    }

    public void uploadFile(Attachment attachment, boolean isMedia, UploadFileListener fileListener) {

        String mimeType = attachment.getMime_type();

        updateQueue(attachment, true);
        UploadFileCallback callback = getUploadFileCallBack(attachment, isMedia, fileListener);
        if (isMedia && attachment.getType().equals(ModelType.attach_image))
            channel.sendImage(attachment.config.getFilePath(), mimeType, callback);
        else
            channel.sendFile(attachment.config.getFilePath(), mimeType, callback);
    }

    private UploadFileCallback getUploadFileCallBack(Attachment attachment,
                                                     boolean isMedia,
                                                     UploadFileListener fileListener) {
        return new UploadFileCallback<UploadFileResponse, Integer>() {
            @Override
            public void onSuccess(UploadFileResponse response) {
                fileUploadSuccess(attachment, response, isMedia, fileListener);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                fileUploadFailed(attachment, errMsg, fileListener);
            }

            @Override
            public void onProgress(Integer percentage) {
                fileUploading(attachment, percentage, fileListener);
            }
        };
    }

    private void fileUploadSuccess(Attachment attachment,
                                   UploadFileResponse response,
                                   boolean isMedia,
                                   UploadFileListener fileListener) {
        updateQueue(attachment,false);
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
        fileListener.onSuccess(attachment);

    }

    private void fileUploadFailed(Attachment attachment,
                                  String errMsg,
                                  UploadFileListener fileListener) {
        Utils.showMessage(context, errMsg);
        fileListener.onFailed(attachment);
    }

    private void fileUploading(Attachment attachment,
                               Integer percentage,
                               UploadFileListener fileListener) {
        attachment.config.setProgress(percentage);
        fileListener.onProgress(attachment);
    }

    public void updateQueue(Attachment attachment, boolean add){
        if (add){
            queue.add(attachment);
        }else {
            queue.remove(attachment);
        }
    }

    public boolean isUploadingFile() {
        return queue != null && !queue.isEmpty();
    }

    public void resetQueue() {
        this.queue.clear();
    }

    public static boolean isOverMaxUploadFileSize(File file, boolean showErrorToast){
        if (file.length() > Constant.MAX_UPLOAD_FILE_SIZE) {
            if (showErrorToast)
                Utils.showMessage(StreamChat.getContext(), StreamChat.getStrings().get(R.string.stream_large_size_file_error));
            return true;
        }
        return false;
    }

    interface UploadFileListener{
        void onSuccess(Attachment attachment);
        void onFailed(Attachment attachment);
        void onProgress(Attachment attachment);
    }
}
