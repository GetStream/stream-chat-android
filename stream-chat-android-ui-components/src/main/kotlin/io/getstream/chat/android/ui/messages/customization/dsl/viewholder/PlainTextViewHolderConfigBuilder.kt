package io.getstream.chat.android.ui.messages.customization.dsl.viewholder

import io.getstream.chat.android.ui.messages.customization.dsl.CustomizationDsl
import io.getstream.chat.android.ui.messages.customization.viewholder.PlainTextViewHolderConfig

@CustomizationDsl
public class PlainTextViewHolderConfigBuilder {
    private var textSizeSp: Float = DEFAULT_TEXT_SIZE_SP
    private var textLineHeight: Float = DEFAULT_TEXT_LINE_HEIGHT_SP

    @CustomizationDsl
    public fun textSize(textSizeProvider: () -> Float) {
        textSizeSp = textSizeProvider()
    }

    @CustomizationDsl
    public fun textSize(textSize: Float): PlainTextViewHolderConfigBuilder {
        textSizeSp = textSize
        return this
    }

    @CustomizationDsl
    public fun textLineHeight(lineHeightProvider: () -> Float) {
        textLineHeight = lineHeightProvider()
    }

    @CustomizationDsl
    public fun textLineHeight(textLineHeight: Float): PlainTextViewHolderConfigBuilder {
        this.textLineHeight = textLineHeight
        return this
    }

    public fun build(): PlainTextViewHolderConfig = PlainTextViewHolderConfig(textSizeSp, textLineHeight)

    private companion object {
        private const val DEFAULT_TEXT_SIZE_SP = 14f
        private const val DEFAULT_TEXT_LINE_HEIGHT_SP = 16f
    }
}
