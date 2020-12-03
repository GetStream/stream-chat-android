package io.getstream.chat.docs.java;

import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.models.Attachment;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;
import kotlin.Unit;

public class Messages {
    static ChannelClient channelController;
    static Message message;

    public static void sendAMessage() {
        Message message = new Message();
        message.setText("Josh I told them I was pesca-pescatarian. Which is one who eats solely fish who eat other fish.");
        message.getExtraData().put("anotherCustomField", 234);

        // add an image attachment to the message
        Attachment attachment = new Attachment();
        attachment.setType("image");
        attachment.setImageUrl("https://bit.ly/2K74TaG");
        attachment.setFallback("test image");
        // add some custom data to the attachment
        attachment.getExtraData().put("myCustomField", 123);

        message.getAttachments().add(attachment);

        message.getMentionedUsers().add(new User());

        channelController.sendMessage(message).enqueue(result -> Unit.INSTANCE);
    }

    public static void getAMessage() {
        channelController.getMessage("messageId").enqueue(result -> {
            Message message = result.data();
            return Unit.INSTANCE;
        });
    }

    public static void updateAMessage() {
        message.setText("my updated text");
        channelController.updateMessage(message).enqueue(result -> {
            Message updatedMessage = result.data();
            return Unit.INSTANCE;
        });
    }

    public static void deleteAMessage() {
        channelController.deleteMessage("messageId").enqueue(messageResult -> Unit.INSTANCE);
    }
}
