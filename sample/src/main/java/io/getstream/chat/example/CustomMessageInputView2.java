package io.getstream.chat.example;

import android.content.Context;
import android.util.AttributeSet;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.view.MessageInputView;

import java.util.HashMap;

public class CustomMessageInputView2 extends MessageInputView {
    public CustomMessageInputView2(Context context) {
        super(context);
    }

    public CustomMessageInputView2(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public Message prepareMessage(String input) {
        Message m = super.prepareMessage(input);
        // note that you typically want to use custom fields on attachments instead of messages
        // attachment UI is easier to customize than the message UI
        HashMap<String, Object> extraData = new HashMap<>();
        extraData.put("mycustomfield", "123");
        m.setExtraData(extraData);

        return m;
    }
}
