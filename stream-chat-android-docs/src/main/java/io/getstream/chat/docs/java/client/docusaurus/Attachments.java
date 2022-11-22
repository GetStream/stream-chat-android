package io.getstream.chat.docs.java.client.docusaurus;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.Attachment;
import io.getstream.chat.android.models.Message;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/sending-custom-attachments/">Sending Attachments</a>
 */
public class Attachments {

    public void sendAttachment() {
        Attachment attachment = new Attachment();
        Message message = new Message();
        message.setCid("messaging:general");
        message.setText("Look at this attachment!");
        message.setAttachments(Collections.singletonList(attachment));

        ChatClient.instance().sendMessage("messaging", "general", message, false).enqueue(result -> {
                    if (result.isSuccess()) {
                        // Handle success
                    } else {
                        // Handle error
                    }
                }
        );
    }

    public void attachmentWithoutFile() {
        Attachment attachment = new Attachment();
        // 1
        attachment.setType("location");
        // 2
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("lat", 40.017985);
        extraData.put("lon", -105.280184);
        attachment.setExtraData(extraData);
    }

    public void attachmentWithFile() {
        Attachment attachment = new Attachment();
        // 1
        attachment.setType("audio");
        // 2
        attachment.setUpload(new File("audio-file.mp3"));
    }
}
