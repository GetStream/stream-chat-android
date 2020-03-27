package com.getstream.sdk.chat.interfaces;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.models.Message;

/**
 * Interface for Send Message
 */
public interface MessageSendListener {

    void onSendMessageSuccess(Message message);

    void onSendMessageError(ChatError error);
}
