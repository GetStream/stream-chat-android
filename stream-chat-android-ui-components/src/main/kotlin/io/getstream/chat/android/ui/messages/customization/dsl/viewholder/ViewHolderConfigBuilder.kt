package io.getstream.chat.android.ui.messages.customization.dsl.viewholder

import io.getstream.chat.android.ui.messages.customization.dsl.CustomizationDsl
import io.getstream.chat.android.ui.messages.customization.viewholder.PlainTextViewHolderConfig
import io.getstream.chat.android.ui.messages.customization.viewholder.ViewHolderConfig

@CustomizationDsl
public class ViewHolderConfigBuilder {

    private var plainTextConfig: PlainTextViewHolderConfig = PlainTextViewHolderConfig.DEFAULT_CONFIG

    @CustomizationDsl
    public fun plainText(builder: PlainTextViewHolderConfigBuilder.() -> Unit) {
        plainTextConfig = PlainTextViewHolderConfig(builder)
    }

    @CustomizationDsl
    public fun build(): ViewHolderConfig {
        return ViewHolderConfig(plainTextConfig = plainTextConfig)
    }
}
