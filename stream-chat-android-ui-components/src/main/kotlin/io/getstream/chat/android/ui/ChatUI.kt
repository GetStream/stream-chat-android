package io.getstream.chat.android.ui

import android.content.Context
import io.getstream.chat.android.ui.avatar.AvatarBitmapFactory
import io.getstream.chat.android.ui.common.UrlSigner
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.common.markdown.ChatMarkdownImpl
import io.getstream.chat.android.ui.common.navigation.ChatNavigator
import io.getstream.chat.android.ui.common.style.ChatFonts
import io.getstream.chat.android.ui.common.style.ChatFontsImpl
import io.getstream.chat.android.ui.common.style.ChatStyle

/**
 * ChatUI handles any configuration for the Chat UI elements.
 *
 * @param fonts allows you to overwrite fonts
 * @param markdown interface to to customize the markdown parsing behaviour, useful if you want to use more markdown modules
 * @param urlSigner url signing logic, enables you to add authorization tokens for images, video etc
 * @param avatarBitmapFactory allows you to generate custom bitmap for avatarView
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

    private var avatarBitmapFactoryOverride: AvatarBitmapFactory? = null
    private val defaultAvatarBitmapFactory: AvatarBitmapFactory by lazy { AvatarBitmapFactory(appContext) }
    public var avatarBitmapFactory: AvatarBitmapFactory
        get() = avatarBitmapFactoryOverride ?: defaultAvatarBitmapFactory
        set(value) {
            avatarBitmapFactoryOverride = value
        }

    private var supportedReactionsOverride: SupportedReactions? = null
    private val defaultSupportedReactions: SupportedReactions by lazy { SupportedReactions(appContext) }
    public var supportedReactions: SupportedReactions
        get() = supportedReactionsOverride ?: defaultSupportedReactions
        set(value) {
            supportedReactionsOverride = value
        }
}
