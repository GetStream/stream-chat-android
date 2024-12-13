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

@file:OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)

package io.getstream.chat.android.compose.ui.theme

import androidx.annotation.RequiresPermission
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import coil.ImageLoader
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.compose.ui.attachments.AttachmentFactory
import io.getstream.chat.android.compose.ui.attachments.StreamAttachmentFactories
import io.getstream.chat.android.compose.ui.attachments.preview.handler.AttachmentPreviewHandler
import io.getstream.chat.android.compose.ui.components.messages.factory.MessageContentFactory
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactories
import io.getstream.chat.android.compose.ui.messages.attachments.factory.AttachmentsPickerTabFactory
import io.getstream.chat.android.compose.ui.util.DefaultPollSwitchItemFactory
import io.getstream.chat.android.compose.ui.util.LocalStreamImageLoader
import io.getstream.chat.android.compose.ui.util.MessageAlignmentProvider
import io.getstream.chat.android.compose.ui.util.MessagePreviewFormatter
import io.getstream.chat.android.compose.ui.util.MessagePreviewIconFactory
import io.getstream.chat.android.compose.ui.util.MessageTextFormatter
import io.getstream.chat.android.compose.ui.util.PollSwitchItemFactory
import io.getstream.chat.android.compose.ui.util.QuotedMessageTextFormatter
import io.getstream.chat.android.compose.ui.util.ReactionIconFactory
import io.getstream.chat.android.compose.ui.util.SearchResultNameFormatter
import io.getstream.chat.android.compose.ui.util.StreamCoilImageLoaderFactory
import io.getstream.chat.android.ui.common.helper.DateFormatter
import io.getstream.chat.android.ui.common.helper.DefaultDownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DefaultImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.DefaultImageHeadersProvider
import io.getstream.chat.android.ui.common.helper.DownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DownloadRequestInterceptor
import io.getstream.chat.android.ui.common.helper.ImageAssetTransformer
import io.getstream.chat.android.ui.common.helper.ImageHeadersProvider
import io.getstream.chat.android.ui.common.helper.TimeProvider
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
private val LocalMessageContentFactory = compositionLocalOf<MessageContentFactory> {
    error("No message content factory provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalUseDefaultSystemMediaPicker = compositionLocalOf<Boolean> {
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
private val LocalReactionOptionsTheme = compositionLocalOf<ReactionOptionsTheme> {
    error("No ReactionOptionsTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessagePreviewIconFactory = compositionLocalOf<MessagePreviewIconFactory> {
    error("No message preview icon factory provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalPollSwitchItemFactory = compositionLocalOf<PollSwitchItemFactory> {
    error(
        "No reaction poll switch item factory provided! Make sure to wrap all usages of Stream components " +
            "in a ChatTheme.",
    )
}
private val LocalDateFormatter = compositionLocalOf<DateFormatter> {
    error("No DateFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalTimeProvider = compositionLocalOf<TimeProvider> {
    error("No TimeProvider provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalChannelNameFormatter = compositionLocalOf<ChannelNameFormatter> {
    error("No ChannelNameFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessagePreviewFormatter = compositionLocalOf<MessagePreviewFormatter> {
    error("No MessagePreviewFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageTextFormatter = compositionLocalOf<MessageTextFormatter> {
    error("No MessageTextFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalQuotedMessageTextFormatter = compositionLocalOf<QuotedMessageTextFormatter> {
    error("No QuotedMessageTextFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalSearchResultNameFormatter = compositionLocalOf<SearchResultNameFormatter> {
    error("No SearchResultNameFormatter provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalStreamImageHeadersProvider = compositionLocalOf<ImageHeadersProvider> {
    error("No ImageHeadersProvider provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalStreamDownloadAttachmentUriGenerator = compositionLocalOf<DownloadAttachmentUriGenerator> {
    error(
        "No DownloadAttachmentUriGenerator provided! Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalStreamDownloadRequestInterceptor = compositionLocalOf<DownloadRequestInterceptor> {
    error("No DownloadRequestInterceptor provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalStreamImageAssetTransformer = compositionLocalOf<ImageAssetTransformer> {
    error("No ImageAssetTransformer provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageAlignmentProvider = compositionLocalOf<MessageAlignmentProvider> {
    error("No MessageAlignmentProvider provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageOptionsTheme = compositionLocalOf<MessageOptionsTheme> {
    error("No MessageOptionsTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalChannelOptionsTheme = compositionLocalOf<ChannelOptionsTheme> {
    error("No ChannelOptionsTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
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
private val LocalMessageDateSeparatorTheme = compositionLocalOf<MessageDateSeparatorTheme> {
    error("No MessageDateSeparatorTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageUnreadSeparatorTheme = compositionLocalOf<MessageUnreadSeparatorTheme> {
    error("No MessageUnreadSeparatorTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalMessageComposerTheme = compositionLocalOf<MessageComposerTheme> {
    error("No MessageComposerTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalAttachmentPickerTheme = compositionLocalOf<AttachmentPickerTheme> {
    error("No AttachmentPickerTheme provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalAutoTranslationEnabled = compositionLocalOf<Boolean> {
    error(
        "No AutoTranslationEnabled Boolean provided! " +
            "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalComposerLinkPreviewEnabled = compositionLocalOf<Boolean> {
    error(
        "No ComposerLinkPreviewEnabled Boolean provided! " +
            "Make sure to wrap all usages of Stream components in a ChatTheme.",
    )
}
private val LocalStreamMediaRecorder = compositionLocalOf<StreamMediaRecorder> {
    error("No StreamMediaRecorder provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}
private val LocalKeyboardBehaviour = compositionLocalOf<StreamKeyboardBehaviour> {
    error("No StreamKeyboardBehaviour provided! Make sure to wrap all usages of Stream components in a ChatTheme.")
}

/**
 * Our theme that provides all the important properties for styling to the user.
 *
 * @param isInDarkMode If we're currently in the dark mode or not. Affects only the default color palette that's
 * provided. If you customize [colors], make sure to add your own logic for dark/light colors.
 * @param autoTranslationEnabled Whether messages auto translation is enabled or not.
 * @param isComposerLinkPreviewEnabled Whether the composer link preview is enabled or not.
 * @param colors The set of colors we provide, wrapped in [StreamColors].
 * @param dimens The set of dimens we provide, wrapped in [StreamDimens].
 * @param typography The set of typography styles we provide, wrapped in [StreamTypography].
 * @param shapes The set of shapes we provide, wrapped in [StreamShapes].
 * @param rippleConfiguration Defines the appearance for ripples.
 * @param attachmentFactories Attachment factories that we provide.
 * @param attachmentPreviewHandlers Attachment preview handlers we provide.
 * @param quotedAttachmentFactories Quoted attachment factories that we provide.
 * @param reactionIconFactory Used to create an icon [Painter] for the given reaction type.
 * @param reactionOptionsTheme [ReactionOptionsTheme] Theme for the reaction option list in the selected message menu.
 * For theming the message option list in the same menu, use [messageOptionsTheme].
 * @param messagePreviewIconFactory Used to create a preview icon for the given message type.
 * @param allowUIAutomationTest Allow to simulate ui automation with given test tags.
 * @param dateFormatter [DateFormatter] Used throughout the app for date and time information.
 * @param timeProvider [TimeProvider] Used throughout the app for time information.
 * @param channelNameFormatter [ChannelNameFormatter] Used throughout the app for channel names.
 * @param messagePreviewFormatter [MessagePreviewFormatter] Used to generate a string preview for the given message.
 * @param imageLoaderFactory A factory that creates new Coil [ImageLoader] instances.
 * @param imageAssetTransformer [ImageAssetTransformer] Used to transform image assets.
 * @param imageHeadersProvider [ImageHeadersProvider] Used to provide headers for image requests.
 * @param downloadAttachmentUriGenerator [DownloadAttachmentUriGenerator] Used to generate download URIs for
 * attachments.
 * @param downloadRequestInterceptor [DownloadRequestInterceptor] Used to intercept download requests.
 * @param messageAlignmentProvider [MessageAlignmentProvider] Used to provide message alignment for the given message.
 * @param messageOptionsTheme [MessageOptionsTheme] Theme for the message option list in the selected message menu.
 * For theming the reaction option list in the same menu, use [reactionOptionsTheme].
 * @param messageOptionsUserReactionAlignment Alignment of the user reaction inside the message options.
 * @param attachmentsPickerTabFactories Attachments picker tab factories that we provide.
 * @param videoThumbnailsEnabled Dictates whether video thumbnails will be displayed inside video previews.
 * @param streamCdnImageResizing Sets the strategy for resizing images hosted on Stream's CDN. Disabled by default,
 * set [StreamCdnImageResizing.imageResizingEnabled] to true if you wish to enable resizing images. Note that resizing
 * applies only to images hosted on Stream's CDN which contain the original height (oh) and width (ow) query parameters.
 * @param ownMessageTheme Theme of the current user messages.
 * @param otherMessageTheme Theme of the other users messages.
 * @param messageDateSeparatorTheme Theme of the message date separator.
 * @param messageUnreadSeparatorTheme Theme of the message unread separator.
 * @param messageComposerTheme Theme of the message composer.
 * @param attachmentPickerTheme Theme of the attachment picker.
 * @param streamMediaRecorder Used for recording audio messages.
 * @param keyboardBehaviour Configuration for different keyboard behaviours.
 * @param content The content shown within the theme wrapper.
 */
@Suppress("LongMethod")
@Composable
public fun ChatTheme(
    isInDarkMode: Boolean = isSystemInDarkTheme(),
    autoTranslationEnabled: Boolean = false,
    isComposerLinkPreviewEnabled: Boolean = false,
    useDefaultSystemMediaPicker: Boolean = false,
    colors: StreamColors = if (isInDarkMode) StreamColors.defaultDarkColors() else StreamColors.defaultColors(),
    dimens: StreamDimens = StreamDimens.defaultDimens(),
    typography: StreamTypography = StreamTypography.defaultTypography(),
    shapes: StreamShapes = StreamShapes.defaultShapes(),
    rippleConfiguration: StreamRippleConfiguration = StreamRippleConfiguration.defaultRippleConfiguration(
        contentColor = LocalContentColor.current,
        lightTheme = !isInDarkMode,
    ),
    attachmentFactories: List<AttachmentFactory> = StreamAttachmentFactories.defaultFactories(),
    messageContentFactory: MessageContentFactory = MessageContentFactory(),
    attachmentPreviewHandlers: List<AttachmentPreviewHandler> =
        AttachmentPreviewHandler.defaultAttachmentHandlers(LocalContext.current),
    quotedAttachmentFactories: List<AttachmentFactory> = StreamAttachmentFactories.defaultQuotedFactories(),
    reactionIconFactory: ReactionIconFactory = ReactionIconFactory.defaultFactory(),
    reactionOptionsTheme: ReactionOptionsTheme = ReactionOptionsTheme.defaultTheme(),
    messagePreviewIconFactory: MessagePreviewIconFactory = MessagePreviewIconFactory.defaultFactory(),
    pollSwitchItemFactory: PollSwitchItemFactory = DefaultPollSwitchItemFactory(context = LocalContext.current),
    allowUIAutomationTest: Boolean = false,
    dateFormatter: DateFormatter = DateFormatter.from(LocalContext.current),
    timeProvider: TimeProvider = TimeProvider.DEFAULT,
    channelNameFormatter: ChannelNameFormatter = ChannelNameFormatter.defaultFormatter(LocalContext.current),
    messagePreviewFormatter: MessagePreviewFormatter = MessagePreviewFormatter.defaultFormatter(
        context = LocalContext.current,
        typography = typography,
        attachmentFactories = attachmentFactories,
        autoTranslationEnabled = autoTranslationEnabled,
    ),
    searchResultNameFormatter: SearchResultNameFormatter = SearchResultNameFormatter.defaultFormatter(),
    imageLoaderFactory: StreamCoilImageLoaderFactory = StreamCoilImageLoaderFactory.defaultFactory(),
    imageHeadersProvider: ImageHeadersProvider = DefaultImageHeadersProvider,
    downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator = DefaultDownloadAttachmentUriGenerator,
    downloadRequestInterceptor: DownloadRequestInterceptor = DownloadRequestInterceptor { },
    imageAssetTransformer: ImageAssetTransformer = DefaultImageAssetTransformer,
    messageAlignmentProvider: MessageAlignmentProvider = MessageAlignmentProvider.defaultMessageAlignmentProvider(),
    messageOptionsTheme: MessageOptionsTheme = MessageOptionsTheme.defaultTheme(),
    channelOptionsTheme: ChannelOptionsTheme = ChannelOptionsTheme.defaultTheme(),
    messageOptionsUserReactionAlignment: MessageOptionsUserReactionAlignment = MessageOptionsUserReactionAlignment.END,
    attachmentsPickerTabFactories: List<AttachmentsPickerTabFactory> =
        if (useDefaultSystemMediaPicker) {
            AttachmentsPickerTabFactories.defaultFactoriesWithoutStoragePermissions()
        } else {
            AttachmentsPickerTabFactories.defaultFactories()
        },
    videoThumbnailsEnabled: Boolean = true,
    streamCdnImageResizing: StreamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
    readCountEnabled: Boolean = true,
    ownMessageTheme: MessageTheme = MessageTheme.defaultOwnTheme(
        isInDarkMode = isInDarkMode,
        typography = typography,
        shapes = shapes,
        colors = colors,
    ),
    otherMessageTheme: MessageTheme = MessageTheme.defaultOtherTheme(
        isInDarkMode = isInDarkMode,
        typography = typography,
        shapes = shapes,
        colors = colors,
    ),
    messageDateSeparatorTheme: MessageDateSeparatorTheme = MessageDateSeparatorTheme.defaultTheme(
        typography = typography,
        colors = colors,
    ),
    messageUnreadSeparatorTheme: MessageUnreadSeparatorTheme = MessageUnreadSeparatorTheme.defaultTheme(
        typography = typography,
        colors = colors,
    ),
    messageComposerTheme: MessageComposerTheme = MessageComposerTheme.defaultTheme(
        isInDarkMode = isInDarkMode,
        typography = typography,
        shapes = shapes,
        colors = colors,
    ),
    attachmentPickerTheme: AttachmentPickerTheme = AttachmentPickerTheme.defaultTheme(colors),
    messageTextFormatter: MessageTextFormatter = MessageTextFormatter.defaultFormatter(
        autoTranslationEnabled = autoTranslationEnabled,
        typography = typography,
        shapes = shapes,
        colors = colors,
        ownMessageTheme = ownMessageTheme,
        otherMessageTheme = otherMessageTheme,
    ),
    quotedMessageTextFormatter: QuotedMessageTextFormatter = QuotedMessageTextFormatter.defaultFormatter(
        autoTranslationEnabled = autoTranslationEnabled,
        context = LocalContext.current,
        typography = typography,
        shapes = shapes,
        colors = colors,
        ownMessageTheme = ownMessageTheme,
        otherMessageTheme = otherMessageTheme,
    ),
    streamMediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(LocalContext.current),
    keyboardBehaviour: StreamKeyboardBehaviour = StreamKeyboardBehaviour.defaultBehaviour(),
    content: @Composable () -> Unit,
) {
    LaunchedEffect(Unit) {
        ChatClient.VERSION_PREFIX_HEADER = VersionPrefixHeader.Compose
    }

    CompositionLocalProvider(
        LocalColors provides colors,
        LocalDimens provides dimens,
        LocalTypography provides typography,
        LocalShapes provides shapes,
        LocalRippleConfiguration provides rippleConfiguration.toRippleConfiguration(),
        LocalUseDefaultSystemMediaPicker provides useDefaultSystemMediaPicker,
        LocalAttachmentFactories provides attachmentFactories,
        LocalAttachmentPreviewHandlers provides attachmentPreviewHandlers,
        LocalQuotedAttachmentFactories provides quotedAttachmentFactories,
        LocalMessageContentFactory provides messageContentFactory,
        LocalReactionIconFactory provides reactionIconFactory,
        LocalMessagePreviewIconFactory provides messagePreviewIconFactory,
        LocalReactionOptionsTheme provides reactionOptionsTheme,
        LocalPollSwitchItemFactory provides pollSwitchItemFactory,
        LocalDateFormatter provides dateFormatter,
        LocalTimeProvider provides timeProvider,
        LocalChannelNameFormatter provides channelNameFormatter,
        LocalMessagePreviewFormatter provides messagePreviewFormatter,
        LocalMessageTextFormatter provides messageTextFormatter,
        LocalQuotedMessageTextFormatter provides quotedMessageTextFormatter,
        LocalSearchResultNameFormatter provides searchResultNameFormatter,
        LocalOwnMessageTheme provides ownMessageTheme,
        LocalOtherMessageTheme provides otherMessageTheme,
        LocalMessageDateSeparatorTheme provides messageDateSeparatorTheme,
        LocalMessageUnreadSeparatorTheme provides messageUnreadSeparatorTheme,
        LocalMessageComposerTheme provides messageComposerTheme,
        LocalAttachmentPickerTheme provides attachmentPickerTheme,
        LocalStreamImageLoader provides imageLoaderFactory.imageLoader(LocalContext.current.applicationContext),
        LocalStreamImageHeadersProvider provides imageHeadersProvider,
        LocalStreamDownloadAttachmentUriGenerator provides downloadAttachmentUriGenerator,
        LocalStreamDownloadRequestInterceptor provides downloadRequestInterceptor,
        LocalStreamImageAssetTransformer provides imageAssetTransformer,
        LocalMessageAlignmentProvider provides messageAlignmentProvider,
        LocalMessageOptionsTheme provides messageOptionsTheme,
        LocalChannelOptionsTheme provides channelOptionsTheme,
        LocalMessageOptionsUserReactionAlignment provides messageOptionsUserReactionAlignment,
        LocalAttachmentsPickerTabFactories provides attachmentsPickerTabFactories,
        LocalVideoThumbnailsEnabled provides videoThumbnailsEnabled,
        LocalStreamCdnImageResizing provides streamCdnImageResizing,
        LocalReadCountEnabled provides readCountEnabled,
        LocalStreamMediaRecorder provides streamMediaRecorder,
        LocalAutoTranslationEnabled provides autoTranslationEnabled,
        LocalComposerLinkPreviewEnabled provides isComposerLinkPreviewEnabled,
        LocalKeyboardBehaviour provides keyboardBehaviour,
    ) {
        if (allowUIAutomationTest) {
            Box(
                modifier = Modifier.semantics { testTagsAsResourceId = allowUIAutomationTest },
            ) {
                content()
            }
        } else {
            content()
        }
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

    public val useDefaultSystemMediaPicker: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalUseDefaultSystemMediaPicker.current

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
     * Retrieves the current list of quoted [MessageContentFactory] at the call site's position in the hierarchy.
     */
    public val messageContentFactory: MessageContentFactory
        @Composable
        @RequiresPermission.Read
        get() = LocalMessageContentFactory.current

    /**
     * Retrieves the current reaction icon factory at the call site's position in the hierarchy.
     */
    public val reactionIconFactory: ReactionIconFactory
        @Composable
        @ReadOnlyComposable
        get() = LocalReactionIconFactory.current

    /**
     * Retrieves the current [ReactionOptionsTheme] at the call site's position in the hierarchy.
     */
    public val reactionOptionsTheme: ReactionOptionsTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalReactionOptionsTheme.current

    /**
     * Retrieves the current message preview icon factory at the call site's position in the hierarchy.
     */
    public val messagePreviewIconFactory: MessagePreviewIconFactory
        @Composable
        @ReadOnlyComposable
        get() = LocalMessagePreviewIconFactory.current

    /**
     * Retrieves the current [PollSwitchItemFactory] at the call site's position in the hierarchy.
     */
    public val pollSwitchitemFactory: PollSwitchItemFactory
        @Composable
        @ReadOnlyComposable
        get() = LocalPollSwitchItemFactory.current

    /**
     * Retrieves the current [DateFormatter] at the call site's position in the hierarchy.
     */
    public val dateFormatter: DateFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalDateFormatter.current

    /**
     * Retrieves the current [TimeProvider] at the call site's position in the hierarchy.
     */
    public val timeProvider: TimeProvider
        @Composable
        @ReadOnlyComposable
        get() = LocalTimeProvider.current

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
     * Retrieves the current [MessageTextFormatter] at the call site's position in the hierarchy.
     */
    public val messageTextFormatter: MessageTextFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageTextFormatter.current

    /**
     * Retrieves the current [QuotedMessageTextFormatter] at the call site's position in the hierarchy.
     */
    public val quotedMessageTextFormatter: QuotedMessageTextFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalQuotedMessageTextFormatter.current

    /**
     * Retrieves the current [SearchResultNameFormatter] at the call site's position in the hierarchy.
     */
    public val searchResultNameFormatter: SearchResultNameFormatter
        @Composable
        @ReadOnlyComposable
        get() = LocalSearchResultNameFormatter.current

    /**
     * Retrieves the current [MessageAlignmentProvider] at the call site's position in the hierarchy.
     */
    public val messageAlignmentProvider: MessageAlignmentProvider
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageAlignmentProvider.current

    /**
     * Retrieves the current [MessageOptionsTheme] at the call site's position in the hierarchy.
     */
    public val messageOptionsTheme: MessageOptionsTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageOptionsTheme.current

    /**
     * Retrieves the current [ChannelOptionsTheme] at the call site's position in the hierarchy.
     */
    public val channelOptionsTheme: ChannelOptionsTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalChannelOptionsTheme.current

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
     * Retrieves the current [MessageDateSeparatorTheme] at the call site's position in the hierarchy.
     */
    public val messageDateSeparatorTheme: MessageDateSeparatorTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageDateSeparatorTheme.current

    /**
     * Retrieves the current [MessageUnreadSeparatorTheme] at the call site's position in the hierarchy.
     */
    public val messageUnreadSeparatorTheme: MessageUnreadSeparatorTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageUnreadSeparatorTheme.current

    /**
     * Retrieves the current [MessageComposerTheme] at the call site's position in the hierarchy.
     */
    public val messageComposerTheme: MessageComposerTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalMessageComposerTheme.current

    /**
     * Retrieves the current [AttachmentPickerTheme] at the call site's position in the hierarchy.
     */
    public val attachmentPickerTheme: AttachmentPickerTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAttachmentPickerTheme.current

    /**
     * Retrieves the current [ImageHeadersProvider] at the call site's position in the hierarchy.
     */
    public val streamImageHeadersProvider: ImageHeadersProvider
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamImageHeadersProvider.current

    public val streamDownloadAttachmentUriGenerator: DownloadAttachmentUriGenerator
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamDownloadAttachmentUriGenerator.current

    public val streamDownloadRequestInterceptor: DownloadRequestInterceptor
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamDownloadRequestInterceptor.current

    /**
     * Retrieves the current [ImageAssetTransformer] at the call site's position in the hierarchy.
     */
    public val streamImageAssetTransformer: ImageAssetTransformer
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamImageAssetTransformer.current

    /**
     * Retrieves the current [autoTranslationEnabled] value at the call site's position in the hierarchy.
     */
    public val autoTranslationEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAutoTranslationEnabled.current

    /**
     * Retrieves the current [isComposerLinkPreviewEnabled] value at the call site's position in the hierarchy.
     */
    public val isComposerLinkPreviewEnabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalComposerLinkPreviewEnabled.current

    /**
     * Retrieves the current list of [StreamMediaRecorder] at the call site's position in the hierarchy.
     */
    public val streamMediaRecorder: StreamMediaRecorder
        @Composable
        @ReadOnlyComposable
        get() = LocalStreamMediaRecorder.current

    /**
     * Retrieves the current [StreamKeyboardBehaviour] at the call site's position in the hierarchy.
     */
    public val keyboardBehaviour: StreamKeyboardBehaviour
        @Composable
        @ReadOnlyComposable
        get() = LocalKeyboardBehaviour.current
}
