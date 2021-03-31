package com.getstream.sdk.chat

import android.content.Context
import android.widget.TextView
import com.getstream.sdk.chat.navigation.ChatNavigationHandler
import com.getstream.sdk.chat.navigation.ChatNavigator
import com.getstream.sdk.chat.navigation.destinations.ChatDestination
import com.getstream.sdk.chat.utils.strings.ChatStrings
import com.getstream.sdk.chat.utils.strings.ChatStringsImpl
import io.getstream.chat.android.client.BuildConfig.STREAM_CHAT_VERSION
import io.getstream.chat.android.ui.common.BuildConfig
import io.getstream.chat.android.ui.common.style.ChatFonts
import io.getstream.chat.android.ui.common.style.ChatFontsImpl
import io.getstream.chat.android.ui.common.style.ChatStyle

/**
 * ChatUI handles any configuration for the Chat UI elements. It replaces the older Chat class.
 *
 * @param fonts allows you to overwrite fonts
 * @param strings allows you to customize strings
 * @param navigator allows you to customize things such as the media browsing experience
 * @param markdown interface to to customize the markdown parsing behaviour, useful if you want to use more markdown modules
 * @param urlSigner url signing logic, enables you to add authorization tokens for images, video etc
 *
 * @see ChatMarkdown
 * @see UrlSigner
 * @see ChatStrings
 * @see ChatFonts
 */
@Deprecated(
    level = DeprecationLevel.WARNING,
    message = "Use new ChatUI implementation",
    replaceWith = ReplaceWith("ChatUI", "io.getstream.chat.android.ui.ChatUI"),
)
public class ChatUI internal constructor(
    public val fonts: ChatFonts,
    public val strings: ChatStrings,
    public val navigator: ChatNavigator,
    public val markdown: ChatMarkdown,
    public val urlSigner: UrlSigner,
) {
    public val version: String
        get() = BuildConfig.BUILD_TYPE + ":" + STREAM_CHAT_VERSION

    public class Builder(private val appContext: Context) {

        private var style: ChatStyle? = null
        private var navigationHandler: ChatNavigationHandler? = null
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

        public fun withNavigationHandler(handler: ChatNavigationHandler): Builder = apply {
            this.navigationHandler = handler
        }

        public fun withFonts(fonts: ChatFonts): Builder = apply {
            this.fonts = fonts
        }

        public fun withStrings(strings: ChatStrings): Builder = apply {
            this.strings = strings
        }

        public fun build(): ChatUI {
            val chatStyle = style ?: ChatStyle()
            val fakeNavigator: ChatNavigator = ChatNavigator { }
            val fakeMarkdown: ChatMarkdown = ChatMarkdown { _, s -> }
            instance = ChatUI(
                fonts ?: ChatFontsImpl(chatStyle, appContext),
                strings ?: ChatStringsImpl(appContext),
                fakeNavigator,
                fakeMarkdown,
                urlSigner ?: UrlSigner.DefaultUrlSigner()
            )

            // override default props of new ChatUI object
            fonts?.let {
                io.getstream.chat.android.ui.ChatUI.fonts = it
            }
            markdown?.let {
                io.getstream.chat.android.ui.ChatUI.markdown = object :
                    ChatMarkdown,
                    io.getstream.chat.android.ui.common.markdown.ChatMarkdown {
                    override fun setText(textView: TextView, text: String) = it.setText(textView, text)
                }
            }
            urlSigner?.let {
                io.getstream.chat.android.ui.ChatUI.urlSigner = object :
                    UrlSigner,
                    io.getstream.chat.android.ui.common.UrlSigner {
                    override fun signFileUrl(url: String): String = it.signFileUrl(url)
                    override fun signImageUrl(url: String): String = it.signImageUrl(url)
                }
            }
            navigationHandler?.let {
                val handler = object : ChatNavigationHandler {
                    override fun navigate(destination: ChatDestination): Boolean = it.navigate(destination)
                }
                io.getstream.chat.android.ui.ChatUI.navigator =
                    io.getstream.chat.android.ui.common.navigation.ChatNavigator(handler)
            }

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
