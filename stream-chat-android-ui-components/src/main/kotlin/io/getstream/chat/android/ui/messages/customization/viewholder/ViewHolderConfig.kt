package io.getstream.chat.android.ui.messages.customization.viewholder

import io.getstream.chat.android.ui.messages.customization.dsl.viewholder.ViewHolderConfigBuilder

public class ViewHolderConfig(public val plainTextConfig: PlainTextViewHolderConfig) {

    public companion object {
        public val DEFAULT_CONFIG: ViewHolderConfig =
            ViewHolderConfig(plainTextConfig = PlainTextViewHolderConfig.DEFAULT_CONFIG)

        public operator fun invoke(builder: ViewHolderConfigBuilder.() -> Unit): ViewHolderConfig {
            return ViewHolderConfigBuilder().apply(builder).build()
        }
    }
}
