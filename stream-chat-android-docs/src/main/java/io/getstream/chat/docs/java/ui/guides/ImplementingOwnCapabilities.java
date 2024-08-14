package io.getstream.chat.docs.java.ui.guides;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.getstream.chat.android.models.ChannelCapabilities;
import io.getstream.chat.android.ui.common.state.messages.MessageMode;
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState;
import io.getstream.chat.android.ui.feature.messages.composer.MessageComposerView;

/**
 * [Implementing Own Capabilities](https://getstream.io/chat/docs/sdk/android/ui/guides/implementing-own-capabilities/)
 */
public class ImplementingOwnCapabilities {

    private MessageComposerView messageComposerView;

    public void customCapabilities() {
        Set<String> customOwnCapabilities = new HashSet<>();
        customOwnCapabilities.add(ChannelCapabilities.SEND_MESSAGE);
        customOwnCapabilities.add(ChannelCapabilities.SEND_LINKS);
        customOwnCapabilities.add(ChannelCapabilities.UPLOAD_FILE);
        customOwnCapabilities.add(ChannelCapabilities.SEND_TYPING_EVENTS);
        customOwnCapabilities.add(ChannelCapabilities.TYPING_EVENTS);

        MessageComposerState messageComposerState = new MessageComposerState(
                "",
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                0,
                MessageMode.Normal.INSTANCE,
                false,
                customOwnCapabilities,
                false
        );

        messageComposerView.renderState(messageComposerState);
    }
}
