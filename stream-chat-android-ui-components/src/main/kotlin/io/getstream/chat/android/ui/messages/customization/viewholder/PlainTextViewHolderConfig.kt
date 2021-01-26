package io.getstream.chat.android.ui.messages.customization.viewholder

import io.getstream.chat.android.ui.messages.customization.dsl.CustomizationDsl
import io.getstream.chat.android.ui.messages.customization.dsl.viewholder.PlainTextViewHolderConfigBuilder

public data class PlainTextViewHolderConfig(
    public val textSizeSp: Float,
    public val lineHeightSp: Float,
) {
    public companion object {
        public const val DEFAULT_TEXT_SIZE_SP: Float = 14f
        public const val DEFAULT_TEXT_LINE_HEIGHT_SP: Float = 16f

        public val DEFAULT_CONFIG: PlainTextViewHolderConfig =
            PlainTextViewHolderConfig(textSizeSp = DEFAULT_TEXT_SIZE_SP, lineHeightSp = DEFAULT_TEXT_LINE_HEIGHT_SP)

        @CustomizationDsl
        public operator fun invoke(builder: PlainTextViewHolderConfigBuilder.() -> Unit): PlainTextViewHolderConfig {
            return PlainTextViewHolderConfigBuilder().apply(builder).build()
        }
    }
}
