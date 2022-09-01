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
import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.getstream.sdk.chat.StreamFileUtil
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.ConnectionState
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.DownloadPermissionHandler
import io.getstream.chat.android.compose.handlers.PermissionHandler
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
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberStreamImagePainter
import io.getstream.chat.android.compose.viewmodel.mediapreview.MediaGalleryPreviewViewModel
import io.getstream.chat.android.compose.viewmodel.mediapreview.MediaGalleryPreviewViewModelFactory
import io.getstream.chat.android.uiutils.constant.AttachmentType
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

/**
 * Shows an image and video previews along with enabling
 * the user to perform various actions such as image or file deletion.
 */
@OptIn(ExperimentalPagerApi::class)
public class MediaGalleryPreviewActivity : AppCompatActivity() {

    /**
     * Factory used to build the screen ViewModel given the received message ID.
     */
    private val factory by lazy {
        MediaGalleryPreviewViewModelFactory(
            chatClient = ChatClient.instance(),
            messageId = intent?.getParcelableExtra<MediaGalleryPreviewActivityState>(
                KeyMediaGalleryPreviewActivityState
            )?.messageId ?: ""
        )
    }

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
        val mediaGalleryPreviewActivityState = intent?.getParcelableExtra<MediaGalleryPreviewActivityState>(
            KeyMediaGalleryPreviewActivityState
        )
        val messageId = mediaGalleryPreviewActivityState?.messageId ?: ""

        if (!mediaGalleryPreviewViewModel.hasCompleteMessage) {
            val message = mediaGalleryPreviewActivityState?.toMessage()

            if (message != null)
                mediaGalleryPreviewViewModel.message = message
        }

        val attachmentPosition = intent?.getIntExtra(KeyAttachmentPosition, 0) ?: 0

        if (messageId.isBlank()) {
            throw IllegalArgumentException("Missing messageId necessary to load images.")
        }

        setContent {
            ChatTheme {
                val message = mediaGalleryPreviewViewModel.message

                if (message.deletedAt != null) {
                    finish()
                    return@ChatTheme
                }

                MediaGalleryPreviewContentWrapper(message, attachmentPosition)
            }
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
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun MediaGalleryPreviewContentWrapper(
        message: Message,
        initialAttachmentPosition: Int,
    ) {
        val startingPosition =
            if (initialAttachmentPosition !in message.attachments.indices) 0 else initialAttachmentPosition

        val pagerState = rememberPagerState(initialPage = startingPosition)

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { MediaGalleryPreviewTopBar(message) },
                content = { contentPadding ->
                    if (message.id.isNotEmpty()) {

                        Surface(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(contentPadding)
                        ) {
                            MediaPreviewContent(pagerState, message.attachments)
                        }
                    }
                },
                bottomBar = {
                    if (message.id.isNotEmpty()) {
                        MediaGalleryPreviewBottomBar(message.attachments, pagerState)
                    }
                }
            )

            AnimatedVisibility(
                visible = mediaGalleryPreviewViewModel.isShowingOptions,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                MediaGalleryPreviewOptions(
                    options = defaultMediaOptions(message = message),
                    pagerState = pagerState,
                    modifier = Modifier.animateEnterExit(
                        enter = slideInVertically(),
                        exit = slideOutVertically()
                    )
                )
            }

            if (message.id.isNotEmpty()) {
                AnimatedVisibility(
                    visible = mediaGalleryPreviewViewModel.isShowingGallery,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    MediaGallery(
                        pagerState = pagerState,
                        modifier = Modifier.animateEnterExit(
                            enter = slideInVertically(initialOffsetY = { height -> height / 2 }),
                            exit = slideOutVertically(targetOffsetY = { height -> height / 2 })
                        )
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
            elevation = 4.dp,
            color = ChatTheme.colors.barsBackground
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
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
                    message = message
                )

                MediaGalleryPreviewOptionsToggle(
                    modifier = Modifier.weight(1f),
                    message = message
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
                ConnectionState.CONNECTED -> Text(
                    text = message.user.name,
                    style = textStyle,
                    color = textColor
                )
                ConnectionState.CONNECTING -> NetworkLoadingIndicator(
                    textStyle = textStyle,
                    textColor = textColor
                )
                ConnectionState.OFFLINE -> Text(
                    text = getString(R.string.stream_compose_disconnected),
                    style = textStyle,
                    color = textColor
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
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = { mediaGalleryPreviewViewModel.toggleMediaOptions(isShowingOptions = true) },
                    enabled = message.id.isNotEmpty()
                ),
            painter = painterResource(id = R.drawable.stream_compose_ic_menu_vertical),
            contentDescription = stringResource(R.string.stream_compose_image_options),
            tint = if (message.id.isNotEmpty()) ChatTheme.colors.textHighEmphasis else ChatTheme.colors.disabled
        )
    }

    /**
     * The media attachment options menu, used to perform different actions for the currently active media
     * attachment.
     *
     * @param options The options available for the attachment.
     * @param pagerState The state of the pager, used to fetch the current attachment.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun MediaGalleryPreviewOptions(
        options: List<MediaGalleryPreviewOption>,
        pagerState: PagerState,
        modifier: Modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.overlay)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { mediaGalleryPreviewViewModel.toggleMediaOptions(isShowingOptions = false) }
                )
        ) {
            Surface(
                modifier = modifier
                    .padding(16.dp)
                    .width(150.dp)
                    .wrapContentHeight()
                    .align(Alignment.TopEnd),
                shape = RoundedCornerShape(16.dp),
                elevation = 4.dp,
                color = ChatTheme.colors.barsBackground
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, option ->
                        MediaGalleryPreviewOptionItem(option, pagerState)

                        if (index != options.lastIndex) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(0.5.dp)
                                    .background(ChatTheme.colors.borders)
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
     */
    @Composable
    private fun MediaGalleryPreviewOptionItem(
        mediaGalleryPreviewOption: MediaGalleryPreviewOption,
        pagerState: PagerState,
    ) {
        val downloadPermissionHandler = ChatTheme.permissionHandlerProvider
            .first { it.canHandle(Manifest.permission.WRITE_EXTERNAL_STORAGE) }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.barsBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    onClick = {
                        mediaGalleryPreviewViewModel.toggleMediaOptions(isShowingOptions = false)
                        handleMediaAction(
                            mediaGalleryPreviewOption.action,
                            pagerState.currentPage,
                            downloadPermissionHandler
                        )
                    }
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                modifier = Modifier
                    .size(18.dp),
                painter = mediaGalleryPreviewOption.iconPainter,
                tint = mediaGalleryPreviewOption.iconColor,
                contentDescription = mediaGalleryPreviewOption.title
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = mediaGalleryPreviewOption.title,
                color = mediaGalleryPreviewOption.titleColor,
                style = ChatTheme.typography.bodyBold,
                fontSize = 12.sp
            )
        }
    }

    /**
     * Consumes the action user selected to perform for the current media attachment.
     *
     * @param mediaGalleryPreviewAction The action the user selected.
     * @param currentPage The index of the current media attachment.
     * @param permissionHandler Checks if we have the necessary permissions
     * to perform an action if the action needs a specific Android permission.
     */
    private fun handleMediaAction(
        mediaGalleryPreviewAction: MediaGalleryPreviewAction,
        currentPage: Int,
        permissionHandler: PermissionHandler,
    ) {
        val message = mediaGalleryPreviewAction.message

        when (mediaGalleryPreviewAction) {
            is ShowInChat -> {
                handleResult(
                    MediaGalleryPreviewResult(
                        messageId = message.id,
                        resultType = MediaGalleryPreviewResultType.SHOW_IN_CHAT
                    )
                )
            }
            is Reply -> {
                handleResult(
                    MediaGalleryPreviewResult(
                        messageId = message.id,
                        resultType = MediaGalleryPreviewResultType.QUOTE
                    )
                )
            }
            is Delete -> mediaGalleryPreviewViewModel.deleteCurrentMediaAttachment(message.attachments[currentPage])
            is SaveMedia -> {
                permissionHandler
                    .onHandleRequest(
                        mapOf(DownloadPermissionHandler.PayloadAttachment to message.attachments[currentPage])
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
    ) {
        if (attachments.isEmpty()) {
            finish()
            return
        }

        HorizontalPager(
            modifier = Modifier.background(ChatTheme.colors.appBackground),
            state = pagerState,
            count = attachments.size,
        ) { page ->
            if (attachments[page].type == AttachmentType.IMAGE) {
                ImagePreviewContent(attachment = attachments[page], pagerState = pagerState, page = page)
            } else if (attachments[page].type == AttachmentType.VIDEO) {
                VideoPreviewContent(
                    attachment = attachments[page],
                    pagerState = pagerState,
                    page = page,
                    onPlaybackError = {
                        // TODO add error
                    }
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
            contentAlignment = Alignment.Center
        ) {

            // Used as a workaround for Coil's lack of a retry policy.
            // See: https://github.com/coil-kt/coil/issues/884#issuecomment-975932886
            var retryHash by remember {
                mutableStateOf(0)
            }

            val painter =
                rememberStreamImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(attachment.imagePreviewUrl)
                        .crossfade(true)
                        .setParameter(key = "retry_hash", value = retryHash)
                        .build()
                )

            val density = LocalDensity.current
            val parentSize = Size(density.run { maxWidth.toPx() }, density.run { maxHeight.toPx() })
            var imageSize by remember { mutableStateOf(Size(0f, 0f)) }

            var currentScale by remember { mutableStateOf(DefaultZoomScale) }
            var translation by remember { mutableStateOf(Offset(0f, 0f)) }

            val scale by animateFloatAsState(targetValue = currentScale)

            // Used to refresh the request for the current page
            // if it has previously failed.
            if (page == pagerState.currentPage &&
                mediaGalleryPreviewViewModel.connectionState == ConnectionState.CONNECTED &&
                painter.state is AsyncImagePainter.State.Error
            ) {
                retryHash++
            }

            val transformModifier = if (painter.state is AsyncImagePainter.State.Success) {
                val size = painter.intrinsicSize
                Modifier
                    .aspectRatio(size.width / size.height, true)
                    .background(color = ChatTheme.colors.overlay)
            } else {
                Modifier
            }

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                PlaceHolder(
                    asyncImagePainterState = painter.state,
                    isImage = attachment.type == AttachmentType.IMAGE,
                    progressIndicatorStrokeWidth = 6.dp,
                    progressIndicatorFillMaxSizePercentage = 0.2f
                )

                Image(
                    modifier = transformModifier
                        .graphicsLayer(
                            scaleY = scale,
                            scaleX = scale,
                            translationX = translation.x,
                            translationY = translation.y
                        )
                        .onGloballyPositioned {
                            imageSize = Size(it.size.width.toFloat(), it.size.height.toFloat())
                        }
                        .pointerInput(Unit) {
                            forEachGesture {
                                awaitPointerEventScope {
                                    awaitFirstDown(requireUnconsumed = true)
                                    do {
                                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)

                                        val zoom = event.calculateZoom()
                                        currentScale = (zoom * currentScale).coerceAtMost(MaxZoomScale)

                                        val maxTranslation = calculateMaxOffset(
                                            imageSize = imageSize,
                                            scale = currentScale,
                                            parentSize = parentSize
                                        )

                                        val offset = event.calculatePan()
                                        val newTranslationX = translation.x + offset.x * currentScale
                                        val newTranslationY = translation.y + offset.y * currentScale

                                        translation = Offset(
                                            newTranslationX.coerceIn(-maxTranslation.x, maxTranslation.x),
                                            newTranslationY.coerceIn(-maxTranslation.y, maxTranslation.y)
                                        )

                                        if (abs(newTranslationX) < calculateMaxOffsetPerAxis(
                                                imageSize.width,
                                                currentScale,
                                                parentSize.width
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
                            forEachGesture {
                                awaitPointerEventScope {
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
                    painter = painter,
                    contentDescription = null
                )

                Log.d("isCurrentPage", "${page != pagerState.currentPage}")

                if (pagerState.currentPage != page) {
                    currentScale = DefaultZoomScale
                    translation = Offset(0f, 0f)
                }
            }
        }
    }

    /**
     * Displays an image icon if no image was loaded previously
     * or the request has failed, a circular progress indicator
     * if the image is loading or nothing if the image has successfully
     * loaded.
     *
     * @param asyncImagePainterState The painter state used to determine
     * which UI to show.
     * @param isImage If the attachment we are holding the place for is
     * a image or not.
     * @param progressIndicatorStrokeWidth The thickness of the progress indicator
     * used to indicate a loading thumbnail.
     * @param progressIndicatorFillMaxSizePercentage Dictates what percentage of
     * available parent size the progress indicator will fill.
     */
    @Composable
    private fun PlaceHolder(
        asyncImagePainterState: AsyncImagePainter.State,
        isImage: Boolean = false,
        progressIndicatorStrokeWidth: Dp,
        progressIndicatorFillMaxSizePercentage: Float,
    ) {
        val painter = painterResource(
            id = R.drawable.stream_compose_ic_image_picker
        )

        val imageModifier = Modifier.fillMaxSize(0.4f)

        when {
            asyncImagePainterState is AsyncImagePainter.State.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(horizontal = 2.dp)
                        .fillMaxSize(progressIndicatorFillMaxSizePercentage),
                    strokeWidth = progressIndicatorStrokeWidth,
                    color = ChatTheme.colors.primaryAccent
                )
            }
            asyncImagePainterState is AsyncImagePainter.State.Error && isImage -> Icon(
                tint = ChatTheme.colors.textLowEmphasis,
                modifier = imageModifier,
                painter = painter,
                contentDescription = null
            )
            asyncImagePainterState is AsyncImagePainter.State.Success -> {}
            asyncImagePainterState is AsyncImagePainter.State.Empty && isImage -> {
                Icon(
                    tint = ChatTheme.colors.textLowEmphasis,
                    modifier = imageModifier,
                    painter = painter,
                    contentDescription = null
                )
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
                    ViewGroup.LayoutParams.MATCH_PARENT
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
                    // otherwise the preview will be removed whenever the video finished downloading.
                    if (!hasPrepared && userHasClickedPlay) {
                        shouldShowProgressBar = false
                        shouldShowPreview = false
                        mediaController.show()
                    }
                    hasPrepared = true
                }

                mediaController.setAnchorView(frameLayout)

                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
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
                val painter = rememberStreamImagePainter(data = attachment.thumbUrl)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
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
                        painter = painter, contentDescription = null
                    )

                    if (shouldShowPlayButton) {
                        PlayButton(
                            modifier = Modifier
                                .shadow(10.dp, shape = CircleShape)
                                .background(color = Color.White, shape = CircleShape)
                                .size(
                                    width = 42.dp,
                                    height = 42.dp
                                )
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
    @Composable
    private fun MediaGalleryPreviewBottomBar(attachments: List<Attachment>, pagerState: PagerState) {
        val attachmentCount = attachments.size

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            elevation = 4.dp,
            color = ChatTheme.colors.barsBackground
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = { onShareMediaClick(attachments[pagerState.currentPage]) },
                    enabled = mediaGalleryPreviewViewModel.connectionState == ConnectionState.CONNECTED
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_share),
                        contentDescription = stringResource(id = R.string.stream_compose_image_preview_share),
                        tint = if (mediaGalleryPreviewViewModel.connectionState == ConnectionState.CONNECTED) {
                            ChatTheme.colors.textHighEmphasis
                        } else {
                            ChatTheme.colors.disabled
                        },
                    )
                }

                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(
                        id = R.string.stream_compose_image_order,
                        pagerState.currentPage + 1,
                        attachmentCount
                    ),
                    style = ChatTheme.typography.title3Bold,
                    color = ChatTheme.colors.textHighEmphasis
                )

                IconButton(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    onClick = { mediaGalleryPreviewViewModel.toggleGallery(isShowingGallery = true) }
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
        val saveMediaColor =
            if (mediaGalleryPreviewViewModel.connectionState == ConnectionState.CONNECTED) {
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
                action = Reply(message)
            ),
            MediaGalleryPreviewOption(
                title = stringResource(id = R.string.stream_compose_media_gallery_preview_show_in_chat),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_show_in_chat),
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = ShowInChat(message)
            ),
            MediaGalleryPreviewOption(
                title = stringResource(id = R.string.stream_compose_media_gallery_preview_save_image),
                titleColor = saveMediaColor,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_download),
                iconColor = saveMediaColor,
                action = SaveMedia(message),
            )
        )

        if (message.user.id == user?.id) {
            val deleteColor =
                if (mediaGalleryPreviewViewModel.connectionState == ConnectionState.CONNECTED) {
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
                )
            )
        }

        return options
    }

    /**
     * Handles the logic of loading the image and preparing a shareable file.
     *
     * @param attachment The attachment to preload and share.
     */
    private fun onShareMediaClick(attachment: Attachment) {
        // TODO share videos as well
        lifecycleScope.launch {
            val uri = StreamImageLoader.instance().loadAsBitmap(
                context = applicationContext,
                url = attachment.imagePreviewUrl!!
            )?.let {
                StreamFileUtil.writeImageToSharableFile(applicationContext, it)
            }

            if (uri != null) {
                shareImage(uri)
            }
        }
    }

    /**
     * Starts a picker to share the current image.
     *
     * @param imageUri The URI of the image to share.
     */
    private fun shareImage(imageUri: Uri) {
        ContextCompat.startActivity(
            this,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                getString(R.string.stream_compose_attachment_gallery_share),
            ),
            null
        )
    }

    /**
     * Starts a picker to share the current image.
     *
     * @param imageUri The URI of the image to share.
     */
    private fun shareVideo(videoUri: Uri) {
        // TODO
    }

    /**
     * Represents the image gallery where the user can browse all media attachments and quickly jump to them.
     *
     * @param pagerState The state of the pager, used to navigate to specific media attachments.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun MediaGallery(
        pagerState: PagerState,
        modifier: Modifier = Modifier,
    ) {
        val message = mediaGalleryPreviewViewModel.message

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.overlay)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { mediaGalleryPreviewViewModel.toggleGallery(isShowingGallery = false) }
                )
        ) {
            Surface(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = 4.dp,
                color = ChatTheme.colors.barsBackground
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    MediaGalleryHeader()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(ColumnCount),
                        content = {
                            itemsIndexed(message.attachments) { index, attachment ->
                                MediaGalleryItem(index, attachment, message.user, pagerState)
                            }
                        }
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
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { mediaGalleryPreviewViewModel.toggleGallery(isShowingGallery = false) }
                    ),
                painter = painterResource(id = R.drawable.stream_compose_ic_close),
                contentDescription = stringResource(id = R.string.stream_compose_cancel),
                tint = ChatTheme.colors.textHighEmphasis
            )

            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(R.string.stream_compose_image_preview_photos),
                style = ChatTheme.typography.title3Bold,
                color = ChatTheme.colors.textHighEmphasis
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
        // Used as a workaround for Coil's lack of a retry policy.
        // See: https://github.com/coil-kt/coil/issues/884#issuecomment-975932886
        var retryHash by remember {
            mutableStateOf(0)
        }

        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable {
                    coroutineScope.launch {
                        mediaGalleryPreviewViewModel.toggleGallery(isShowingGallery = false)
                        pagerState.animateScrollToPage(index)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            val painter = rememberStreamImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(attachment.imagePreviewUrl)
                    .setHeader("rety_hash", retryHash.toString())
                    .build()
            )

            if (mediaGalleryPreviewViewModel.connectionState == ConnectionState.CONNECTED &&
                painter.state is AsyncImagePainter.State.Error
            ) {
                retryHash++
            }

            Image(
                modifier = Modifier
                    .padding(1.dp)
                    .fillMaxSize()
                    .background(color = ChatTheme.colors.imageBackgroundMediaGalleryPicker),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            PlaceHolder(
                asyncImagePainterState = painter.state,
                isImage = attachment.type == AttachmentType.IMAGE,
                progressIndicatorStrokeWidth = 3.dp,
                progressIndicatorFillMaxSizePercentage = 0.3f
            )

            Avatar(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(24.dp)
                    .border(
                        width = 1.dp,
                        color = Color.White,
                        shape = ChatTheme.shapes.avatar
                    )
                    .shadow(
                        elevation = 5.dp,
                        shape = ChatTheme.shapes.avatar
                    ),
                imageUrl = user.image,
                initials = user.initials
            )

            if (attachment.type == AttachmentType.VIDEO) {
                PlayButton(
                    modifier = Modifier
                        .shadow(10.dp, shape = CircleShape)
                        .background(color = Color.White, shape = CircleShape)
                        .fillMaxSize(0.2f)
                )
            }
        }
    }

    public companion object {
        /**
         * The column count used for the image gallery.
         */
        private const val ColumnCount = 3

        /**
         * Represents the key for the ID of the message with the attachments we're browsing.
         */
        private const val KeyMediaGalleryPreviewActivityState: String = "mediaGalleryPreviewActivityState"

        /**
         * Represents the key for the starting attachment position based on the clicked attachment.
         */
        private const val KeyAttachmentPosition: String = "attachmentPosition"

        /**
         * Represents the key for the result of the preview, like scrolling to the message.
         */
        public const val KeyMediaGalleryPreviewResult: String = "mediaGalleryPreviewResult"

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
         * Used to build an [Intent] to start the [MediaGalleryPreviewActivity] with the required data.
         *
         * @param context The context to start the activity with.
         * @param message The [Message] containing the attachments.
         * @param attachmentPosition The initial position of the clicked media attachment.
         */
        public fun getIntent(context: Context, message: Message, attachmentPosition: Int): Intent {
            return Intent(context, MediaGalleryPreviewActivity::class.java).apply {
                val mediaGalleryPreviewActivityState = message.toMediaGalleryPreviewActivityState()

                putExtra(KeyMediaGalleryPreviewActivityState, mediaGalleryPreviewActivityState)
                putExtra(KeyAttachmentPosition, attachmentPosition)
            }
        }
    }
}
