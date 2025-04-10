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

package io.getstream.chat.android.compose.ui.attachments.preview

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil3.compose.AsyncImagePainter
import coil3.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.attachment.isImage
import io.getstream.chat.android.client.utils.attachment.isVideo
import io.getstream.chat.android.client.utils.message.isDeleted
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.mediagallerypreview.Delete
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewAction
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewActivityState
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewOption
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResult
import io.getstream.chat.android.compose.state.mediagallerypreview.MediaGalleryPreviewResultType
import io.getstream.chat.android.compose.state.mediagallerypreview.Reply
import io.getstream.chat.android.compose.state.mediagallerypreview.SaveMedia
import io.getstream.chat.android.compose.state.mediagallerypreview.ShowInChat
import io.getstream.chat.android.compose.state.mediagallerypreview.toMediaGalleryPreviewActivityState
import io.getstream.chat.android.compose.state.mediagallerypreview.toMessage
import io.getstream.chat.android.compose.ui.attachments.content.PlayButton
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.ShimmerProgressIndicator
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.isCompleted
import io.getstream.chat.android.compose.util.attachmentDownloadState
import io.getstream.chat.android.compose.util.onDownloadHandleRequest
import io.getstream.chat.android.compose.viewmodel.mediapreview.MediaGalleryPreviewViewModel
import io.getstream.chat.android.compose.viewmodel.mediapreview.MediaGalleryPreviewViewModelFactory
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.streamcdn.image.StreamCdnCropImageMode
import io.getstream.chat.android.models.streamcdn.image.StreamCdnResizeImageMode
import io.getstream.chat.android.ui.common.helper.DefaultDownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DownloadAttachmentUriGenerator
import io.getstream.chat.android.ui.common.helper.DownloadRequestInterceptor
import io.getstream.chat.android.ui.common.images.internal.StreamImageLoader
import io.getstream.chat.android.ui.common.images.resizing.StreamCdnImageResizing
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.StreamFileUtil
import io.getstream.chat.android.ui.common.utils.extensions.imagePreviewUrl
import io.getstream.chat.android.ui.common.utils.extensions.initials
import io.getstream.chat.android.uiutils.extension.hasLink
import io.getstream.result.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import kotlin.math.abs

/**
 * Shows an image and video previews along with enabling
 * the user to perform various actions such as image or file deletion.
 */
public class MediaGalleryPreviewActivity : AppCompatActivity() {

    /**
     * Factory used to build the screen ViewModel given the received message ID.
     */
    private val factory by lazy {
        val messageId = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(
                KeyMediaGalleryPreviewActivityState, MediaGalleryPreviewActivityState::class.java,
            )?.messageId
        } else {
            intent?.getParcelableExtra<MediaGalleryPreviewActivityState>(
                KeyMediaGalleryPreviewActivityState,
            )?.messageId
        } ?: ""

        MediaGalleryPreviewViewModelFactory(
            chatClient = ChatClient.instance(),
            messageId = messageId,
            skipEnrichUrl = intent?.getBooleanExtra(KeySkipEnrichUrl, false) ?: false,
        )
    }

    /**
     * Holds a job used to share an image or a file.
     */
    private var fileSharingJob: Job? = null

    /**
     * The current state of the screen.
     */
    private var uiState: MediaGalleryPreviewActivityState? = null

    /**
     * The ViewModel that exposes screen data.
     */
    private val mediaGalleryPreviewViewModel by viewModels<MediaGalleryPreviewViewModel>(factoryProducer = { factory })

    /**
     * Sets up the data required to show the previews of images or videos within the given message.
     *
     * Immediately finishes in case the data is invalid.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ChatClient.isInitialized.not()) {
            finish()
            return
        }

        uiState = savedInstanceState?.getParcelable(
            KeyMediaGalleryPreviewActivityState,
        ) ?: intent?.getParcelableExtra(KeyMediaGalleryPreviewActivityState)

        val videoThumbnailsEnabled = intent?.getBooleanExtra(KeyVideoThumbnailsEnabled, true) ?: true
        val streamCdnImageResizing = intent?.createStreamCdnImageResizing()
            ?: StreamCdnImageResizing.defaultStreamCdnImageResizing()
        val messageId = uiState?.messageId ?: ""

        if (!mediaGalleryPreviewViewModel.hasCompleteMessage) {
            val message = uiState?.toMessage()

            if (message != null) {
                mediaGalleryPreviewViewModel.message = message
            }
        }

        val attachmentPosition = intent?.getIntExtra(KeyAttachmentPosition, 0) ?: 0

        if (messageId.isBlank()) {
            throw IllegalArgumentException("Missing messageId necessary to load images.")
        }

        setContent {
            ChatTheme(
                videoThumbnailsEnabled = videoThumbnailsEnabled,
                streamCdnImageResizing = streamCdnImageResizing,
                downloadAttachmentUriGenerator = downloadAttachmentUriGenerator,
                downloadRequestInterceptor = downloadRequestInterceptor,
            ) {
                SetupEdgeToEdge()

                val message = mediaGalleryPreviewViewModel.message

                if (message.isDeleted()) {
                    finish()
                    return@ChatTheme
                }

                MediaGalleryPreviewContentWrapper(message, attachmentPosition)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        uiState?.also {
            outState.putParcelable(KeyMediaGalleryPreviewActivityState, it)
        }
    }

    @Composable
    private fun SetupEdgeToEdge() {
        val nightMode = isSystemInDarkTheme()
        val systemBarsColor = ChatTheme.colors.barsBackground.toArgb()
        LaunchedEffect(nightMode) {
            val style = if (nightMode) {
                SystemBarStyle.dark(systemBarsColor)
            } else {
                SystemBarStyle.light(systemBarsColor, systemBarsColor)
            }
            enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
        }
    }

    /**
     * Wraps the content of the screen in a composable that represents the top and bottom bars and the
     * image and video previews.
     *
     * @param message The message to show the attachments from.
     * @param initialAttachmentPosition The initial pager position, based on the attachment preview
     * the user clicked on.
     */
    @Suppress("MagicNumber", "LongMethod")
    @Composable
    private fun MediaGalleryPreviewContentWrapper(
        message: Message,
        initialAttachmentPosition: Int,
    ) {
        // Filters out any link attachments. Pass this value along to all children
        // Composables that read message attachments to prevent inconsistent state.
        val filteredAttachments = message.attachments.filter { attachment ->
            !attachment.hasLink()
        }

        val startingPosition =
            if (initialAttachmentPosition !in filteredAttachments.indices) 0 else initialAttachmentPosition

        val pagerState = rememberPagerState(
            initialPage = startingPosition,
            pageCount = { filteredAttachments.size },
        )

        val coroutineScope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.barsBackground)
                .windowInsetsPadding(WindowInsets.systemBars),
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { MediaGalleryPreviewTopBar(message) },
                content = { contentPadding ->
                    if (message.id.isNotEmpty()) {
                        Box(Modifier.fillMaxSize()) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(contentPadding),
                            ) {
                                MediaPreviewContent(pagerState, filteredAttachments) {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = getString(R.string.stream_ui_message_list_video_display_error),
                                            duration = SnackbarDuration.Short,
                                        )
                                    }
                                }
                            }
                            SnackbarHost(
                                hostState = snackbarHostState,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = contentPadding.calculateBottomPadding()),
                            )
                        }

                        val promptedAttachment = mediaGalleryPreviewViewModel.promptedAttachment

                        if (promptedAttachment != null) {
                            SimpleDialog(
                                title = getString(
                                    R.string.stream_compose_media_gallery_share_large_file_prompt_title,
                                ),
                                message = getString(
                                    R.string.stream_compose_media_gallery_share_large_file_prompt_message,
                                    (promptedAttachment.fileSize.toFloat() / (1024 * 1024)),
                                ),
                                onPositiveAction = remember(mediaGalleryPreviewViewModel) {
                                    {
                                        shareAttachment(promptedAttachment)
                                        mediaGalleryPreviewViewModel.promptedAttachment = null
                                    }
                                },
                                onDismiss = remember(mediaGalleryPreviewViewModel) {
                                    {
                                        mediaGalleryPreviewViewModel.promptedAttachment = null
                                    }
                                },
                            )
                        }
                    }
                },
                bottomBar = {
                    if (message.id.isNotEmpty()) {
                        MediaGalleryPreviewBottomBar(filteredAttachments, pagerState)
                    }
                },
            )

            AnimatedVisibility(
                visible = mediaGalleryPreviewViewModel.isShowingOptions,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                MediaGalleryPreviewOptions(
                    options = defaultMediaOptions(message = message),
                    pagerState = pagerState,
                    attachments = filteredAttachments,
                    modifier = Modifier.animateEnterExit(
                        enter = slideInVertically(),
                        exit = slideOutVertically(),
                    ),
                )
            }

            if (message.id.isNotEmpty()) {
                AnimatedVisibility(
                    visible = mediaGalleryPreviewViewModel.isShowingGallery,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    MediaGallery(
                        pagerState = pagerState,
                        attachments = filteredAttachments,
                        modifier = Modifier.animateEnterExit(
                            enter = slideInVertically(initialOffsetY = { height -> height / 2 }),
                            exit = slideOutVertically(targetOffsetY = { height -> height / 2 }),
                        ),
                    )
                }
            }
        }
    }

    /**
     * The top bar which allows the user to go back or browse more screen options.
     *
     * @param message The message used for info and actions.
     */
    @Composable
    private fun MediaGalleryPreviewTopBar(message: Message) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shadowElevation = 4.dp,
            color = ChatTheme.colors.barsBackground,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = ::finish) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_close),
                        contentDescription = stringResource(id = R.string.stream_compose_cancel),
                        tint = ChatTheme.colors.textHighEmphasis,
                    )
                }

                MediaGalleryPreviewHeaderTitle(
                    modifier = Modifier.weight(8f),
                    message = message,
                )

                MediaGalleryPreviewOptionsToggle(
                    modifier = Modifier.weight(1f),
                    message = message,
                )
            }
        }
    }

    /**
     * Represents the header title that shows more information about the media attachments.
     *
     * @param message The message with the media attachments we're observing.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun MediaGalleryPreviewHeaderTitle(
        message: Message,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            val textStyle = ChatTheme.typography.title3Bold
            val textColor = ChatTheme.colors.textHighEmphasis

            when (mediaGalleryPreviewViewModel.connectionState) {
                is ConnectionState.Connected -> Text(
                    text = message.user.name,
                    style = textStyle,
                    color = textColor,
                )

                is ConnectionState.Connecting -> NetworkLoadingIndicator(
                    textStyle = textStyle,
                    textColor = textColor,
                )

                is ConnectionState.Offline -> Text(
                    text = getString(R.string.stream_compose_disconnected),
                    style = textStyle,
                    color = textColor,
                )
            }

            Timestamp(date = message.updatedAt ?: message.createdAt ?: Date())
        }
    }

    /**
     * Toggles the media attachments options menu.
     *
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun MediaGalleryPreviewOptionsToggle(
        message: Message,
        modifier: Modifier = Modifier,
    ) {
        Icon(
            modifier = modifier
                .size(24.dp)
                .clickable(
                    bounded = false,
                    enabled = message.id.isNotEmpty(),
                    onClick = { mediaGalleryPreviewViewModel.toggleMediaOptions(isShowingOptions = true) },
                ),
            painter = painterResource(id = R.drawable.stream_compose_ic_menu_vertical),
            contentDescription = stringResource(R.string.stream_compose_image_options),
            tint = if (message.id.isNotEmpty()) ChatTheme.colors.textHighEmphasis else ChatTheme.colors.disabled,
        )
    }

    /**
     * The media attachment options menu, used to perform different actions for the currently active media
     * attachment.
     *
     * @param options The options available for the attachment.
     * @param pagerState The state of the pager, used to fetch the current attachment.
     * @param attachments The list of attachments for which we display options.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun MediaGalleryPreviewOptions(
        options: List<MediaGalleryPreviewOption>,
        pagerState: PagerState,
        attachments: List<Attachment>,
        modifier: Modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.overlay)
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleMediaOptions(
                                isShowingOptions = false,
                            )
                        }
                    },
                ),
        ) {
            Surface(
                modifier = modifier
                    .padding(16.dp)
                    .width(150.dp)
                    .wrapContentHeight()
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 4.dp,
                color = ChatTheme.colors.barsBackground,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        MediaGalleryPreviewOptionItem(
                            mediaGalleryPreviewOption = option,
                            pagerState = pagerState,
                            attachments = attachments,
                        )

                        if (index != options.lastIndex) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(ChatTheme.colors.borders),
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Represents each item in the media options menu that the user can pick.
     *
     * @param mediaGalleryPreviewOption The option information to show.
     * @param pagerState The state of the pager, used to handle selected actions.
     * @param attachments The list of attachments for which we display options.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun MediaGalleryPreviewOptionItem(
        mediaGalleryPreviewOption: MediaGalleryPreviewOption,
        pagerState: PagerState,
        attachments: List<Attachment>,
    ) {
        val (writePermissionState, downloadPayload) = attachmentDownloadState()
        val context = LocalContext.current
        val downloadAttachmentUriGenerator = ChatTheme.streamDownloadAttachmentUriGenerator
        val downloadRequestInterceptor = ChatTheme.streamDownloadRequestInterceptor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.barsBackground)
                .padding(8.dp)
                .clickable(
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleMediaOptions(isShowingOptions = false)

                            handleMediaAction(
                                context = context,
                                mediaGalleryPreviewAction = mediaGalleryPreviewOption.action,
                                currentPage = pagerState.currentPage,
                                writePermissionState = writePermissionState,
                                downloadPayload = downloadPayload,
                                attachments = attachments,
                                generateDownloadUri = downloadAttachmentUriGenerator::generateDownloadUri,
                                interceptRequest = downloadRequestInterceptor::intercept,
                            )
                        }
                    },
                    enabled = mediaGalleryPreviewOption.isEnabled,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                modifier = Modifier
                    .size(18.dp),
                painter = mediaGalleryPreviewOption.iconPainter,
                tint = mediaGalleryPreviewOption.iconColor,
                contentDescription = mediaGalleryPreviewOption.title,
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = mediaGalleryPreviewOption.title,
                color = mediaGalleryPreviewOption.titleColor,
                style = ChatTheme.typography.bodyBold,
                fontSize = 12.sp,
            )
        }
    }

    /**
     * Consumes the action user selected to perform for the current media attachment.
     *
     * @param context The [Context] used to ask for handling permissions.
     * @param mediaGalleryPreviewAction The action the user selected.
     * @param currentPage The index of the current media attachment.
     * @param attachments The list of attachments for which actions need to be handled.
     * @param writePermissionState The current state of permissions.
     * @param downloadPayload The attachment to be downloaded.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Suppress("LongParameterList")
    private fun handleMediaAction(
        context: Context,
        mediaGalleryPreviewAction: MediaGalleryPreviewAction,
        currentPage: Int,
        attachments: List<Attachment>,
        writePermissionState: PermissionState,
        downloadPayload: MutableState<Attachment?>,
        generateDownloadUri: (Attachment) -> Uri,
        interceptRequest: DownloadManager.Request.() -> Unit,
    ) {
        val message = mediaGalleryPreviewAction.message

        when (mediaGalleryPreviewAction) {
            is ShowInChat -> {
                handleResult(
                    MediaGalleryPreviewResult(
                        messageId = message.id,
                        parentMessageId = message.parentId,
                        resultType = MediaGalleryPreviewResultType.SHOW_IN_CHAT,
                    ),
                )
            }

            is Reply -> {
                handleResult(
                    MediaGalleryPreviewResult(
                        messageId = message.id,
                        parentMessageId = message.parentId,
                        resultType = MediaGalleryPreviewResultType.QUOTE,
                    ),
                )
            }

            is Delete -> mediaGalleryPreviewViewModel.deleteCurrentMediaAttachment(attachments[currentPage])
            is SaveMedia -> {
                onDownloadHandleRequest(
                    context = context,
                    payload = attachments[currentPage],
                    permissionState = writePermissionState,
                    downloadPayload = downloadPayload,
                    generateDownloadUri = generateDownloadUri,
                    interceptRequest = interceptRequest,
                )
            }
        }
    }

    /**
     * Prepares and sets the result of this Activity and propagates it back to the user.
     *
     * @param result The chosen action result.
     */
    private fun handleResult(result: MediaGalleryPreviewResult) {
        val data = Intent().apply {
            putExtra(KeyMediaGalleryPreviewResult, result)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    /**
     * Renders a horizontal pager that shows images and allows the user to swipe, zoom and pan through them.
     *
     * @param pagerState The state of the content pager.
     * @param attachments The attachments to show within the pager.
     */
    @Suppress("LongMethod", "ComplexMethod")
    @Composable
    private fun MediaPreviewContent(
        pagerState: PagerState,
        attachments: List<Attachment>,
        onPlaybackError: () -> Unit,
    ) {
        if (attachments.isEmpty()) {
            finish()
            return
        }

        HorizontalPager(
            modifier = Modifier.background(ChatTheme.colors.appBackground),
            state = pagerState,
        ) { page ->
            if (attachments[page].isImage()) {
                ImagePreviewContent(attachment = attachments[page], pagerState = pagerState, page = page)
            } else if (attachments[page].isVideo()) {
                VideoPreviewContent(
                    attachment = attachments[page],
                    pagerState = pagerState,
                    page = page,
                    onPlaybackError = onPlaybackError,
                )
            }
        }
    }

    /**
     * Represents an individual page containing an image that is zoomable and scrollable.
     *
     * @param attachment The image attachment to be displayed.
     * @param pagerState The state of the pager that contains this page
     * @param page The page an instance of this content is located on.
     */
    @Composable
    private fun ImagePreviewContent(
        attachment: Attachment,
        pagerState: PagerState,
        page: Int,
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val data = attachment.imagePreviewUrl
            val context = LocalContext.current
            val imageRequest = remember {
                ImageRequest.Builder(context)
                    .data(data)
                    .build()
            }

            var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

            val density = LocalDensity.current
            val parentSize = Size(density.run { maxWidth.toPx() }, density.run { maxHeight.toPx() })
            var imageSize by remember { mutableStateOf(Size(0f, 0f)) }

            var currentScale by remember { mutableFloatStateOf(DefaultZoomScale) }
            var translation by remember { mutableStateOf(Offset(0f, 0f)) }

            val scale by animateFloatAsState(targetValue = currentScale, label = "")

            val transformModifier = if (imageState is AsyncImagePainter.State.Success) {
                val state = imageState as AsyncImagePainter.State.Success
                val size = Size(
                    width = state.result.image.width.toFloat(),
                    height = state.result.image.height.toFloat(),
                )
                Modifier
                    .aspectRatio(size.width / size.height, true)
                    .background(color = ChatTheme.colors.overlay)
            } else {
                Modifier
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                StreamAsyncImage(
                    imageRequest = imageRequest,
                    modifier = transformModifier
                        .graphicsLayer(
                            scaleY = scale,
                            scaleX = scale,
                            translationX = translation.x,
                            translationY = translation.y,
                        )
                        .onGloballyPositioned {
                            imageSize = Size(it.size.width.toFloat(), it.size.height.toFloat())
                        }
                        .pointerInput(Unit) {
                            coroutineScope {
                                awaitEachGesture {
                                    awaitFirstDown(requireUnconsumed = true)
                                    do {
                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                                        val zoom = event.calculateZoom()
                                        currentScale = (zoom * currentScale).coerceAtMost(MaxZoomScale)

                                        val maxTranslation = calculateMaxOffset(
                                            imageSize = imageSize,
                                            scale = currentScale,
                                            parentSize = parentSize,
                                        )

                                        val offset = event.calculatePan()
                                        val newTranslationX = translation.x + offset.x * currentScale
                                        val newTranslationY = translation.y + offset.y * currentScale

                                        translation = Offset(
                                            newTranslationX.coerceIn(-maxTranslation.x, maxTranslation.x),
                                            newTranslationY.coerceIn(-maxTranslation.y, maxTranslation.y),
                                        )

                                        if (abs(newTranslationX) < calculateMaxOffsetPerAxis(
                                                imageSize.width,
                                                currentScale,
                                                parentSize.width,
                                            ) || zoom != DefaultZoomScale
                                        ) {
                                            event.changes.forEach { it.consume() }
                                        }
                                    } while (event.changes.any { it.pressed })

                                    if (currentScale < DefaultZoomScale) {
                                        currentScale = DefaultZoomScale
                                    }
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            coroutineScope {
                                awaitEachGesture {
                                    awaitFirstDown()
                                    withTimeoutOrNull(DoubleTapTimeoutMs) {
                                        awaitFirstDown()
                                        currentScale = when {
                                            currentScale == MaxZoomScale -> DefaultZoomScale
                                            currentScale >= MidZoomScale -> MaxZoomScale
                                            else -> MidZoomScale
                                        }

                                        if (currentScale == DefaultZoomScale) {
                                            translation = Offset(0f, 0f)
                                        }
                                    }
                                }
                            }
                        },
                ) { asyncImageState ->
                    imageState = asyncImageState

                    Crossfade(targetState = asyncImageState) { state ->
                        when (state) {
                            is AsyncImagePainter.State.Empty,
                            is AsyncImagePainter.State.Loading,
                            -> ShimmerProgressIndicator(
                                modifier = Modifier.fillMaxSize(),
                            )

                            is AsyncImagePainter.State.Success,
                            -> Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = state.painter,
                                contentDescription = null,
                            )

                            is AsyncImagePainter.State.Error,
                            -> ErrorIcon(modifier = Modifier.fillMaxSize())
                        }
                    }
                }

                Log.d("isCurrentPage", "${page != pagerState.currentPage}")

                if (pagerState.currentPage != page) {
                    currentScale = DefaultZoomScale
                    translation = Offset(0f, 0f)
                }
            }
        }
    }

    /**
     * Represents an individual page containing video player with media controls.
     *
     * @param attachment The video attachment to be played.
     * @param pagerState The state of the pager that contains this page
     * @param page The page an instance of this content is located on.
     * @param onPlaybackError Handler for playback errors.
     */
    @Composable
    private fun VideoPreviewContent(
        attachment: Attachment,
        pagerState: PagerState,
        page: Int,
        onPlaybackError: () -> Unit,
    ) {
        val context = LocalContext.current

        var hasPrepared by remember {
            mutableStateOf(false)
        }

        var userHasClickedPlay by remember {
            mutableStateOf(false)
        }

        var shouldShowProgressBar by remember {
            mutableStateOf(false)
        }

        var shouldShowPreview by remember {
            mutableStateOf(true)
        }

        var shouldShowPlayButton by remember {
            mutableStateOf(true)
        }

        val mediaController = remember {
            createMediaController(context)
        }

        val videoView = remember {
            VideoView(context)
        }

        val contentView = remember {
            val frameLayout = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
            videoView.apply {
                setVideoURI(Uri.parse(attachment.assetUrl))
                this.setMediaController(mediaController)
                setOnErrorListener { _, _, _ ->
                    shouldShowProgressBar = false
                    onPlaybackError()
                    true
                }
                setOnPreparedListener {
                    // Don't remove the preview unless the user has clicked play previously,
                    // otherwise the preview will be removed whenever the video has finished downloading.
                    if (!hasPrepared && userHasClickedPlay && page == pagerState.currentPage) {
                        shouldShowProgressBar = false
                        shouldShowPreview = false
                        mediaController.show()
                    }
                    hasPrepared = true
                }

                mediaController.setAnchorView(frameLayout)

                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ).apply {
                    gravity = Gravity.CENTER
                }
            }

            frameLayout.apply {
                addView(videoView)
            }
        }

        Box(contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                factory = { contentView },
            )

            if (shouldShowPreview) {
                val data = if (ChatTheme.videoThumbnailsEnabled) {
                    attachment.thumbUrl
                } else {
                    null
                }

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    StreamAsyncImage(
                        modifier = Modifier
                            .clickable {
                                shouldShowProgressBar = true
                                shouldShowPlayButton = false
                                userHasClickedPlay = true
                                // Don't remove the preview unless the player
                                // is ready to play.
                                if (hasPrepared) {
                                    shouldShowProgressBar = false
                                    shouldShowPreview = false
                                    mediaController.show()
                                }
                                videoView.start()
                            }
                            .fillMaxSize()
                            .background(color = Color.Black),
                        data = data,
                        contentDescription = null,
                    )

                    if (shouldShowPlayButton) {
                        PlayButton(
                            modifier = Modifier
                                .shadow(6.dp, shape = CircleShape)
                                .background(color = Color.White, shape = CircleShape)
                                .size(
                                    width = 42.dp,
                                    height = 42.dp,
                                ),
                            contentDescription = getString(R.string.stream_compose_cd_play_button),
                        )
                    }
                }
            }

            if (shouldShowProgressBar) {
                LoadingIndicator()
            }
        }

        if (page != pagerState.currentPage) {
            shouldShowPlayButton = true
            shouldShowPreview = true
            shouldShowProgressBar = false
            mediaController.hide()
        }
    }

    /**
     * Creates a custom instance of [MediaController].
     *
     * @param context The Context used to create the [MediaController].
     */
    private fun createMediaController(
        context: Context,
    ): MediaController {
        return object : MediaController(context) {}
    }

    /**
     * Calculates max offset that an image can have before reaching the edges.
     *
     * @param imageSize The size of the image that is being viewed.
     * @param scale The current scale of the image that is being viewed.
     * @param parentSize The size of the view containing the image being viewed.
     */
    private fun calculateMaxOffset(imageSize: Size, scale: Float, parentSize: Size): Offset {
        val maxTranslationY = calculateMaxOffsetPerAxis(imageSize.height, scale, parentSize.height)
        val maxTranslationX = calculateMaxOffsetPerAxis(imageSize.width, scale, parentSize.width)
        return Offset(maxTranslationX, maxTranslationY)
    }

    /**
     * Calculates max offset an image can have on a single axis.
     *
     * @param axisSize The size of the image on a given axis.
     * @param scale The current scale of of the image.
     * @param parentAxisSize The size of the parent view on a given axis.
     */
    private fun calculateMaxOffsetPerAxis(axisSize: Float, scale: Float, parentAxisSize: Float): Float {
        return (axisSize * scale - parentAxisSize).coerceAtLeast(0f) / 2
    }

    /**
     * Represents the bottom bar which holds more options and information about the current
     * media attachment.
     *
     * @param attachments The attachments to use for the UI state and options.
     * @param pagerState The state of the pager, used for current page information.
     */
    @Suppress("LongMethod")
    @Composable
    private fun MediaGalleryPreviewBottomBar(attachments: List<Attachment>, pagerState: PagerState) {
        val attachmentCount = attachments.size

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shadowElevation = 4.dp,
            color = ChatTheme.colors.barsBackground,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            val attachment = attachments[pagerState.currentPage]

                            when {
                                mediaGalleryPreviewViewModel.isSharingInProgress -> {
                                    fileSharingJob?.cancel()
                                    mediaGalleryPreviewViewModel.isSharingInProgress = false
                                }

                                attachment.fileSize >= MaxUnpromptedFileSize -> {
                                    val result = StreamFileUtil.getFileFromCache(
                                        context = applicationContext,
                                        attachment = attachment,
                                    )

                                    when (result) {
                                        is Result.Success -> shareAttachment(
                                            mediaUri = result.value,
                                            attachmentType = attachment.type,
                                        )

                                        is Result.Failure ->
                                            mediaGalleryPreviewViewModel.promptedAttachment =
                                                attachment
                                    }
                                }

                                else -> shareAttachment(attachment)
                            }
                        }
                    },
                    enabled = mediaGalleryPreviewViewModel.connectionState is ConnectionState.Connected,
                ) {
                    val shareIcon = if (!mediaGalleryPreviewViewModel.isSharingInProgress) {
                        R.drawable.stream_compose_ic_share
                    } else {
                        R.drawable.stream_compose_ic_clear
                    }

                    Icon(
                        painter = painterResource(id = shareIcon),
                        contentDescription = stringResource(id = R.string.stream_compose_image_preview_share),
                        tint = if (mediaGalleryPreviewViewModel.connectionState is ConnectionState.Connected) {
                            ChatTheme.colors.textHighEmphasis
                        } else {
                            ChatTheme.colors.disabled
                        },
                    )
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (mediaGalleryPreviewViewModel.isSharingInProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(horizontal = 12.dp)
                                .size(24.dp),
                            strokeWidth = 2.dp,
                            color = ChatTheme.colors.primaryAccent,
                        )
                    }

                    val text = if (!mediaGalleryPreviewViewModel.isSharingInProgress) {
                        stringResource(
                            id = R.string.stream_compose_image_order,
                            pagerState.currentPage + 1,
                            attachmentCount,
                        )
                    } else {
                        stringResource(id = R.string.stream_compose_media_gallery_preview_preparing)
                    }

                    Text(
                        text = text,
                        style = ChatTheme.typography.title3Bold,
                        color = ChatTheme.colors.textHighEmphasis,
                    )
                }

                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleGallery(
                                isShowingGallery = true,
                            )
                        }
                    },
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_gallery),
                        contentDescription = stringResource(id = R.string.stream_compose_image_preview_photos),
                        tint = ChatTheme.colors.textHighEmphasis,
                    )
                }
            }
        }
    }

    /**
     * Builds the media options based on the given [message]. These options let the user interact more
     * with the media they're observing.
     *
     * @param message The message that holds all the media.
     * @return [List] of options the user can choose from, in the form of [MediaGalleryPreviewOption].
     */
    @Composable
    private fun defaultMediaOptions(message: Message): List<MediaGalleryPreviewOption> {
        val user by mediaGalleryPreviewViewModel.user.collectAsState()

        val isChatConnected by remember(mediaGalleryPreviewViewModel.connectionState) {
            derivedStateOf {
                mediaGalleryPreviewViewModel.connectionState is ConnectionState.Connected
            }
        }

        val saveMediaColor =
            if (isChatConnected) {
                ChatTheme.colors.textHighEmphasis
            } else {
                ChatTheme.colors.disabled
            }

        val options = mutableListOf(
            MediaGalleryPreviewOption(
                title = stringResource(id = R.string.stream_compose_media_gallery_preview_reply),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_reply),
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = Reply(message),
                isEnabled = true,
            ),
            MediaGalleryPreviewOption(
                title = stringResource(id = R.string.stream_compose_media_gallery_preview_show_in_chat),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_show_in_chat),
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = ShowInChat(message),
                isEnabled = true,
            ),
            MediaGalleryPreviewOption(
                title = stringResource(id = R.string.stream_compose_media_gallery_preview_save_image),
                titleColor = saveMediaColor,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_download),
                iconColor = saveMediaColor,
                action = SaveMedia(message),
                isEnabled = isChatConnected,
            ),
        )

        if (message.user.id == user?.id) {
            val deleteColor =
                if (mediaGalleryPreviewViewModel.connectionState is ConnectionState.Connected) {
                    ChatTheme.colors.errorAccent
                } else {
                    ChatTheme.colors.disabled
                }

            options.add(
                MediaGalleryPreviewOption(
                    title = stringResource(id = R.string.stream_compose_media_gallery_preview_delete),
                    titleColor = deleteColor,
                    iconPainter = painterResource(id = R.drawable.stream_compose_ic_delete),
                    iconColor = deleteColor,
                    action = Delete(message),
                    isEnabled = isChatConnected,
                ),
            )
        }

        return options
    }

    /**
     * Handles the logic of sharing a file.
     *
     * @param attachment The attachment to be shared.
     */
    private fun shareAttachment(attachment: Attachment) {
        fileSharingJob = lifecycleScope.launch {
            mediaGalleryPreviewViewModel.isSharingInProgress = true

            when (attachment.type) {
                AttachmentType.IMAGE -> shareImage(attachment)
                AttachmentType.VIDEO -> shareVideo(attachment)
                else -> toastFailedShare()
            }
        }
    }

    /**
     * Fetches an image from Coil's cache and shares it.
     *
     * @param attachment The attachment used to prepare the URI.
     */
    private suspend fun shareImage(attachment: Attachment) {
        val attachmentUrl = attachment.imagePreviewUrl

        if (attachmentUrl != null) {
            StreamImageLoader.instance().loadAsBitmap(
                context = applicationContext,
                url = attachmentUrl,
            )?.let {
                val imageUri = StreamFileUtil.writeImageToSharableFile(applicationContext, it)

                shareAttachment(
                    mediaUri = imageUri,
                    attachmentType = attachment.type,
                )
            }
        } else {
            mediaGalleryPreviewViewModel.isSharingInProgress = false
            toastFailedShare()
        }
    }

    /**
     * Displays a toast saying that sharing the attachment has failed.
     */
    private fun toastFailedShare() {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(R.string.stream_compose_media_gallery_preview_could_not_share_attachment),
            Toast.LENGTH_SHORT,
        ).show()
    }

    /**
     * Starts a picker to share the current image.
     *
     * @param mediaUri The URI of the media attachment to share.
     * @param attachmentType type of attachment being shared.
     */
    private fun shareAttachment(
        mediaUri: Uri?,
        attachmentType: String?,
    ) {
        mediaGalleryPreviewViewModel.isSharingInProgress = false

        if (mediaUri == null) {
            toastFailedShare()
            return
        }

        val mediaType = when (attachmentType) {
            AttachmentType.IMAGE -> "image/*"
            AttachmentType.VIDEO -> "video/*"
            else -> {
                toastFailedShare()
                return
            }
        }

        ContextCompat.startActivity(
            this,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = mediaType
                    putExtra(Intent.EXTRA_STREAM, mediaUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                getString(R.string.stream_compose_attachment_gallery_share),
            ),
            null,
        )
    }

    /**
     * Starts a picker to share the current image.
     *
     * @param attachment The attachment to share.
     */
    private suspend fun shareVideo(attachment: Attachment) {
        val result = withContext(DispatcherProvider.IO) {
            StreamFileUtil.writeFileToShareableFile(
                context = applicationContext,
                attachment = attachment,
            )
        }

        mediaGalleryPreviewViewModel.isSharingInProgress = false

        when (result) {
            is Result.Success -> shareAttachment(
                mediaUri = result.value,
                attachmentType = attachment.type,
            )

            is Result.Failure -> toastFailedShare()
        }
    }

    /**
     * Represents the image gallery where the user can browse all media attachments and quickly jump to them.
     *
     * @param pagerState The state of the pager, used to navigate to specific media attachments.
     * @param attachments The list of attachments to be displayed.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun MediaGallery(
        pagerState: PagerState,
        attachments: List<Attachment>,
        modifier: Modifier = Modifier,
    ) {
        val message = mediaGalleryPreviewViewModel.message

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.overlay)
                .clickable(
                    indication = null,
                    interactionSource = null,
                    onClick = remember(mediaGalleryPreviewViewModel) {
                        {
                            mediaGalleryPreviewViewModel.toggleGallery(
                                isShowingGallery = false,
                            )
                        }
                    },
                ),
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
                    .clickable(
                        indication = null,
                        interactionSource = null,
                        onClick = {},
                    ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                shadowElevation = 4.dp,
                color = ChatTheme.colors.barsBackground,
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    MediaGalleryHeader()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(ColumnCount),
                        content = {
                            itemsIndexed(attachments) { index, attachment ->
                                MediaGalleryItem(index, attachment, message.user, pagerState)
                            }
                        },
                    )
                }
            }
        }
    }

    /**
     * Represents the header of [MediaGallery] that allows the user to dismiss the component.
     */
    @Composable
    private fun MediaGalleryHeader() {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
                    .clickable(
                        bounded = false,
                        onClick = remember(mediaGalleryPreviewViewModel) {
                            {
                                mediaGalleryPreviewViewModel.toggleGallery(
                                    isShowingGallery = false,
                                )
                            }
                        },
                    ),
                painter = painterResource(id = R.drawable.stream_compose_ic_close),
                contentDescription = stringResource(id = R.string.stream_compose_cancel),
                tint = ChatTheme.colors.textHighEmphasis,
            )

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.stream_compose_image_preview_photos),
                style = ChatTheme.typography.title3Bold,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
    }

    /**
     * Represents each item in the [MediaGallery].
     *
     * @param index The index of the item.
     * @param attachment The attachment data used to load the item media attachment.
     * @param user The user who sent the media attachment.
     * @param pagerState The state of the pager, used to navigate to items when the user selects them.
     */
    @Composable
    private fun MediaGalleryItem(
        index: Int,
        attachment: Attachment,
        user: User,
        pagerState: PagerState,
    ) {
        val isImage = attachment.isImage()
        val isVideo = attachment.isVideo()
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            mediaGalleryPreviewViewModel.toggleGallery(isShowingGallery = false)
                            pagerState.animateScrollToPage(index)
                        }
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            val data =
                if (isImage || (isVideo && ChatTheme.videoThumbnailsEnabled)) {
                    attachment.imagePreviewUrl?.applyStreamCdnImageResizingIfEnabled(ChatTheme.streamCdnImageResizing)
                } else {
                    null
                }

            val context = LocalContext.current
            val imageRequest = remember {
                ImageRequest.Builder(context)
                    .data(data)
                    .build()
            }

            var imageState by remember { mutableStateOf<AsyncImagePainter.State>(AsyncImagePainter.State.Empty) }

            val backgroundColor = if (isImage) {
                ChatTheme.colors.imageBackgroundMediaGalleryPicker
            } else {
                ChatTheme.colors.videoBackgroundMediaGalleryPicker
            }

            StreamAsyncImage(
                imageRequest = imageRequest,
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
                    .background(color = backgroundColor),
                contentScale = ContentScale.Crop,
            ) { asyncImageState ->
                imageState = asyncImageState

                when (asyncImageState) {
                    is AsyncImagePainter.State.Empty,
                    is AsyncImagePainter.State.Loading,
                    -> ShimmerProgressIndicator(
                        modifier = Modifier.fillMaxSize(),
                    )

                    is AsyncImagePainter.State.Success,
                    -> Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = asyncImageState.painter,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                    )

                    is AsyncImagePainter.State.Error,
                    -> ErrorIcon(Modifier.fillMaxSize())
                }
            }

            Avatar(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(24.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = ChatTheme.shapes.avatar,
                    )
                    .shadow(
                        elevation = 4.dp,
                        shape = ChatTheme.shapes.avatar,
                    ),
                imageUrl = user.image,
                initials = user.initials,
            )

            if (isVideo && imageState.isCompleted) {
                PlayButton(
                    modifier = Modifier
                        .shadow(6.dp, shape = CircleShape)
                        .background(color = Color.White, shape = CircleShape)
                        .fillMaxSize(0.2f),
                    contentDescription = getString(R.string.stream_compose_cd_play_button),
                )
            }
        }
    }

    @Composable
    private fun ErrorIcon(modifier: Modifier) {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                tint = ChatTheme.colors.disabled,
                modifier = Modifier.fillMaxSize(0.4f),
                painter = painterResource(R.drawable.stream_compose_ic_image_picker),
                contentDescription = stringResource(R.string.stream_ui_message_list_attachment_load_failed),
            )
        }
    }

    /**
     * Fetches individual image resizing options from the bundle and
     * packs them into [StreamCdnImageResizing].
     *
     * @return An instance of [StreamCdnImageResizing] created from individual options fetched from
     * the bundle packed inside the given intent.
     */
    private fun Intent.createStreamCdnImageResizing(): StreamCdnImageResizing {
        val imageResizingEnabled = getBooleanExtra(KeyImageResizingEnabled, false)

        val resizedWidthPercentage = getFloatExtra(KeyStreamCdnResizeImagedWidthPercentage, 1f)
        val resizedHeightPercentage = getFloatExtra(KeyStreamCdnResizeImagedHeightPercentage, 1f)

        val resizeModeEnumValue = getStringExtra(KeyStreamCdnResizeImageMode)
        val resizeMode =
            if (resizeModeEnumValue != null) StreamCdnResizeImageMode.valueOf(value = resizeModeEnumValue) else null

        val cropModeEnumValue = getStringExtra(KeyStreamCdnResizeImageCropMode)
        val cropMode =
            if (cropModeEnumValue != null) StreamCdnCropImageMode.valueOf(value = cropModeEnumValue) else null

        return StreamCdnImageResizing(
            imageResizingEnabled = imageResizingEnabled,
            resizedWidthPercentage = resizedWidthPercentage,
            resizedHeightPercentage = resizedHeightPercentage,
            cropMode = cropMode,
            resizeMode = resizeMode,
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        StreamFileUtil.clearStreamCache(context = applicationContext)
    }

    public companion object {

        /**
         * If the file is at least this big or bigger we prompt the user to make sure they
         * want to download it.
         *
         * Expressed in bytes.
         */
        private const val MaxUnpromptedFileSize = 10 * 1024 * 1024

        /**
         * The column count used for the image gallery.
         */
        private const val ColumnCount = 3

        /**
         * Represents the key for the ID of the message with the attachments we're browsing.
         */
        private const val KeyMediaGalleryPreviewActivityState: String = "mediaGalleryPreviewActivityState"

        /**
         * Represents the key for the [Boolean] value dictating whether video thumbnails
         * will be displayed in previews or not.
         */
        private const val KeyVideoThumbnailsEnabled: String = "videoThumbnailsEnabled"

        /**
         * Represents the key for the [Boolean] value found inside [StreamCdnImageResizing]
         * turning image resizing on or off.
         */
        private const val KeyImageResizingEnabled: String = "imageResizingEnabled"

        /**
         * Represents the key for the [Float] value found inside [StreamCdnImageResizing]
         * dictating the resized image width percentage.
         */
        private const val KeyStreamCdnResizeImagedWidthPercentage: String = "streamCdnResizeImagedWidthPercentage"

        /**
         * Represents the key for the [Float] value found inside [StreamCdnImageResizing]
         * dictating the resized image height percentage.
         */
        private const val KeyStreamCdnResizeImagedHeightPercentage: String = "streamCdnResizeImagedHeightPercentage"

        /**
         * Represents the key for the [StreamCdnResizeImageMode] value found inside [StreamCdnImageResizing]
         * dictating the resize image mode.
         */
        private const val KeyStreamCdnResizeImageMode: String = "streamCdnResizeImageMode"

        /**
         * Represents the key for the [StreamCdnCropImageMode] value found inside [StreamCdnImageResizing]
         * dictating the crop image mode.
         */
        private const val KeyStreamCdnResizeImageCropMode: String = "streamCdnResizeImageCropMode"

        /**
         * Represents the key for the starting attachment position based on the clicked attachment.
         */
        private const val KeyAttachmentPosition: String = "attachmentPosition"

        /**
         * Represents the key for the result of the preview, like scrolling to the message.
         */
        public const val KeyMediaGalleryPreviewResult: String = "mediaGalleryPreviewResult"

        /**
         * Represents the key for the boolean which dictates if we should skip enriching URLs when updating a
         * message.
         */
        private const val KeySkipEnrichUrl: String = "skipEnrichUrl"

        /**
         * Time period inside which two taps are registered as double tap.
         */
        private const val DoubleTapTimeoutMs: Long = 500L

        /**
         * Maximum scale that can be applied to the image.
         */
        private const val MaxZoomScale: Float = 3f

        /**
         * Middle scale value that can be applied to image.
         */
        private const val MidZoomScale: Float = 2f

        /**
         * Default (min) value that can be applied to image.
         */
        private const val DefaultZoomScale: Float = 1f

        /**
         * Used to generate download URIs for attachments.
         */
        private var downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator =
            DefaultDownloadAttachmentUriGenerator

        /**
         * Used to intercept download requests.
         */
        private var downloadRequestInterceptor: DownloadRequestInterceptor = DownloadRequestInterceptor { }

        /**
         * Used to build an [Intent] to start the [MediaGalleryPreviewActivity] with the required data.
         *
         * @param context The context to start the activity with.
         * @param message The [Message] containing the attachments.
         * @param attachmentPosition The initial position of the clicked media attachment.
         * @param videoThumbnailsEnabled Whether video thumbnails will be displayed in previews or not.
         * @param downloadAttachmentUriGenerator Used to generate download URIs for attachments.
         * @param downloadRequestInterceptor Used to intercept download requests.
         * @param streamCdnImageResizing Sets the Stream CDN hosted image resizing strategy. Turned off by default.
         * Please note that only Stream CDN hosted images containing original width (ow) and original height (oh)
         * parameters are able to be resized.
         * @param skipEnrichUrl If set to true will skip enriching URLs when you update the message
         * by deleting an attachment contained within it. Set to false by default.
         */
        @Suppress("LongParameterList")
        public fun getIntent(
            context: Context,
            message: Message,
            attachmentPosition: Int,
            videoThumbnailsEnabled: Boolean,
            downloadAttachmentUriGenerator: DownloadAttachmentUriGenerator,
            downloadRequestInterceptor: DownloadRequestInterceptor,
            streamCdnImageResizing: StreamCdnImageResizing = StreamCdnImageResizing.defaultStreamCdnImageResizing(),
            skipEnrichUrl: Boolean = false,
        ): Intent {
            this.downloadAttachmentUriGenerator = downloadAttachmentUriGenerator
            this.downloadRequestInterceptor = downloadRequestInterceptor
            return Intent(context, MediaGalleryPreviewActivity::class.java).apply {
                val mediaGalleryPreviewActivityState = message.toMediaGalleryPreviewActivityState()

                putExtra(KeyMediaGalleryPreviewActivityState, mediaGalleryPreviewActivityState)
                putExtra(KeyAttachmentPosition, attachmentPosition)
                putExtra(KeyVideoThumbnailsEnabled, videoThumbnailsEnabled)

                // Image resizing options
                putExtra(KeyImageResizingEnabled, streamCdnImageResizing.imageResizingEnabled)
                putExtra(KeyStreamCdnResizeImagedWidthPercentage, streamCdnImageResizing.resizedWidthPercentage)
                putExtra(KeyStreamCdnResizeImagedHeightPercentage, streamCdnImageResizing.resizedHeightPercentage)
                putExtra(KeyStreamCdnResizeImageMode, streamCdnImageResizing.resizeMode?.name)
                putExtra(KeyStreamCdnResizeImageCropMode, streamCdnImageResizing.cropMode?.name)
                putExtra(KeySkipEnrichUrl, skipEnrichUrl)
            }
        }
    }
}
