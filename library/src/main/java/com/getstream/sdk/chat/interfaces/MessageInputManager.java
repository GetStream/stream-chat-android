package com.getstream.sdk.chat.interfaces;
import com.getstream.sdk.chat.rest.Message;

public interface MessageInputManager {

    void onSendMessageSuccess(Message message);

    void onSendMessageError(String errMsg);

    void onEditMessage(Message message);
}
