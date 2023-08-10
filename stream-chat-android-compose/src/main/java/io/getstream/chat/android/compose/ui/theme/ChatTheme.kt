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

package io.getstream.chat.android.compose.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactories
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.compose.ui.util.LocalStreamImageLoader
import io.getstream.chat.android.compose.ui.util.MessageAlignmentProvider
import io.getstream.chat.android.compose.ui.util.MessagePreviewFormatter
import io.getstream.chat.android.compose.ui.util.ReactionIconFactory
import io.getstream.chat.android.compose.ui.util.StreamCoilImageLoaderFactory
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing
import io.getstream.chat.android.ui.common.state.messages.list.MessageOptionsUserReactionAlignment
import io.getstream.chat.android.ui.common.utils.ChannelNameFormatter
import io.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import io.getstream.sdk.chat.audio.recording.StreamMediaRecorder

/**
 * Local providers for various properties we connect to our components, for styling.
 */
private val LocalColors = compositionLocalOf<StreamColors> {
    error("No colors provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalDimens = compositionLocalOf<StreamDimens> {
    error("No dimens provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalTypography = compositionLocalOf<StreamTypography> {
    error("No typography provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalShapes = compositionLocalOf<StreamShapes> {
    error("No shapes provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalAttachmentFactories = compositionLocalOf<List<AttachmentFactory>> {
    error("No attachment factories provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalAttachmentPreviewHandlers = compositionLocalOf<List<AttachmentPreviewHandler>> {
    error("No attachment preview handlers provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalQuotedAttachmentFactories = compositionLocalOf<List<AttachmentFactory>> {
    error("No quoted attachment factories provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalReactionIconFactory = compositionLocalOf<ReactionIconFactory> {
    error("No reaction icon factory provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalDateFormatter = compositionLocalOf<DateFormatter> {
    error("No DateFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalChannelNameFormatter = compositionLocalOf<ChannelNameFormatter> {
    error("No ChannelNameFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessagePreviewFormatter = compositionLocalOf<MessagePreviewFormatter> {
    error("No MessagePreviewFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageAlignmentProvider = compositionLocalOf<MessageAlignmentProvider> {
    error("No MessageAlignmentProvider provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageOptionsUserReactionAlignment = compositionLocalOf<MessageOptionsUserReactionAlignment> {
    error(
        "No LocalMessageOptionsUserReactionAlignment provided! Make sure to wrap all usages of Stream components " +
            "in a ChatTheme.",
    )
}

private val LocalAttachmentsPickerTabFactories = compositionLocalOf<List<AttachmentsPickerTabFactory>> {
    error("No attachments picker tab factories provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}

private val LocalVideoThumbnailsEnabled = compositionLocalOf<Boolean> {
    error(
        "No videoThumbnailsEnabled Boolean provided! " +
            "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalStreamCdnImageResizing = compositionLocalOf<StreamCdnImageResizing> {
    error(
        "No StreamCdnImageResizing provided! " +
            "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalReadCountEnabled = compositionLocalOf<Boolean> {
    error(
        "No readCountEnabled Boolean provided! " +
            "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalOwnMessageTheme = compositionLocalOf<MessageTheme> {
    error("No OwnMessageTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalOtherMessageTheme = compositionLocalOf<MessageTheme> {
    error("No OtherMessageTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}

private val LocalStreamMediaRecorder = compositionLocalOf<StreamMediaRecorder> {
    error("No StreamMediaRecorder provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}

/**
 * Our theme that provides all the important properties for styling to the user.
 *
 * @param isInDarkMode If we're currently in the dark mode or not. Affects only the default color palette that's
 * provided. If you customize [colors], make sure to add your own logic for dark/light colors.
 * @param colors The set of colors we provide, wrapped in [StreamColors].
 * @param dimens The set of dimens we provide, wrapped in [StreamDimens].
 * @param typography The set of typography styles we provide, wrapped in [StreamTypography].
 * @param shapes The set of shapes we provide, wrapped in [StreamShapes].
 * @param rippleTheme Defines the appearance for ripples.
 * @param attachmentFactories Attachment factories that we provide.
 * @param attachmentPreviewHandlers Attachment preview handlers we provide.
 * @param quotedAttachmentFactories Quoted attachment factories that we provide.
 * @param reactionIconFactory Used to create an icon [Painter] for the given reaction type.
 * @param dateFormatter [DateFormatter] used throughout the app for date and time information.
 * @param channelNameFormatter [ChannelNameFormatter] used throughout the app for channel names.
 * @param messagePreviewFormatter [MessagePreviewFormatter] used to generate a string preview for the given message.
 * @param imageLoaderFactory A factory that creates new Coil [ImageLoader] instances.
 * @param messageAlignmentProvider [MessageAlignmentProvider] used to provide message alignment for the given message.
 * @param messageOptionsUserReactionAlignment Alignment of the user reaction inside the message options.
 * @param attachmentsPickerTabFactories Attachments picker tab factories that we provide.
 * @param videoThumbnailsEnabled Dictates whether video thumbnails will be displayed inside video previews.
 * @param streamCdnImageResizing Sets the strategy for resizing images hosted on Stream's CDN. Disabled by default,
 * set [StreamCdnImageResizing.imageResizingEnabled] to true if you wish to enable resizing images. Note that resizing
 * applies only to images hosted on Stream's CDN which contain the original height (oh) and width (ow) query parameters.
 * @param ownMessageTheme Theme of the current user messages.
 * @param otherMessageTheme Theme of the other users messages.
 * @param streamMediaRecorder Used for recording audio messages.
 * @param content The content shown within the theme wrapper.
 */
@Composable
public fun ChatTheme(
    isInDarkMode: Boolean = isSystemInDarkTheme(),
    colors: StreamColors = if (isInDarkMode) StreamColors.defaultDarkColors() else StreamColors.defaultColors(),
    dimens: StreamDimens = StreamDimens.defaultDimens(),
    typography: StreamTypography = StreamTypography.defaultTypography(),
    shapes: StreamShapes = StreamShapes.defaultShapes(),
    rippleTheme: RippleTheme = StreamRippleTheme,
    attachmentFactories: List<AttachmentFactory> = StreamAttachmentFactories.defaultFactories(),
    attachmentPreviewHandlers: List<AttachmentPreviewHandler> =
        AttachmentPreviewHandler.defaultAttachmentHandlers(LocalContext.current),
    quotedAttachmentFactories: List<AttachmentFactory> = StreamAttachmentFactories.defaultQuotedFactories(),
    reactionIconFactory: ReactionIconFactory = ReactionIconFactory.defaultFactory(),
    dateFormatter: DateFormatter = DateFormatter.from(LocalContext.current),
    channelNameFormatter: ChannelNameFormatter = ChannelNameFormatter.defaultFormatter(LocalContext.current),
    messagePreviewFormatter: MessagePreviewFormatter = MessagePreviewFormatter.defaultFormatter(
        context = LocalContext.current,
        typography = typography,
        attachmentFactories = attachmentFactories,
    ),
    imageLoaderFactory: StreamCoilImageLoaderFactory = StreamCoilImageLoaderFactory.defaultFactory(),
    messageAlignmentProvider: MessageAlignmentProvider = MessageAlignmentProvider.defaultMessageAlignmentProvider(),
    messageOptionsUserReactionAlignment: MessageOptionsUserReactionAlignment = MessageOptionsUserReactionAlignment.END,
    attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory> = AttachmentsPickerTabFactories.defaultFactories(),
    videoThumbnailsEnabled: Boolean = true,
    streamCdnImageResizing: StreamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
    readCountEnabled: Boolean = true,
    ownMessageTheme: MessageTheme = MessageTheme.defaultOwnTheme(
        typography = typography,
        colors = colors,
    ),
    otherMessageTheme: MessageTheme = MessageTheme.defaultOtherTheme(
        typography = typography,
        colors = colors,
    ),
    streamMediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(LocalContext.current),
    content: @Composable () -> Unit,
) {
    LaunchedEffect(Unit) {
        ChatClient.VERSION_PREFIX_HEADER = VersionPrefixHeader.COMPOSE
    }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalDimens provides dimens,
        LocalTypography provides typography,
        LocalShapes provides shapes,
        LocalRippleTheme provides rippleTheme,
        LocalAttachmentFactories provides attachmentFactories,
        LocalAttachmentPreviewHandlers provides attachmentPreviewHandlers,
        LocalQuotedAttachmentFactories provides quotedAttachmentFactories,
        LocalReactionIconFactory provides reactionIconFactory,
        LocalDateFormatter provides dateFormatter,
        LocalChannelNameFormatter provides channelNameFormatter,
        LocalMessagePreviewFormatter provides messagePreviewFormatter,
        LocalOwnMessageTheme provides ownMessageTheme,
        LocalOtherMessageTheme provides otherMessageTheme,
        LocalStreamImageLoader provides imageLoaderFactory.imageLoader(LocalContext.current.applicationContext),
        LocalMessageAlignmentProvider provides messageAlignmentProvider,
        LocalMessageOptionsUserReactionAlignment provides messageOptionsUserReactionAlignment,
        LocalAttachmentsPickerTabFactories provides attachmentsPickerTabFactories,
        LocalVideoThumbnailsEnabled provides videoThumbnailsEnabled,
        LocalStreamCdnImageResizing provides streamCdnImageResizing,
        LocalReadCountEnabled provides readCountEnabled,
        LocalStreamMediaRecorder provides streamMediaRecorder,
    ) {
        content()
    }
}

/**
 * Contains ease-of-use accessors for different properties used to style and customize the app
 * look and feel.
 */
public object ChatTheme {
    /**
     * Retrieves the current [StreamColors] at the call site's position in the hierarchy.
     */
    public val colors: StreamColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    /**
     * Retrieves the current [StreamDimens] at the call site's position in the hierarchy.
     */
    public val dimens: StreamDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalDimens.current

    /**
     * Retrieves the current [StreamTypography] at the call site's position in the hierarchy.
     */
    public val typography: StreamTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    /**
     * Retrieves the current [StreamShapes] at the call site's position in the hierarchy.
     */
    public val shapes: StreamShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current

    /**
     * Retrieves the current list of [AttachmentFactory] at the call site's position in the hierarchy.
     */
    public val attachmentFactories: List<AttachmentFactory>
        @Composable
        @ReadOnlyComposable
        get() = LocalAttachmentFactories.current

    /**
     * Retrieves the current list of [AttachmentPreviewHandler] at the call site's position in the hierarchy.
     */
    public val attachmentPreviewHandlers: List<AttachmentPreviewHandler>
        @Composable
        @ReadOnlyComposable
        get() = LocalAttachmentPreviewHandlers.current

    /**
     * Retrieves the current list of quoted [AttachmentFactory] at the call site's position in the hierarchy.
     */
    public val quotedAttachmentFactories: List<AttachmentFactory>
        @Composable
        @ReadOnlyComposable
        get() = LocalQuotedAttachmentFactories.current

    /**
     * Retrieves the current reaction icon factory at the call site's position in the hierarchy.
     */
    public val reactionIconFactory: ReactionIconFactory
        @Composable
        @ReadOnlyComposable
        get() = LocalReactionIconFactory.current

    /**
     * Retrieves the current [DateFormatter] at the call site's position in the hierarchy.
     */
    public val dateFormatter: DateFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalDateFormatter.current

    /**
     * Retrieves the current [ChannelNameFormatter] at the call site's position in the hierarchy.
     */
    public val channelNameFormatter: ChannelNameFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalChannelNameFormatter.current

    /**
     * Retrieves the current [MessagePreviewFormatter] at the call site's position in the hierarchy.
     */
    public val messagePreviewFormatter: MessagePreviewFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalMessagePreviewFormatter.current

    /**
     * Retrieves the current [MessageAlignmentProvider] at the call site's position in the hierarchy.
     */
    public val messageAlignmentProvider: MessageAlignmentProvider
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageAlignmentProvider.current

    /**
     * Retrieves the current [MessageOptionsUserReactionAlignment] at the call site's position in the hierarchy.
     */
    public val messageOptionsUserReactionAlignment: MessageOptionsUserReactionAlignment
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageOptionsUserReactionAlignment.current

    /**
     * Retrieves the current list of [AttachmentsPickerTabFactory] at the call site's position in the hierarchy.
     */
    public val attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory>
        @Composable
        @ReadOnlyComposable
        get() = LocalAttachmentsPickerTabFactories.current

    /**
     * Retrieves the value of [Boolean] dictating whether video thumbnails are enabled at the call site's
     * position in the hierarchy.
     */
    public val videoThumbnailsEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalVideoThumbnailsEnabled.current

    /**
     * Retrieves the value of [StreamCdnImageResizing] at the call site's position in the hierarchy.
     */
    public val streamCdnImageResizing: StreamCdnImageResizing
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamCdnImageResizing.current

    public val readCountEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalReadCountEnabled.current

    /**
     * Retrieves the current own [MessageTheme] at the call site's position in the hierarchy.
     */
    public val ownMessageTheme: MessageTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalOwnMessageTheme.current

    /**
     * Retrieves the current other [MessageTheme] at the call site's position in the hierarchy.
     */
    public val otherMessageTheme: MessageTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalOtherMessageTheme.current

    /**
     * Retrieves the current list of [StreamMediaRecorder] at the call site's position in the hierarchy.
     */
    public val streamMediaRecorder: StreamMediaRecorder
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamMediaRecorder.current
}
