package io.getstream.chat.android.ui.messages.customization.dsl

import io.getstream.chat.android.ui.messages.customization.MessageListViewConfig
import io.getstream.chat.android.ui.messages.customization.dsl.viewholder.ViewHolderConfigBuilder
import io.getstream.chat.android.ui.messages.customization.viewholder.ViewHolderConfig

@CustomizationDsl
public class MessageListViewConfigBuilder {
    private var viewHolderConfig: ViewHolderConfig = ViewHolderConfig.DEFAULT_CONFIG

    @CustomizationDsl
    public fun viewHolders(builder: ViewHolderConfigBuilder.() -> Unit) {
        viewHolderConfig = ViewHolderConfig(builder)
    }

    @CustomizationDsl
    public fun build(): MessageListViewConfig {
        return MessageListViewConfig(viewHolders = viewHolderConfig)
    }
}
