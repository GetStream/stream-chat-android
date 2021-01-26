package io.getstream.chat.android.ui.messages.customization

import io.getstream.chat.android.ui.messages.customization.dsl.CustomizationDsl
import io.getstream.chat.android.ui.messages.customization.dsl.MessageListViewConfigBuilder
import io.getstream.chat.android.ui.messages.customization.viewholder.ViewHolderConfig

public data class MessageListViewConfig(public val viewHolders: ViewHolderConfig) {

    public companion object {
        public val DEFAULT_CONFIG: MessageListViewConfig =
            MessageListViewConfig(viewHolders = ViewHolderConfig.DEFAULT_CONFIG)

        @CustomizationDsl
        public operator fun invoke(builder: MessageListViewConfigBuilder.() -> Unit): MessageListViewConfig {
            return MessageListViewConfigBuilder().apply(builder).build()
        }
    }
}
