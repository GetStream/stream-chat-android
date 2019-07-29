package com.getstream.sdk.chat.function;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;

import com.getstream.sdk.chat.interfaces.MessageSendListener;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Message;
import com.getstream.sdk.chat.rest.apimodel.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.apimodel.response.ChannelResponse;
import com.getstream.sdk.chat.rest.apimodel.response.MessageResponse;
import com.getstream.sdk.chat.utils.Global;

import java.util.List;

public class MessageFunction {

    private final String TAG = MessageFunction.class.getSimpleName();

    private ChannelResponse channelResponse;

    public MessageFunction(ChannelResponse channelResponse) {
        this.channelResponse = channelResponse;
    }

    // region Message
    public void sendMessage(String text,
                            @Nullable Message parentMessage,
                            @Nullable List<Attachment> attachments,
                            final MessageSendListener sendListener) {

        String parentId = (parentMessage != null) ? parentMessage.getId() : null;

        SendMessageRequest request = new SendMessageRequest(this.channelResponse, text, attachments, parentId, false);
        Global.mRestController.sendMessage(channelResponse.getChannel().getId(), request,
                (MessageResponse response) -> sendListener.onSuccess(response),
                (String errMsg, int errCode) -> sendListener.onFailed(errMsg, errCode));
    }

    public void updateMessage(String text,
                              Message message,
                              @Nullable List<Attachment> attachments,
                              final MessageSendListener sendListener) {
        if (message == null) return;
        message.setText(text);
        UpdateMessageRequest request = new UpdateMessageRequest(message);

        Global.mRestController.updateMessage(message.getId(), request,
                (MessageResponse response) -> sendListener.onSuccess(response),
                (String errMsg, int errCode) -> sendListener.onFailed(errMsg, errCode));
    }

    public void deleteMessage(@NonNull final EditText et_message,
                              @NonNull Message message,
                              final MessageSendListener sendListener) {
        message.setText(et_message.getText().toString());
        Global.mRestController.deleteMessage(message.getId(),
                (MessageResponse response) -> sendListener.onSuccess(response),
                (String errMsg, int errCode) -> sendListener.onFailed(errMsg, errCode)
        );
    }
    // endregion
}
