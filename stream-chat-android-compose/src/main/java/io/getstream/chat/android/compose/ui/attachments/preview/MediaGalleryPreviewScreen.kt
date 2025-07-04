/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.compose.ui.attachments.preview

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewOption
import io.getstream.chat.android.compose.ui.attachments.preview.internal.MediaGalleryImagePage
import io.getstream.chat.android.compose.ui.attachments.preview.internal.MediaGalleryPhotosMenu
import io.getstream.chat.android.compose.ui.attachments.preview.internal.MediaGalleryVideoPage
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.mediapreview.MediaGalleryPreviewViewModel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.uiutils.extension.hasLink
import kotlinx.coroutines.launch
import java.util.Date

/**
 * A stateful composable function rendering a screen for previewing visual media attachments (images and videos).
 * Renders a screen with the following structure:
 * - Header consisting of:
 *   - Leading content (close icon)
 *   - Center content (title)
 *   - Trailing content (options icon)
 * - Content (pager with images/videos)
 * - Footer consisting of:
 *   - Leading content (share icon)
 *   - Center content (page indicator or sharing progress)
 *   - Trailing content (photos/gallery icon)
 *
 * @param viewModel The [MediaGalleryPreviewViewModel] instance to use for managing the state of the screen.
 * @param initialPage The initial page to display in the pager.
 * @param onHeaderLeadingContentClick Callback to be invoked when the leading content in the header is clicked. Usually
 * closes the screen.
 * @param onOptionClick Callback to be invoked when an option in the options menu is clicked.
 * @param onRequestShareAttachment Callback to be invoked when the share icon in the footer is clicked.
 * @param onConfirmShareAttachment Callback to be invoked when the user confirms sharing an attachment.
 * @param modifier The [Modifier] to be applied to the screen.
 * @param config The configuration for the media gallery.
 * @param onHeaderTrailingContentClick Callback to be invoked when the trailing content in the header is clicked. By
 * default, it shows the options menu.
 * @param onFooterLeadingContentClick Callback to be invoked when the leading content in the footer is clicked. By
 * default, it shares the attachment.
 * @param onFooterTrailingContentClick Callback to be invoked when the trailing content in the footer is clicked. By
 * default, it shows a bottom sheet gallery with all attachments in the message.
 * @param onDismissShareAttachment Callback to be invoked when the user dismisses the share large file dialog.
 * @param onDismissOptionsMenu Callback to be invoked when the options menu is dismissed.
 * @param onDismissGallery Callback to be invoked when the gallery bottom sheet is dismissed.
 * @param header Composable function to render the header. By default, it renders a [MediaGalleryPreviewHeader].
 * @param content Composable function to render the content. By default, it renders a [MediaGalleryPager].
 * @param footer Composable function to render the footer. By default, it renders a [MediaGalleryPreviewFooter].
 * @param optionsMenu Composable function to render the options menu. By default, it renders a
 * [MediaGalleryOptionsMenu].
 */
@Suppress("LongParameterList")
@ExperimentalStreamChatApi
@Composable
public fun MediaGalleryPreviewScreen(
    viewModel: MediaGalleryPreviewViewModel,
    initialPage: Int,
    onHeaderLeadingContentClick: () -> Unit,
    onOptionClick: (Attachment, MediaGalleryPreviewOption) -> Unit,
    onRequestShareAttachment: (Attachment) -> Unit,
    onConfirmShareAttachment: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    config: MediaGalleryConfig = ChatTheme.mediaGalleryConfig,
    onHeaderTrailingContentClick: () -> Unit = { viewModel.toggleMediaOptions(true) },
    onFooterLeadingContentClick: (Attachment) -> Unit = onRequestShareAttachment,
    onFooterTrailingContentClick: (Attachment) -> Unit = { viewModel.toggleGallery(true) },
    onDismissShareAttachment: () -> Unit = { viewModel.promptedAttachment = null },
    onDismissOptionsMenu: () -> Unit = { viewModel.toggleMediaOptions(false) },
    onDismissGallery: () -> Unit = { viewModel.toggleGallery(false) },
    header: @Composable (attachments: List<Attachment>, currentPage: Int) -> Unit = { _, _ ->
        MediaGalleryPreviewHeader(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            message = viewModel.message,
            connectionState = viewModel.connectionState,
            onLeadingContentClick = onHeaderLeadingContentClick,
            onTrailingContentClick = onHeaderTrailingContentClick,
        )
    },
    content: @Composable (
        padding: PaddingValues,
        pagerState: PagerState,
        attachments: List<Attachment>,
        onPlaybackError: () -> Unit,
    ) -> Unit = { padding, pagerState, attachments, onPlaybackError ->
        MediaGalleryPager(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.appBackground)
                .padding(padding),
            pagerState = pagerState,
            attachments = attachments,
            onPlaybackError = onPlaybackError,
        )
    },
    footer: @Composable (attachments: List<Attachment>, currentPage: Int) -> Unit = { attachments, currentPage ->
        MediaGalleryPreviewFooter(
            attachments = attachments,
            currentPage = currentPage,
            totalPages = attachments.size,
            connectionState = viewModel.connectionState,
            isSharingInProgress = viewModel.isSharingInProgress,
            onLeadingContentClick = onFooterLeadingContentClick,
            onTrailingContentClick = onFooterTrailingContentClick,
        )
    },
    optionsMenu: @Composable (
        attachment: Attachment,
        options: List<MediaGalleryPreviewOption>,
    ) -> Unit = { attachment, options ->
        MediaGalleryOptionsMenu(
            attachment = attachment,
            options = options,
            onOptionClick = onOptionClick,
            onDismiss = onDismissOptionsMenu,
        )
    },
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    MediaGalleryPreviewScreen(
        message = viewModel.message,
        connectionState = viewModel.connectionState,
        currentUser = user,
        initialPage = initialPage,
        promptedAttachment = viewModel.promptedAttachment,
        isSharingInProgress = viewModel.isSharingInProgress,
        isShowingOptions = viewModel.isShowingOptions,
        isShowingGallery = viewModel.isShowingGallery,
        modifier = modifier,
        config = config,
        onHeaderLeadingContentClick = onHeaderLeadingContentClick,
        onHeaderTrailingContentClick = onHeaderTrailingContentClick,
        onFooterLeadingContentClick = onFooterLeadingContentClick,
        onFooterTrailingContentClick = onFooterTrailingContentClick,
        onConfirmShareAttachment = onConfirmShareAttachment,
        onDismissShareAttachment = onDismissShareAttachment,
        onOptionClick = onOptionClick,
        onRequestShareAttachment = onRequestShareAttachment,
        onDismissOptionsMenu = onDismissOptionsMenu,
        onDismissGallery = onDismissGallery,
        header = header,
        content = content,
        footer = footer,
        optionsMenu = optionsMenu,
    )
}

/**
 * A stateless composable function rendering a screen for previewing visual media attachments (images and videos).
 * Renders a screen with the following structure:
 * - Header consisting of:
 *   - Leading content (close icon)
 *   - Center content (title)
 *   - Trailing content (options icon)
 * - Content (pager with images/videos)
 * - Footer consisting of:
 *   - Leading content (share icon)
 *   - Center content (page indicator or sharing progress)
 *   - Trailing content (photos/gallery icon)
 *
 * @param message The message containing the attachments to be previewed.
 * @param connectionState TThe network connection state.
 * @param currentUser The currently logged user.
 * @param initialPage The initial page to display in the pager.
 * @param onHeaderLeadingContentClick Callback to be invoked when the leading content in the header is clicked. Usually
 * closes the screen.
 * @param onOptionClick Callback to be invoked when an option in the options menu is clicked.
 * @param onRequestShareAttachment Callback to be invoked when the share icon in the footer is clicked.
 * @param modifier The [Modifier] to be applied to the screen.
 * @param config The configuration for the media gallery.
 * @param onHeaderTrailingContentClick Callback to be invoked when the trailing content in the header is clicked. By
 * default, it shows the options menu.
 * @param onFooterLeadingContentClick Callback to be invoked when the leading content in the footer is clicked. By
 * default, it shares the attachment.
 * @param onFooterTrailingContentClick Callback to be invoked when the trailing content in the footer is clicked. By
 * default, it shows a bottom sheet gallery with all attachments in the message.
 * @param onConfirmShareAttachment Callback to be invoked when the user confirms sharing a large file.
 * @param onDismissShareAttachment Callback to be invoked when the user dismisses the share large file dialog.
 * @param onDismissOptionsMenu Callback to be invoked when the options menu is dismissed.
 * @param onDismissGallery Callback to be invoked when the gallery bottom sheet is dismissed.
 * @param header Composable function to render the header. By default, it renders a [MediaGalleryPreviewHeader].
 * @param content Composable function to render the content. By default, it renders a [MediaGalleryPager].
 * @param footer Composable function to render the footer. By default, it renders a [MediaGalleryPreviewFooter].
 * @param optionsMenu Composable function to render the options menu. By default, it renders a
 * [MediaGalleryOptionsMenu].
 */
@Suppress("LongParameterList", "LongMethod")
@ExperimentalStreamChatApi
@Composable
public fun MediaGalleryPreviewScreen(
    message: Message,
    connectionState: ConnectionState,
    currentUser: User?,
    initialPage: Int,
    promptedAttachment: Attachment?,
    isSharingInProgress: Boolean,
    isShowingOptions: Boolean,
    isShowingGallery: Boolean,
    onOptionClick: (Attachment, MediaGalleryPreviewOption) -> Unit,
    onRequestShareAttachment: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    config: MediaGalleryConfig = ChatTheme.mediaGalleryConfig,
    onHeaderLeadingContentClick: () -> Unit = {},
    onHeaderTrailingContentClick: () -> Unit = {},
    onFooterLeadingContentClick: (Attachment) -> Unit = onRequestShareAttachment,
    onFooterTrailingContentClick: (Attachment) -> Unit = {},
    onConfirmShareAttachment: (Attachment) -> Unit = {},
    onDismissShareAttachment: () -> Unit = {},
    onDismissOptionsMenu: () -> Unit = {},
    onDismissGallery: () -> Unit = {},
    header: @Composable (attachments: List<Attachment>, currentPage: Int) -> Unit = { _, _ ->
        MediaGalleryPreviewHeader(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            message = message,
            connectionState = connectionState,
            onLeadingContentClick = onHeaderLeadingContentClick,
            onTrailingContentClick = onHeaderTrailingContentClick,
        )
    },
    content: @Composable (
        padding: PaddingValues,
        pagerState: PagerState,
        attachments: List<Attachment>,
        onPlaybackError: () -> Unit,
    ) -> Unit = { padding, pagerState, attachments, onPlaybackError ->
        MediaGalleryPager(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.appBackground)
                .padding(padding),
            pagerState = pagerState,
            attachments = attachments,
            onPlaybackError = onPlaybackError,
        )
    },
    footer: @Composable (attachments: List<Attachment>, currentPage: Int) -> Unit = { attachments, currentPage ->
        MediaGalleryPreviewFooter(
            attachments = attachments,
            currentPage = currentPage,
            totalPages = attachments.size,
            connectionState = connectionState,
            isSharingInProgress = isSharingInProgress,
            onLeadingContentClick = onFooterLeadingContentClick,
            onTrailingContentClick = onFooterTrailingContentClick,
        )
    },
    optionsMenu: @Composable (
        attachment: Attachment,
        options: List<MediaGalleryPreviewOption>,
    ) -> Unit = { attachment, options ->
        MediaGalleryOptionsMenu(
            attachment = attachment,
            options = options,
            onOptionClick = onOptionClick,
            onDismiss = onDismissOptionsMenu,
        )
    },
) {
    // Filters out any link attachments. Pass this value along to all children
    // Composable-s that read message attachments to prevent inconsistent state.
    val filteredAttachments by remember(message) {
        derivedStateOf {
            message.attachments.filter { attachment -> !attachment.hasLink() }
        }
    }
    val startingPosition = if (initialPage !in filteredAttachments.indices) 0 else initialPage
    val pagerState = rememberPagerState(
        initialPage = startingPosition,
        pageCount = { filteredAttachments.size },
    )
    // Ensure the pager is scrolled to the correct page when the screen is first displayed
    LaunchedEffect(startingPosition) {
        if (startingPosition != pagerState.currentPage) {
            pagerState.scrollToPage(startingPosition)
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Full-size container holding the main scaffold and the overlay menus
    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                header(filteredAttachments, pagerState.currentPage)
            },
            bottomBar = {
                if (message.id.isNotEmpty()) {
                    footer(filteredAttachments, pagerState.currentPage)
                }
            },
        ) { padding ->
            if (message.id.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Main content
                    val playbackErrorText = stringResource(R.string.stream_ui_message_list_video_display_error)
                    content(padding, pagerState, filteredAttachments) {
                        // Show snackbar when playback error occurs
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar(
                                message = playbackErrorText,
                                duration = SnackbarDuration.Short,
                            )
                        }
                    }
                    // Error snackbar
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = padding.calculateBottomPadding()),
                    )
                }
                // Prompt the user to share a large file (if needed)
                if (promptedAttachment != null) {
                    ConfirmShareLargeFileDialog(
                        attachment = promptedAttachment,
                        onConfirm = {
                            onConfirmShareAttachment(promptedAttachment)
                            onDismissShareAttachment()
                        },
                        onDismiss = onDismissShareAttachment,
                    )
                }
            }
        }

        // Attachment options
        AnimatedVisibility(
            visible = isShowingOptions,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            if (pagerState.currentPage in filteredAttachments.indices) {
                val attachment = filteredAttachments[pagerState.currentPage]
                val options = defaultMediaOptions(currentUser, message, connectionState, config.optionsConfig)
                optionsMenu(attachment, options)
            }
        }

        // Gallery
        AnimatedVisibility(
            visible = isShowingGallery,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            MediaGalleryPhotosMenu(
                attachments = filteredAttachments,
                user = message.user,
                onClick = { index ->
                    onDismissGallery()
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                onDismiss = onDismissGallery,
            )
        }
    }
}

/**
 * Composable rendering the default header of the media gallery screen.
 * It consists of:
 * - Leading content (close icon)
 * - Center content (title)
 * - Trailing content (options icon)
 * Note: The default click actions are empty, and you need to provide them.
 *
 * @param message The message containing the attachments to be previewed.
 * @param connectionState The network connection state.
 * @param onLeadingContentClick Callback to be invoked when the leading content is clicked.
 * @param onTrailingContentClick Callback to be invoked when the trailing content is clicked.
 * @param modifier The [Modifier] to be applied to the header.
 * @param elevation The elevation of the header.
 * @param backgroundColor The background color of the header.
 * @param contentColor The content color of the header.
 * @param config The configuration for the media gallery.
 * @param leadingContent Composable function to render the leading content. By default, it renders a
 * [MediaGalleryPreviewCloseIcon], and binds it to the [onLeadingContentClick].
 * @param centerContent Composable function to render the center content. By default, it renders a
 * [MediaGalleryPreviewTitle].
 * @param trailingContent Composable function to render the trailing content. By default, it renders a
 * [MediaGalleryPreviewOptionsIcon], and binds it to the [onTrailingContentClick].
 */
@Composable
internal fun MediaGalleryPreviewHeader(
    message: Message,
    connectionState: ConnectionState,
    onLeadingContentClick: () -> Unit,
    onTrailingContentClick: () -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    backgroundColor: Color = ChatTheme.colors.barsBackground,
    contentColor: Color = ChatTheme.colors.textHighEmphasis,
    config: MediaGalleryConfig = ChatTheme.mediaGalleryConfig,
    leadingContent: @Composable (Modifier) -> Unit = {
        if (config.isCloseVisible) {
            MediaGalleryPreviewCloseIcon(
                modifier = it,
                onClick = onLeadingContentClick,
            )
        } else {
            Spacer(modifier = Modifier.minimumInteractiveComponentSize())
        }
    },
    centerContent: @Composable (Modifier) -> Unit = {
        MediaGalleryPreviewTitle(
            modifier = it,
            message = message,
            connectionState = connectionState,
        )
    },
    trailingContent: @Composable (Modifier) -> Unit = {
        if (config.isOptionsVisible) {
            MediaGalleryPreviewOptionsIcon(
                modifier = it,
                message = message,
                onClick = onTrailingContentClick,
            )
        } else {
            Spacer(modifier = Modifier.minimumInteractiveComponentSize())
        }
    },
) {
    Surface(
        modifier = modifier,
        shadowElevation = elevation,
        color = backgroundColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            leadingContent(Modifier)
            centerContent(Modifier.weight(1f))
            trailingContent(Modifier)
        }
    }
}

/**
 * A composable function rendering a pager for displaying images and videos. It is the default content of the
 * [MediaGalleryPreviewScreen].
 *
 * @param pagerState The [PagerState] for managing the pager's state. (passed from outside, so it can be also used by
 * the adjacent components. For example, to show the current position of the pager in the footer)
 * @param attachments The list of [Attachment]s to be displayed in the pager.
 * @param onPlaybackError Callback to be invoked when an error during the playing of a video occurs.
 * @param modifier The [Modifier] to be applied to the pager.
 */
@Composable
internal fun MediaGalleryPager(
    pagerState: PagerState,
    attachments: List<Attachment>,
    onPlaybackError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
    ) { page ->
        when {
            attachments[page].isImage() -> {
                MediaGalleryImagePage(
                    attachment = attachments[page],
                    pagerState = pagerState,
                    page = page,
                )
            }

            attachments[page].isVideo() -> {
                MediaGalleryVideoPage(
                    modifier = Modifier.fillMaxSize(),
                    assetUrl = attachments[page].assetUrl,
                    thumbnailUrl = attachments[page].thumbUrl,
                    onPlaybackError = onPlaybackError,
                )
            }
        }
    }
}

/**
 * Composable rendering the default footer of the media gallery screen.
 * It consists of:
 * - Leading content (share icon)
 * - Center content (page indicator or sharing progress)
 * - Trailing content (photos/gallery icon)
 *
 * @param attachments The list of [Attachment]s to be displayed in the pager.
 * @param currentPage The current page index in the pager.
 * @param totalPages The total number of pages in the pager.
 * @param connectionState The network connection state.
 * @param isSharingInProgress Indicates if the sharing process is in progress.
 * @param onLeadingContentClick Callback to be invoked when the leading content is clicked.
 * @param onTrailingContentClick Callback to be invoked when the trailing content is clicked.
 * @param modifier The [Modifier] to be applied to the footer.
 * @param elevation The elevation of the footer.
 * @param backgroundColor The background color of the footer.
 * @param contentColor The content color of the footer.
 * @param config The configuration for the media gallery.
 * @param leadingContent Composable function to render the leading content. By default, it renders a
 * [MediaGalleryPreviewShareIcon], and binds it to the [onLeadingContentClick].
 * @param centerContent Composable function to render the center content. By default, it renders a
 * [MediaGalleryPreviewSharingInProgressIndicator] if sharing is in progress, or [MediaGalleryPreviewPageIndicator] if
 * no sharing process is active.
 * @param trailingContent Composable function to render the trailing content. By default, it renders a
 * [MediaGalleryPreviewPhotosIcon], and binds it to the [onTrailingContentClick].
 */
@Suppress("LongParameterList")
@Composable
internal fun MediaGalleryPreviewFooter(
    attachments: List<Attachment>,
    currentPage: Int,
    totalPages: Int,
    connectionState: ConnectionState,
    isSharingInProgress: Boolean,
    onLeadingContentClick: (Attachment) -> Unit,
    onTrailingContentClick: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 4.dp,
    backgroundColor: Color = ChatTheme.colors.barsBackground,
    contentColor: Color = ChatTheme.colors.textHighEmphasis,
    config: MediaGalleryConfig = ChatTheme.mediaGalleryConfig,
    leadingContent: @Composable (Modifier) -> Unit = {
        if (config.isShareVisible) {
            MediaGalleryPreviewShareIcon(
                modifier = it,
                connectionState = connectionState,
                isSharingInProgress = isSharingInProgress,
                onClick = { onLeadingContentClick(attachments[currentPage]) },
            )
        } else {
            Spacer(modifier = Modifier.minimumInteractiveComponentSize())
        }
    },
    centerContent: @Composable (Modifier) -> Unit = {
        if (isSharingInProgress) {
            MediaGalleryPreviewSharingInProgressIndicator()
        } else {
            MediaGalleryPreviewPageIndicator(
                currentPage = currentPage,
                totalPages = totalPages,
            )
        }
    },
    trailingContent: @Composable (Modifier) -> Unit = {
        if (config.isGalleryVisible) {
            MediaGalleryPreviewPhotosIcon(
                modifier = it,
                onClick = { onTrailingContentClick(attachments[currentPage]) },
            )
        } else {
            Spacer(modifier = Modifier.minimumInteractiveComponentSize())
        }
    },
) {
    Surface(
        modifier = modifier,
        shadowElevation = elevation,
        color = backgroundColor,
        contentColor = contentColor,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            leadingContent(Modifier)
            centerContent(Modifier.weight(1f))
            trailingContent(Modifier)
        }
    }
}

/**
 * Composable rendering the close icon of the media gallery screen.
 *
 * @param onClick Callback to be invoked when the close icon is clicked.
 * @param modifier The [Modifier] to be applied to the close icon.
 */
@Composable
internal fun MediaGalleryPreviewCloseIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_close),
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
        )
    }
}

/**
 * Composable rendering the title section of the media gallery preview screen.
 *
 * This composable adapts its display based on the current connection state:
 * - When [ConnectionState.Connected]: Shows the message sender's name
 * - When [ConnectionState.Connecting]: Shows a loading indicator
 * - When [ConnectionState.Offline]: Shows "Disconnected" text
 *
 * In all cases, it displays a timestamp below the main content, using either
 * the message's updated time or created time, falling back to the current date
 * if neither is available.
 *
 * @param message The message containing user and timestamp information.
 * @param connectionState The current network connection state.
 * @param modifier Optional modifier applied to the Column container.
 */
@Composable
internal fun MediaGalleryPreviewTitle(
    message: Message,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val textStyle = ChatTheme.typography.title3Bold

        when (connectionState) {
            is ConnectionState.Connected -> Text(
                text = message.user.name,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            is ConnectionState.Connecting -> NetworkLoadingIndicator(
                textStyle = textStyle,
            )

            is ConnectionState.Offline -> Text(
                text = stringResource(id = R.string.stream_compose_disconnected),
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        val timestamp = message.updatedAt ?: message.createdAt
        if (timestamp != null) {
            Timestamp(date = timestamp)
        }
    }
}

/**
 * Composable rendering the options icon of the media gallery screen.
 *
 * @param message The message containing the attachments to be previewed. The button will be disabled if the message is
 * not fully loaded ([message#id] is empty).
 * @param onClick Callback to be invoked when the options icon is clicked.
 * @param modifier The [Modifier] to be applied to the options icon.
 */
@Composable
internal fun MediaGalleryPreviewOptionsIcon(
    message: Message,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = message.id.isNotEmpty()
    IconButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_menu_vertical),
            contentDescription = stringResource(R.string.stream_compose_image_options),
        )
    }
}

/**
 * Composable rendering the share icon of the media gallery screen.
 *
 * This composable adapts its display and behavior based on the current state:
 * - When [isSharingInProgress] is true: Shows a cancel share icon
 * - When [isSharingInProgress] is false: Shows a share icon
 *
 * The button is enabled only when the connection state is [ConnectionState.Connected].
 * The icon color changes based on whether the button is enabled or disabled.
 *
 * @param connectionState The current network connection state.
 * @param isSharingInProgress Whether a sharing operation is currently in progress.
 * @param onClick Callback to be invoked when the icon is clicked.
 * @param modifier The [Modifier] to be applied to the icon button.
 */
@Composable
internal fun MediaGalleryPreviewShareIcon(
    connectionState: ConnectionState,
    isSharingInProgress: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = connectionState is ConnectionState.Connected
    IconButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onClick,
    ) {
        val painter = if (isSharingInProgress) {
            R.drawable.stream_compose_ic_clear
        } else {
            R.drawable.stream_compose_ic_share
        }
        Icon(
            painter = painterResource(id = painter),
            contentDescription = stringResource(id = R.string.stream_compose_image_preview_share),
        )
    }
}

/**
 * Composable rendering the page indicator for media gallery preview.
 *
 * Displays the current position within the gallery, showing "X of Y" format
 * where X is the current page (1-based index) and Y is the total number of pages.
 *
 * @param currentPage Zero-based index of the current page being displayed
 * @param totalPages Total number of pages in the gallery
 */
@Composable
internal fun MediaGalleryPreviewPageIndicator(
    currentPage: Int,
    totalPages: Int,
) {
    val text = stringResource(id = R.string.stream_compose_image_order, currentPage + 1, totalPages)
    Text(
        text = text,
        style = ChatTheme.typography.title3Bold,
    )
}

/**
 * Composable rendering the sharing progress indicator for media gallery preview.
 *
 * Displays a horizontal layout with a circular progress indicator followed by
 * "Preparing..." text, indicating that a media sharing operation is in progress.
 */
@Composable
internal fun MediaGalleryPreviewSharingInProgressIndicator() {
    Row {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(24.dp),
            strokeWidth = 2.dp,
            color = ChatTheme.colors.primaryAccent,
        )
        Text(
            text = stringResource(id = R.string.stream_compose_media_gallery_preview_preparing),
            style = ChatTheme.typography.title3Bold,
        )
    }
}

/**
 * Composable rendering the photos icon of the media gallery screen.
 *
 * @param onClick Callback to be invoked when the photos icon is clicked.
 * @param modifier The [Modifier] to be applied to the photos icon.
 */
@Composable
internal fun MediaGalleryPreviewPhotosIcon(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_gallery),
            contentDescription = stringResource(id = R.string.stream_compose_image_preview_photos),
        )
    }
}

/**
 * Composable displaying a confirmation dialog when attempting to share a large media file.
 *
 * Shows a simple dialog with a title and message informing the user about the file size
 * (displayed in MB), allowing them to confirm or cancel the sharing operation. The dialog
 * uses localized strings from resources for the title and message content.
 *
 * @param attachment The attachment being shared, used to determine the file size.
 * @param onConfirm Callback invoked when the user confirms they want to share the large file.
 * @param onDismiss Callback invoked when the user cancels the sharing operation.
 */
@Composable
private fun ConfirmShareLargeFileDialog(
    attachment: Attachment,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    SimpleDialog(
        title = stringResource(R.string.stream_compose_media_gallery_share_large_file_prompt_title),
        message = stringResource(
            R.string.stream_compose_media_gallery_share_large_file_prompt_message,
            (attachment.fileSize.toFloat() / BytesInMegabyte),
        ),
        onPositiveAction = onConfirm,
        onDismiss = onDismiss,
    )
}

/**
 * Constant representing the number of bytes in a megabyte.
 */
private const val BytesInMegabyte = 1024 * 1024

@Composable
@Preview
private fun MediaGalleryPreviewCloseIconPreview() {
    ChatPreviewTheme {
        Surface {
            MediaGalleryPreviewCloseIcon(onClick = {})
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewOptionsIconPreview() {
    ChatPreviewTheme {
        Surface {
            val message = Message(
                id = "messageId",
                text = "Hello!",
                user = User(id = "solo", name = "Han Solo"),
            )
            MediaGalleryPreviewOptionsIcon(
                message = message,
                onClick = {},
            )
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewTitlePreview() {
    ChatPreviewTheme {
        Surface {
            val message = Message(
                id = "messageId",
                text = "Hello!",
                user = User(id = "solo", name = "Han Solo"),
                createdAt = Date(),
            )
            val connectionState = ConnectionState.Connected
            MediaGalleryPreviewTitle(
                message = message,
                connectionState = connectionState,
            )
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewHeaderPreview() {
    ChatPreviewTheme {
        Surface {
            val message = Message(
                id = "messageId",
                text = "Hello!",
                user = User(id = "solo", name = "Han Solo"),
                createdAt = Date(),
            )
            val connectionState = ConnectionState.Connected
            MediaGalleryPreviewHeader(
                message = message,
                connectionState = connectionState,
                onLeadingContentClick = {},
                onTrailingContentClick = {},
            )
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewShareButtonPreview() {
    ChatPreviewTheme {
        Surface {
            Row {
                val connectionState = ConnectionState.Connected
                MediaGalleryPreviewShareIcon(
                    connectionState = connectionState,
                    isSharingInProgress = false,
                    onClick = {},
                )
                MediaGalleryPreviewShareIcon(
                    connectionState = connectionState,
                    isSharingInProgress = true,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewPhotosButtonPreview() {
    ChatPreviewTheme {
        Surface {
            MediaGalleryPreviewPhotosIcon(onClick = {})
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewPageIndicatorPreview() {
    ChatPreviewTheme {
        Surface {
            MediaGalleryPreviewPageIndicator(
                currentPage = 0,
                totalPages = 3,
            )
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewSharingInProgressIndicatorPreview() {
    ChatPreviewTheme {
        Surface {
            MediaGalleryPreviewSharingInProgressIndicator()
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewFooterPreview() {
    ChatPreviewTheme {
        Surface {
            Column {
                MediaGalleryPreviewFooter(
                    attachments = emptyList(),
                    currentPage = 0,
                    totalPages = 3,
                    connectionState = ConnectionState.Connected,
                    isSharingInProgress = false,
                    onLeadingContentClick = {},
                    onTrailingContentClick = {},
                )
            }
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewFooterSharingInProgressPreview() {
    ChatPreviewTheme {
        Surface {
            Column {
                MediaGalleryPreviewFooter(
                    attachments = emptyList(),
                    currentPage = 0,
                    totalPages = 3,
                    connectionState = ConnectionState.Connected,
                    isSharingInProgress = true,
                    onLeadingContentClick = {},
                    onTrailingContentClick = {},
                )
            }
        }
    }
}

@Composable
@Preview
private fun MediaGalleryPreviewScreenPreview() {
    ChatPreviewTheme {
        Surface {
            val user = User(id = "solo", name = "Han Solo")
            val message = Message(
                id = "messageId",
                text = "Hello!",
                user = user,
                attachments = listOf(
                    Attachment(
                        type = "image",
                        mimeType = "image/jpeg",
                        imageUrl = "https://example.com/image1.jpg",
                        thumbUrl = "https://example.com/thumb1.jpg",
                    ),
                    Attachment(
                        type = "video",
                        mimeType = "video/mp4",
                        imageUrl = "https://example.com/image1.jpg",
                        thumbUrl = "https://example.com/thumb1.jpg",
                    ),
                ),
                createdAt = Date(),
            )
            MediaGalleryPreviewScreen(
                message = message,
                connectionState = ConnectionState.Connected,
                currentUser = user,
                initialPage = 0,
                promptedAttachment = null,
                isShowingOptions = true,
                isShowingGallery = false,
                isSharingInProgress = false,
                onOptionClick = { _, _ -> },
                onRequestShareAttachment = {},
            )
        }
    }
}
