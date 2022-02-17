@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.ui

import android.content.Context
import com.getstream.sdk.chat.images.ImageHeadersProvider
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.ui.avatar.AvatarBitmapFactory
import io.getstream.chat.android.ui.common.ChannelNameFormatter
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.common.navigation.ChatNavigator
import io.getstream.chat.android.ui.common.style.ChatFonts
import io.getstream.chat.android.ui.common.style.ChatFontsImpl
import io.getstream.chat.android.ui.common.style.ChatStyle
import io.getstream.chat.android.ui.transformer.AutoLinkableTextTransformer
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer
import io.getstream.chat.android.ui.utils.lazyVar

/**
 * ChatUI handles any configuration for the Chat UI elements.
 *
 * @see ChatMarkdown
 * @see ChatFonts
 * @see ImageHeadersProvider
 */
public object ChatUI {
    internal lateinit var appContext: Context

    public var style: ChatStyle = ChatStyle()

    /**
     * A class responsible for handling navigation to chat destinations. Allows overriding
     * a default navigation between chat components.
     */
    public var navigator: ChatNavigator = ChatNavigator()

    /**
     * Provides HTTP headers for image loading requests.
     */
    public var imageHeadersProvider: ImageHeadersProvider by StreamImageLoader.instance()::imageHeadersProvider

    /**
     * Allows setting default fonts used by UI components.
     */
    public var fonts: ChatFonts by lazyVar { ChatFontsImpl(style, appContext) }

    /**
     * Allows customizing the markdown parsing behaviour, e.g. useful if you want
     * to use more markdown modules.
     */
    @Deprecated(
        message = "ChatUI.markdown is deprecated. Markdown support is extracted into another module. " +
            "See docs for more reference",
        level = DeprecationLevel.ERROR,
        replaceWith = ReplaceWith(
            expression = "ChatUI.messageTextTransformer"
        )
    )
    public var markdown: ChatMarkdown by lazyVar {
        ChatMarkdown { textView, message ->
            textView.text = message
        }
    }

    /**
     * Allows customising the message text's format or style.
     *
     * For example, it can be used to provide markdown support in chat or it can be used
     * to highlight specific messages by making them bold etc.
     */
    public var messageTextTransformer: ChatMessageTextTransformer by lazyVar {
        AutoLinkableTextTransformer { textView, messageItem ->
            // Bypass to markdown by default for backwards compatibility.
            markdown.setText(textView, messageItem.message.text)
        }
    }

    /**
     * Allows intercepting and providing custom bitmap displayed with AvatarView.
     */
    public var avatarBitmapFactory: AvatarBitmapFactory by lazyVar { AvatarBitmapFactory(appContext) }

    /**
     * Allows overriding default set of message reactions available.
     */
    public var supportedReactions: SupportedReactions by lazyVar { SupportedReactions(appContext) }

    /**
     * Allows overriding default icons for attachments MIME types.
     */
    public var mimeTypeIconProvider: MimeTypeIconProvider by lazyVar { MimeTypeIconProviderImpl() }

    /**
     * Allows to generate a name for the given channel.
     */
    public var channelNameFormatter: ChannelNameFormatter by lazyVar {
        ChannelNameFormatter.defaultFormatter(appContext)
    }

    /**
     * Allows formatting date-time objects as strings.
     */
    public var dateFormatter: DateFormatter by lazyVar { DateFormatter.from(appContext) }
}
