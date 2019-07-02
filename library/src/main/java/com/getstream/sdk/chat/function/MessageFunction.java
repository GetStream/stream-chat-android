package com.getstream.sdk.chat.function;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.EditText;

import com.getstream.sdk.chat.interfaces.MessageSendListener;
import com.getstream.sdk.chat.model.Device;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.message.Attachment;
import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.rest.apimodel.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.MessageResponse;
import com.getstream.sdk.chat.rest.controller.RestController;
import com.getstream.sdk.chat.utils.Global;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageFunction {

    private final String TAG = MessageFunction.class.getSimpleName();
    private ChannelResponse channelResponse;

    public MessageFunction(ChannelResponse channelResponse) {
        this.channelResponse = channelResponse;
    }

    boolean isSendLock = false;

    // region Message
    public void sendMessage(String text, @Nullable Message parentMessage, @Nullable List<Attachment> attachments, final MessageSendListener sendListener) {
        if (isSendLock) return;
        String parentId = (parentMessage != null) ? parentMessage.getId() : null;
        isSendLock = true;

        SendMessageRequest request = new SendMessageRequest(this.channelResponse, text, attachments, parentId, false);
        Global.mRestController.sendMessage(channelResponse.getChannel().getId(), request, (MessageResponse response) -> {
            sendListener.onSuccess(response);
            isSendLock = false;
        }, (String errMsg, int errCode) -> {
            sendListener.onFailed(errMsg, errCode);
            isSendLock = false;
        });

    }

    public void updateMessage(String text, Message message, @Nullable List<Attachment> attachments, final MessageSendListener sendListener) {
        if (message == null) return;
        if (isSendLock) return;
        isSendLock = true;
        message.setText(text);
        UpdateMessageRequest request = new UpdateMessageRequest(message);

        RestController.SendMessageCallback callback = (MessageResponse response) -> {
            Log.d(TAG, "Message Edited!");
            sendListener.onSuccess(response);
            isSendLock = false;
        };

        Global.mRestController.updateMessage(message.getId(), request, callback, (String errMsg, int errCode) -> {
            sendListener.onFailed(errMsg, errCode);
            isSendLock = false;
        });
        Log.d(TAG, "Message Editing...");
    }

    public void deleteMessage(@NonNull final EditText et_message, @NonNull Message message) {
        message.setText(et_message.getText().toString());
        Log.d(TAG, "Message Deleting...");
        RestController.SendMessageCallback callback = (MessageResponse response) -> {
            Log.d(TAG, "Message Deleted!");
            Message message1 = response.getMessage();
            et_message.setText("");
        };
        Global.mRestController.deleteMessage(message.getId(), callback, (String errMsg, int errCode) -> {
            Log.d(TAG, "Failed DeleteMessage : " + errMsg);
        });
    }
    // endregion
}
