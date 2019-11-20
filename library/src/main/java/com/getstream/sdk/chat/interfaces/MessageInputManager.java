package com.getstream.sdk.chat.interfaces;

import android.content.Intent;

import com.getstream.sdk.chat.rest.Message;

public interface MessageInputManager {
    void onSendMessageSuccess(Message message);
    void onSendMessageError(String errMsg);
    void onAddAttachments();
    void openPermissionRequest();
    void openCameraView(Intent intent, int REQUEST_CODE);
}
