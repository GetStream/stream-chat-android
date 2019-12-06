package com.getstream.sdk.chat.interfaces;
import com.getstream.sdk.chat.rest.Message;

/**
 * Interface for Send Message
 */
public interface MessageSendListener {

    void onSendMessageSuccess(Message message);

    void onSendMessageError(String errMsg);
}
