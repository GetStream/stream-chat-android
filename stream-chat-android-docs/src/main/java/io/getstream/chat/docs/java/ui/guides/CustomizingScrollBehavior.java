package io.getstream.chat.docs.java.ui.guides;

import io.getstream.chat.android.ui.helper.TransformStyle;

/**
 * [Customizing MessageListView Scrolling Behavior](https://getstream.io/chat/docs/sdk/android/ui/guides/customizing-message-list-scroll-behavior/)
 */
public class CustomizingScrollBehavior {

    public void customization() {
        TransformStyle.setMessageListStyleTransformer(defaultViewStyle -> {
            // Customize the style
            return defaultViewStyle;
        });
    }
}
