package com.getstream.sdk.chat.function;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;

import com.getstream.sdk.chat.model.Device;
import com.getstream.sdk.chat.model.message.Attachment;
import com.getstream.sdk.chat.model.message.Message;
import com.getstream.sdk.chat.rest.apimodel.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.apimodel.response.MessageResponse;
import com.getstream.sdk.chat.rest.controller.RestController;
import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.Push;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageFunction {
    private final String TAG = MessageFunction.class.getSimpleName();
    private ChannelResponse channelResponse;

    public interface MessageSendListener {
        void onSuccess(MessageResponse response);

        void onFailed(String errMsg, int errCode);
    }

    public MessageFunction(ChannelResponse channelResponse) {
        this.channelResponse = channelResponse;
    }

    boolean isSendLock = false;

    // region Message
    public void sendMessage(String text, @Nullable Message parentMessage, @Nullable List<Attachment> attachments, final MessageSendListener sendListener) {
        if (isSendLock) return;
        String parentId = (parentMessage != null) ? parentMessage.getId() : null;
        isSendLock = true;
        sendNotification(text);

        SendMessageRequest request = new SendMessageRequest(this.channelResponse, text, attachments, parentId, false);
        RestController.SendMessageCallback callback = (MessageResponse response) -> {
            sendListener.onSuccess(response);
            isSendLock = false;
        };
        Global.mRestController.sendMessage(channelResponse.getChannel().getId(), request, callback, (String errMsg, int errCode) -> {
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

    private void sendNotification(String text) {
        String opponetId = Global.getOpponentId(channelResponse);
        if (TextUtils.isEmpty(opponetId)) return;

        Map<String, String> map = new HashMap<>();
        map.put("user_id", Global.getOpponentId(channelResponse));
        Global.mRestController.getDevices(map, (GetDevicesResponse response) -> {
            List<Device> devices = response.getDevices();
            Log.d(TAG, "Devices: " + devices);
            String title = Global.streamChat.getUser().getName() + " sent you a text message.";
            Push.sendPushNotification2(devices.get(0).getId(), title, text);
        }, (String errMsg, int errCode) -> {
            Log.d(TAG, "Failed get Devices");
        });
    }
    // endregion
}
