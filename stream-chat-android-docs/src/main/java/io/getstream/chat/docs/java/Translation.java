package io.getstream.chat.docs.java;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.client.models.Message;
import kotlin.Unit;

public class Translation {
    private ChatClient client;

    /**
     * @see <a href="https://getstream.io/chat/docs/translation/?language=java#message-translation-endpoint">Message Translation</a>
     */
    public void messageTranslation() {
        // Translate message to french
        ChannelClient channelController = client.channel("messaging:general");
        Message message = new Message();
        message.setText("Hello, I would like to have more information about your product.");
        String frenchLanguage = "fr";
        channelController.sendMessage(message).enqueue(result -> {
            String messageId = result.data().getId();
            client.translate(messageId, frenchLanguage).enqueue(translationResult -> {
                Message translatedMessage = translationResult.data();
                String translation = translatedMessage.getI18n().get(frenchLanguage);
                return Unit.INSTANCE;
            });
            return Unit.INSTANCE;
        });
    }
}
