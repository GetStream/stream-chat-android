package com.getstream.sdk.chat.interfaces;
import com.getstream.sdk.chat.rest.Message;

public interface MessageSendManager {

    void onSendMessageSuccess(Message message);

    void onSendMessageError(String errMsg);
}
