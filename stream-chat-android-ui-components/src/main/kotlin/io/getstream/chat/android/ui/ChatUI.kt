package io.getstream.chat.android.ui

import android.content.Context
import com.getstream.sdk.chat.style.ChatStyle
import io.getstream.chat.android.ui.common.UrlSigner
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.common.markdown.ChatMarkdownImpl
import io.getstream.chat.android.ui.common.navigation.ChatNavigator
import io.getstream.chat.android.ui.common.style.ChatFonts
import io.getstream.chat.android.ui.common.style.ChatFontsImpl

/**
 * ChatUI handles any configuration for the Chat UI elements. It replaces the older Chat class.
 *
 * @param fonts allows you to overwrite fonts
 * @param markdown interface to to customize the markdown parsing behaviour, useful if you want to use more markdown modules
 * @param urlSigner url signing logic, enables you to add authorization tokens for images, video etc
 *
 * @see ChatMarkdown
 * @see UrlSigner
 * @see ChatFonts
 */
public object ChatUI {
    internal lateinit var appContext: Context

    public var style: ChatStyle = ChatStyle()
    public var navigator: ChatNavigator = ChatNavigator()
    public var urlSigner: UrlSigner = UrlSigner.DefaultUrlSigner()

    private var fontsOverride: ChatFonts? = null
    private val defaultFonts: ChatFonts by lazy { ChatFontsImpl(style, appContext) }
    public var fonts: ChatFonts
        get() = fontsOverride ?: defaultFonts
        set(value) {
            fontsOverride = value
        }

    private var markdownOverride: ChatMarkdown? = null
    private val defaultMarkdown: ChatMarkdown by lazy { ChatMarkdownImpl(appContext) }
    public var markdown: ChatMarkdown
        get() = markdownOverride ?: defaultMarkdown
        set(value) {
            markdownOverride = value
        }
}
