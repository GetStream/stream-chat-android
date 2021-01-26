package io.getstream.chat.android.ui.messages.customization.dsl.viewholder

import io.getstream.chat.android.ui.messages.customization.viewholder.PlainTextViewHolderConfig

public class PlainTextViewHolderConfigBuilder {
    private var textSizeSp: Float = DEFAULT_TEXT_SIZE_SP
    private var textLineHeight: Float = DEFAULT_TEXT_LINE_HEIGHT_SP

    public fun textSize(textSizeProvider: () -> Float) {
        textSizeSp = textSizeProvider()
    }

    public fun textLineHeight(lineHeightProvider: () -> Float) {
        textLineHeight = lineHeightProvider()
    }

    public fun build(): PlainTextViewHolderConfig = PlainTextViewHolderConfig(textSizeSp, textLineHeight)

    private companion object {
        private const val DEFAULT_TEXT_SIZE_SP = 14f
        private const val DEFAULT_TEXT_LINE_HEIGHT_SP = 16f
    }
}
