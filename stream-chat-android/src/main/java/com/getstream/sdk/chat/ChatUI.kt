package com.getstream.sdk.chat

import com.getstream.sdk.chat.navigation.ChatNavigationHandler
import com.getstream.sdk.chat.navigation.ChatNavigator
import com.getstream.sdk.chat.navigation.ChatNavigatorImpl
import com.getstream.sdk.chat.style.ChatFonts
import com.getstream.sdk.chat.style.ChatFontsImpl
import com.getstream.sdk.chat.style.ChatStyle
import com.getstream.sdk.chat.utils.strings.ChatStrings
import com.getstream.sdk.chat.utils.strings.ChatStringsImpl
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.livedata.ChatDomain

/**
 * ChatUX handles any configuration for the Chat UI elements. It replaces the older Chat class.
 *
 * @param client the low level chat client
 * @param chatDomain the chat domain interface used for offline storage and state
 * @param fonts allows you to overwrite fonts
 * @param strings allows you to customize strings
 * @param navigationHandler navigation handler for customizing things such as the media browsing experience
 * @param markdown interface to to customize the markdown parsing behaviour, useful if you want to use more markdown modules
 * @param urlSigner url signing logic, enables you to add authorization tokens for images, video etc
 *
 * @see ChatMarkdown
 * @see UrlSigner
 * @see ChatStrings
 * @see ChatFonts
 */
public class ChatUI internal constructor(
    public val client: ChatClient,
    public val chatDomain: ChatDomain,
    public val fonts: ChatFonts,
    public val strings: ChatStrings,
    public val navigationHandler: ChatNavigationHandler? = null,
    public val markdown: ChatMarkdown,
    public val urlSigner: UrlSigner
) {
    public val version: String
        get() = BuildConfig.BUILD_TYPE + ":" + BuildConfig.STREAM_CHAT_UI_VERSION

    public val navigator: ChatNavigator = ChatNavigatorImpl()

    init {
        if (navigationHandler != null) {
            navigator.setHandler(navigationHandler)
        }
    }

    public data class Builder(
        private var client: ChatClient,
        private var chatDomain: ChatDomain
    ) {

        private val style = ChatStyle.Builder().build()
        private var navigationHandler: ChatNavigationHandler? = null
        private var urlSigner: UrlSigner = UrlSigner.DefaultUrlSigner()
        private var markdown: ChatMarkdown = ChatMarkdownImpl(client.appContext)
        private var fonts: ChatFonts = ChatFontsImpl(style, client.appContext)
        private var strings: ChatStrings = ChatStringsImpl(client.appContext)

        public fun withMarkdown(markdown: ChatMarkdown): Builder {
            this.markdown = markdown
            return this
        }

        public fun withUrlSigner(signer: UrlSigner): Builder {
            this.urlSigner = signer
            return this
        }

        public fun withNavigationHandler(handler: ChatNavigationHandler): Builder {
            this.navigationHandler = handler
            return this
        }

        public fun withFonts(fonts: ChatFonts): Builder {
            this.fonts = fonts
            return this
        }

        public fun withStrings(strings: ChatStrings): Builder {
            this.strings = strings
            return this
        }

        public fun build(): ChatUI {

            instance = ChatUI(client, chatDomain, fonts, strings, navigationHandler, markdown, urlSigner)
            return instance
        }
    }

    public companion object {
        private lateinit var instance: ChatUI

        @JvmStatic
        public fun instance(): ChatUI {
            return instance
        }
    }
}
