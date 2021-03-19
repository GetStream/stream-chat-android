package io.getstream.chat.android.ui

import android.content.Context
import com.getstream.sdk.chat.style.ChatFonts
import com.getstream.sdk.chat.style.ChatFontsImpl
import com.getstream.sdk.chat.style.ChatStyle
import com.getstream.sdk.chat.utils.strings.ChatStrings
import com.getstream.sdk.chat.utils.strings.ChatStringsImpl
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.android.ui.common.BuildConfig
import io.getstream.chat.android.ui.common.UrlSigner
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.common.markdown.ChatMarkdownImpl

/**
 * ChatUI handles any configuration for the Chat UI elements. It replaces the older Chat class.
 *
 * @param fonts allows you to overwrite fonts
 * @param strings allows you to customize strings
 * @param markdown interface to to customize the markdown parsing behaviour, useful if you want to use more markdown modules
 * @param urlSigner url signing logic, enables you to add authorization tokens for images, video etc
 *
 * @see ChatMarkdown
 * @see UrlSigner
 * @see ChatStrings
 * @see ChatFonts
 */
public class ChatUI internal constructor(
    public val fonts: ChatFonts,
    public val strings: ChatStrings,
    public val markdown: ChatMarkdown,
    public val urlSigner: UrlSigner
) {
    public val version: String
        get() = BuildConfig.BUILD_TYPE + ":" + STREAM_CHAT_VERSION

    public class Builder(private val appContext: Context) {
        private var style: ChatStyle? = null
        private var urlSigner: UrlSigner? = null
        private var markdown: ChatMarkdown? = null
        private var fonts: ChatFonts? = null
        private var strings: ChatStrings? = null

        public fun withStyle(style: ChatStyle): Builder = apply {
            this.style = style
        }

        public fun withMarkdown(markdown: ChatMarkdown): Builder = apply {
            this.markdown = markdown
        }

        public fun withUrlSigner(signer: UrlSigner): Builder = apply {
            this.urlSigner = signer
        }

        public fun withFonts(fonts: ChatFonts): Builder = apply {
            this.fonts = fonts
        }

        public fun withStrings(strings: ChatStrings): Builder = apply {
            this.strings = strings
        }

        public fun build(): ChatUI {
            val chatStyle = style ?: ChatStyle.Builder().build()
            instance = ChatUI(
                fonts ?: ChatFontsImpl(chatStyle, appContext),
                strings ?: ChatStringsImpl(appContext),
                markdown ?: ChatMarkdownImpl(appContext),
                urlSigner ?: UrlSigner.DefaultUrlSigner()
            )
            return instance()
        }
    }

    public companion object {
        private var instance: ChatUI? = null

        @JvmStatic
        public fun instance(): ChatUI {
            return checkNotNull(instance) { "Be sure to call ChatUI.Builder().build() before using ChatUI.instance()" }
        }
    }
}
