/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import com.getstream.sdk.chat.images.ImageHeadersProvider
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.ui.avatar.AvatarBitmapFactory
import io.getstream.chat.android.ui.common.ChannelNameFormatter
import io.getstream.chat.android.ui.common.navigation.ChatNavigator
import io.getstream.chat.android.ui.common.style.ChatFonts
import io.getstream.chat.android.ui.common.style.ChatFontsImpl
import io.getstream.chat.android.ui.common.style.ChatStyle
import io.getstream.chat.android.ui.message.composer.attachment.AttachmentPreviewFactoryManager
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.DefaultQuotedAttachmentMessageFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager
import io.getstream.chat.android.ui.transformer.AutoLinkableTextTransformer
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer
import io.getstream.chat.android.ui.utils.lazyVar

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
     * Provides HTTP headers for image loading requests.
     */
    @JvmStatic
    public var imageHeadersProvider: ImageHeadersProvider by StreamImageLoader.instance()::imageHeadersProvider

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
            textView.text = messageItem.message.text
        }
    }

    /**
     * Allows intercepting and providing custom bitmap displayed with AvatarView.
     */
    @JvmStatic
    public var avatarBitmapFactory: AvatarBitmapFactory by lazyVar { AvatarBitmapFactory(appContext) }

    /**
     * Allows overriding default set of message reactions available.
     */
    @JvmStatic
    public var supportedReactions: SupportedReactions by lazyVar { SupportedReactions(appContext) }

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
            listOf(DefaultQuotedAttachmentMessageFactory())
        )
    }

    /**
     * Provides the currently logged in user.
     */
    @JvmStatic
    public var currentUserProvider: CurrentUserProvider = CurrentUserProvider.defaultCurrentUserProvider()

    /**
     * Configures if we show a thread separator when threads are empty or not. Adds the
     * separator item when value is `true`.
     */
    @JvmStatic
    public var showThreadSeparatorInEmptyThread: Boolean = false
}
