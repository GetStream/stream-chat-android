package io.getstream.chat.android.ui.messages.customization.dsl.viewholder

import io.getstream.chat.android.ui.messages.customization.viewholder.PlainTextViewHolderConfig
import io.getstream.chat.android.ui.messages.customization.viewholder.ViewHolderConfig

public class ViewHolderConfigBuilder {

    private var plainTextConfig: PlainTextViewHolderConfig = PlainTextViewHolderConfig.DEFAULT_CONFIG

    public fun plainText(builder: PlainTextViewHolderConfigBuilder.() -> Unit) {
        plainTextConfig = PlainTextViewHolderConfig(builder)
    }

    public fun build(): ViewHolderConfig {
        return ViewHolderConfig(plainTextConfig = plainTextConfig)
    }
}
