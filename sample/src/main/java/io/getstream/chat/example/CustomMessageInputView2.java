package io.getstream.chat.example;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.getstream.sdk.chat.interfaces.MessageSendListener;
import io.getstream.chat.android.client.models.Message;
import com.getstream.sdk.chat.view.MessageInputView;
import java.util.HashMap;


public class CustomMessageInputView2 extends MessageInputView implements MessageSendListener {

    final static String TAG = CustomMessageInputView2.class.getSimpleName();

    public CustomMessageInputView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        setMessageSendListener(this);
    }

    @Override
    public Message prepareNewMessage(Message message){
        Message preparedMessage = super.prepareNewMessage(message);
        // note that you typically want to use custom fields on attachments instead of messages
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("mycustomfield", "123");
        preparedMessage.setExtraData(extraData);
        return preparedMessage;
    }

    @Override
    public void onSendMessageSuccess(Message message) {
        Log.d(TAG, "Sent message! :" + message.getText());
    }

    @Override
    public void onSendMessageError(String errMsg) {
        Log.d(TAG, "Failed send message! :" + errMsg);
    }
}
