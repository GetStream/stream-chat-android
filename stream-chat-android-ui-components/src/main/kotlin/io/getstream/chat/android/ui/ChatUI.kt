/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui

import android.content.Context
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.helper.DefaultDownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DefaultVideoHeadersProvider
import io.getstream.chat.android.ui.common.helper.DownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DownloadRequestInterceptor
import io.getstream.chat.android.ui.common.helper.DurationFormatter
import io.getstream.chat.android.ui.common.helper.ImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.ImageHeadersProvider
import io.getstream.chat.android.ui.common.helper.ReactionPushEmojiFactory
import io.getstream.chat.android.ui.common.helper.VideoHeadersProvider
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing
import io.getstream.chat.android.ui.common.utils.ChannelNameFormatter
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.DefaultQuotedAttachmentMessageFactory
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.DecoratorProviderFactory
import io.getstream.chat.android.ui.font.ChatFonts
import io.getstream.chat.android.ui.font.ChatFontsImpl
import io.getstream.chat.android.ui.font.ChatStyle
import io.getstream.chat.android.ui.helper.CurrentUserProvider
import io.getstream.chat.android.ui.helper.MessagePreviewFormatter
import io.getstream.chat.android.ui.helper.MimeTypeIconProvider
import io.getstream.chat.android.ui.helper.MimeTypeIconProviderImpl
import io.getstream.chat.android.ui.helper.SupportedReactions
import io.getstream.chat.android.ui.helper.transformer.AutoLinkableTextTransformer
import io.getstream.chat.android.ui.helper.transformer.ChatMessageTextTransformer
import io.getstream.chat.android.ui.navigation.ChatNavigator
import io.getstream.chat.android.ui.utils.extensions.getToggleableTranslatedText
import io.getstream.chat.android.ui.utils.lazyVar
import io.getstream.chat.android.ui.widgets.avatar.ChannelAvatarRenderer
import io.getstream.chat.android.ui.widgets.avatar.UserAvatarRenderer

/**
 * ChatUI handles any configuration for the Chat UI elements.
 *
 * @see ChatMessageTextTransformer
 * @see ChatFonts
 * @see ImageHeadersProvider
 */
public object ChatUI {
    internal lateinit var appContext: Context

    @JvmStatic
    public var style: ChatStyle = ChatStyle()

    /**
     * A class responsible for handling navigation to chat destinations. Allows overriding
     * a default navigation between chat components.
     */
    @JvmStatic
    public var navigator: ChatNavigator = ChatNavigator()

    /**
     * Provides a custom implementation for loading images.
     */
    @JvmStatic
    public var imageAssetTransformer: ImageAssetTransformer by StreamImageLoader.instance()::imageAssetTransformer

    /**
     * Generates a download URI for the given attachment.
     */
    @JvmStatic
    public var downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator = DefaultDownloadAttachmentUriGenerator

    /**
     * Intercepts and modifies the download request before it is enqueued.
     */
    @JvmStatic
    public var downloadRequestInterceptor: DownloadRequestInterceptor = DownloadRequestInterceptor { }

    /**
     * Provides HTTP headers for image loading requests.
     */
    @JvmStatic
    public var imageHeadersProvider: ImageHeadersProvider by StreamImageLoader.instance()::imageHeadersProvider

    /**
     * Provides HTTP headers for video loading requests.
     */
    @JvmStatic
    public var videoHeadersProvider: VideoHeadersProvider = DefaultVideoHeadersProvider

    /**
     * Allows setting default fonts used by UI components.
     */
    @JvmStatic
    public var fonts: ChatFonts by lazyVar { ChatFontsImpl(style, appContext) }

    /**
     * Allows customising the message text's format or style.
     *
     * For example, it can be used to provide markdown support in chat or it can be used
     * to highlight specific messages by making them bold etc.
     */
    @JvmStatic
    public var messageTextTransformer: ChatMessageTextTransformer by lazyVar {
        AutoLinkableTextTransformer { textView, messageItem ->
            // Customize the transformer if needed
            val displayedText = messageItem.message.getToggleableTranslatedText(messageItem.showOriginalText)
            textView.text = displayedText
        }
    }

    /**
     * Allows overriding default set of message reactions available.
     */
    @JvmStatic
    public var supportedReactions: SupportedReactions by lazyVar { SupportedReactions(appContext) }

    /**
     * Allows overriding default emojis for reactions in push notifications.
     */
    @JvmStatic
    public var reactionPushEmojiFactory: ReactionPushEmojiFactory by lazyVar {
        ReactionPushEmojiFactory.defaultFactory()
    }

    /**
     * Allows overriding default icons for attachments MIME types.
     */
    @JvmStatic
    public var mimeTypeIconProvider: MimeTypeIconProvider by lazyVar { MimeTypeIconProviderImpl() }

    /**
     * Allows to generate a name for the given channel.
     */
    @JvmStatic
    public var channelNameFormatter: ChannelNameFormatter by lazyVar {
        ChannelNameFormatter.defaultFormatter(appContext)
    }

    /**
     *  Allows to generate a preview text for the given message.
     */
    @JvmStatic
    public var messagePreviewFormatter: MessagePreviewFormatter by lazyVar {
        MessagePreviewFormatter.defaultFormatter(appContext)
    }

    /**
     * Allows formatting date-time objects as strings.
     */
    @JvmStatic
    public var dateFormatter: DateFormatter by lazyVar { DateFormatter.from(appContext) }

    /**
     * Allows formatting durations as strings.
     */
    @JvmStatic
    public var durationFormatter: DurationFormatter by lazyVar { DurationFormatter.defaultFormatter() }

    /**
     * Allows adding support for custom attachments in the message list.
     */
    @JvmStatic
    public var decoratorProviderFactory: DecoratorProviderFactory by lazyVar { DecoratorProviderFactory.defaultFactory() }

    /**
     * Allows adding support for custom attachments in the message list.
     */
    @JvmStatic
    public var attachmentFactoryManager: AttachmentFactoryManager by lazyVar { AttachmentFactoryManager() }

    /**
     * Allows adding support for custom attachments in the preview section of the message composer.
     */
    @JvmStatic
    public var attachmentPreviewFactoryManager: AttachmentPreviewFactoryManager by lazyVar { AttachmentPreviewFactoryManager() }

    /**
     * Allows adding support for custom attachment inside quoted messages in the message list. If none are found here
     * will default to [attachmentFactoryManager].
     */
    @JvmStatic
    public var quotedAttachmentFactoryManager: QuotedAttachmentFactoryManager by lazyVar {
        QuotedAttachmentFactoryManager(
            listOf(DefaultQuotedAttachmentMessageFactory()),
        )
    }

    /**
     * Provides the currently logged in user.
     */
    @JvmStatic
    public var currentUserProvider: CurrentUserProvider = CurrentUserProvider.defaultCurrentUserProvider()

    /**
     * Whether thumbnails for video attachments will be displayed in previews.
     */
    @JvmStatic
    public var videoThumbnailsEnabled: Boolean = true

    /**
     * Whether draft messages are enabled.
     */
    @JvmStatic
    public var draftMessagesEnabled: Boolean = false

    /**
     * Sets the strategy for resizing images hosted on Stream's CDN. Disabled by default,
     * set [StreamCdnImageResizing.imageResizingEnabled] to true if you wish to enable resizing images. Note that
     * resizing applies only to images hosted on Stream's CDN which contain the original width (ow) and height (oh)
     * query parameters.
     */
    @JvmStatic
    public var streamCdnImageResizing: StreamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing()

    /**
     * Whether or not the auto-translation feature is enabled.
     */
    @JvmStatic
    public var autoTranslationEnabled: Boolean = false

    /**
     * Whether the option to show the original translation is enabled or not.
     */
    @JvmStatic
    public var showOriginalTranslationEnabled: Boolean = false

    /**
     * Provides a custom renderer for user avatars.
     */
    @JvmStatic
    public var userAvatarRenderer: UserAvatarRenderer? = null

    /**
     * Provides a custom renderer for channel avatars.
     */
    @JvmStatic
    public var channelAvatarRenderer: ChannelAvatarRenderer? = null
}
