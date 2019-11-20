package com.getstream.sdk.chat.interfaces;

import android.content.Intent;

import com.getstream.sdk.chat.rest.Message;

public interface StreamMessageInputManager {
    void onSendMessageSuccess(Message message);
    void onEditMessage(Message message);
    void onSendMessageError(String errMsg);
}
