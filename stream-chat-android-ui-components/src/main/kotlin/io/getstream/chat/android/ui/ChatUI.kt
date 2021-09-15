package io.getstream.chat.android.ui

import android.content.Context
import com.getstream.sdk.chat.images.ImageHeadersProvider
import com.getstream.sdk.chat.images.StreamImageLoader
import io.getstream.chat.android.ui.avatar.AvatarBitmapFactory
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.common.markdown.ChatMarkdownImpl
import io.getstream.chat.android.ui.common.navigation.ChatNavigator
import io.getstream.chat.android.ui.common.style.ChatFonts
import io.getstream.chat.android.ui.common.style.ChatFontsImpl
import io.getstream.chat.android.ui.common.style.ChatStyle

/**
 * ChatUI handles any configuration for the Chat UI elements.
 *
 * @param fonts Allows setting default fonts used by UI components.
 * @param markdown Allows customizing the markdown parsing behaviour, e.g. useful if you want to use more markdown modules.
 * @param avatarBitmapFactory Allows intercepting and providing custom bitmap displayed with AvatarView.
 * @param navigator A class responsible for handling navigation to chat destinations. Allows overriding a default navigation between chat components.
 * @param supportedReactions Allows overriding default set of message reactions available.
 * @param mimeTypeIconProvider Allows overriding default icons for attachments MIME types.
 *
 * @see ChatMarkdown
 * @see ChatFonts
 * @see ImageHeadersProvider
 */
public object ChatUI {
    internal lateinit var appContext: Context

    public var style: ChatStyle = ChatStyle()
    public var navigator: ChatNavigator = ChatNavigator()
    public var imageHeadersProvider: ImageHeadersProvider
        get() = StreamImageLoader.instance().imageHeadersProvider
        set(value) {
            StreamImageLoader.instance().imageHeadersProvider = value
        }

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

    private var mimeTypeIconProviderOverride: MimeTypeIconProvider? = null
    private val defaultMimeTypeIconProvider: MimeTypeIconProvider by lazy { MimeTypeIconProviderImpl() }
    public var mimeTypeIconProvider: MimeTypeIconProvider
        get() = mimeTypeIconProviderOverride ?: defaultMimeTypeIconProvider
        set(value) {
            mimeTypeIconProviderOverride = value
        }

    public var uiMode: UiMode = UiMode.LIGHT
}
