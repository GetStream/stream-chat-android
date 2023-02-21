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

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImagePainter
import com.getstream.sdk.chat.StreamFileUtil
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.Delete
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewAction
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewOption
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType
import io.getstream.chat.android.compose.state.imagepreview.Reply
import io.getstream.chat.android.compose.state.imagepreview.SaveImage
import io.getstream.chat.android.compose.state.imagepreview.ShowInChat
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberStreamImagePainter
import io.getstream.chat.android.compose.util.attachmentDownloadState
import io.getstream.chat.android.compose.util.onDownloadHandleRequest
import io.getstream.chat.android.compose.viewmodel.imagepreview.ImagePreviewViewModel
import io.getstream.chat.android.compose.viewmodel.imagepreview.ImagePreviewViewModelFactory
import io.getstream.chat.android.uiutils.extension.hasLink
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.abs

/**
 * Shows an image preview, where we can page through image items, zoom in and perform various actions.
 */
@OptIn(ExperimentalPagerApi::class)
public class ImagePreviewActivity : AppCompatActivity() {

    /**
     * Factory used to build the screen ViewModel given the received message ID.
     */
    private val factory by lazy {
        ImagePreviewViewModelFactory(
            chatClient = ChatClient.instance(),
            messageId = intent?.getStringExtra(KeyMessageId) ?: ""
        )
    }

    /**
     * The ViewModel that exposes screen data.
     */
    private val imagePreviewViewModel by viewModels<ImagePreviewViewModel>(factoryProducer = { factory })

    /**
     * Sets up the data required to show the preview of images within the given message.
     *
     * Immediately finishes in case the data is invalid.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val messageId = intent?.getStringExtra(KeyMessageId) ?: ""
        val attachmentPosition = intent?.getIntExtra(KeyAttachmentPosition, 0) ?: 0

        if (messageId.isBlank()) {
            throw IllegalArgumentException("Missing messageId to load images.")
        }

        setContent {
            ChatTheme {
                val message = imagePreviewViewModel.message

                if (message.deletedAt != null) {
                    finish()
                    return@ChatTheme
                }

                if (message.id.isNotEmpty()) {
                    ImagePreviewContentWrapper(message, attachmentPosition)
                }
            }
        }
    }

    /**
     * Wraps the content of the screen in a composable that represents the top and bottom bars and the
     * images preview.
     *
     * @param message The message to show the attachments from.
     * @param initialAttachmentPosition The initial pager position, based on the image the user clicked on.
     */
    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    private fun ImagePreviewContentWrapper(
        message: Message,
        initialAttachmentPosition: Int,
    ) {
        // Filters out any link attachments. Pass this value along to all children
        // Composables that read message attachments to prevent inconsistent state.
        val filteredAttachments: List<Attachment> = message.attachments.filter { attachment ->
            !attachment.hasLink()
        }

        val startingPosition =
            if (initialAttachmentPosition !in filteredAttachments.indices) 0 else initialAttachmentPosition

        val pagerState = rememberPagerState(initialPage = startingPosition)

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { ImagePreviewTopBar(message) },
                content = { contentPadding ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(contentPadding)
                    ) {
                        ImagePreviewContent(pagerState, filteredAttachments)
                    }
                },
                bottomBar = { ImagePreviewBottomBar(filteredAttachments, pagerState) }
            )

            AnimatedVisibility(
                visible = imagePreviewViewModel.isShowingOptions,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ImagePreviewOptions(
                    options = defaultImageOptions(message = message),
                    pagerState = pagerState,
                    attachments = filteredAttachments,
                    modifier = Modifier.animateEnterExit(
                        enter = slideInVertically(),
                        exit = slideOutVertically()
                    )
                )
            }

            AnimatedVisibility(
                visible = imagePreviewViewModel.isShowingGallery,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ImageGallery(
                    pagerState = pagerState,
                    attachments = filteredAttachments,
                    modifier = Modifier.animateEnterExit(
                        enter = slideInVertically(initialOffsetY = { height -> height / 2 }),
                        exit = slideOutVertically(targetOffsetY = { height -> height / 2 })
                    )
                )
            }
        }
    }

    /**
     * The top bar which allows the user to go back or browse more screen options.
     *
     * @param message The message used for info and actions.
     */
    @Composable
    private fun ImagePreviewTopBar(message: Message) {
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

                ImagePreviewHeaderTitle(
                    modifier = Modifier.weight(8f),
                    message = message
                )

                ImagePreviewOptionsToggle(modifier = Modifier.weight(1f))
            }
        }
    }

    /**
     * Represents the header title that shows more information about the images.
     *
     * @param message The message with the images we're observing.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun ImagePreviewHeaderTitle(
        message: Message,
        modifier: Modifier = Modifier,
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = message.user.name,
                style = ChatTheme.typography.title3Bold,
                color = ChatTheme.colors.textHighEmphasis
            )

            Timestamp(date = message.updatedAt ?: message.createdAt ?: Date())
        }
    }

    /**
     * Toggles the image options menu.
     *
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun ImagePreviewOptionsToggle(
        modifier: Modifier = Modifier,
    ) {
        Icon(
            modifier = modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(bounded = false),
                    onClick = { imagePreviewViewModel.toggleImageOptions(isShowingOptions = true) }
                ),
            painter = painterResource(id = R.drawable.stream_compose_ic_menu_vertical),
            contentDescription = stringResource(R.string.stream_compose_image_options),
            tint = ChatTheme.colors.textHighEmphasis
        )
    }

    /**
     * The image options menu, used to perform different actions for the currently active image.
     *
     * @param options The options available for the image.
     * @param pagerState The state of the pager, used to fetch the current image.
     * @param attachments The list of attachments for which we display options.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun ImagePreviewOptions(
        options: List<ImagePreviewOption>,
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
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { imagePreviewViewModel.toggleImageOptions(isShowingOptions = false) }
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
                        ImagePreviewOptionItem(
                            imagePreviewOption = option,
                            pagerState = pagerState,
                            attachments = attachments
                        )

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
     * Represents each item in the image options menu that the user can pick.
     *
     * @param imagePreviewOption The option information to show.
     * @param pagerState The state of the pager, used to handle selected actions.
     * @param attachments The list of attachments for which we display options.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun ImagePreviewOptionItem(
        imagePreviewOption: ImagePreviewOption,
        pagerState: PagerState,
        attachments: List<Attachment>,
    ) {
        val (writePermissionState, downloadPayload) = attachmentDownloadState()
        val context = LocalContext.current

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.barsBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    onClick = {
                        imagePreviewViewModel.toggleImageOptions(isShowingOptions = false)
                        handleImageAction(
                            context = context,
                            imagePreviewAction = imagePreviewOption.action,
                            currentPage = pagerState.currentPage,
                            writePermissionState = writePermissionState,
                            downloadPayload = downloadPayload,
                            attachments = attachments
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
                painter = imagePreviewOption.iconPainter,
                tint = imagePreviewOption.iconColor,
                contentDescription = imagePreviewOption.title
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = imagePreviewOption.title,
                color = imagePreviewOption.titleColor,
                style = ChatTheme.typography.bodyBold,
                fontSize = 12.sp
            )
        }
    }

    /**
     * Consumes the action user selected to perform for the current image.
     *
     * @param imagePreviewAction The action the user selected.
     * @param currentPage The index of the current image.
     * @param attachments The list of attachments for which actions need to be handled.
     */
    @OptIn(ExperimentalPermissionsApi::class)
    private fun handleImageAction(
        context: Context,
        imagePreviewAction: ImagePreviewAction,
        currentPage: Int,
        attachments: List<Attachment>,
        writePermissionState: PermissionState,
        downloadPayload: MutableState<Attachment?>
    ) {
        val message = imagePreviewAction.message

        when (imagePreviewAction) {
            is ShowInChat -> {
                handleResult(
                    ImagePreviewResult(
                        messageId = message.id,
                        resultType = ImagePreviewResultType.SHOW_IN_CHAT
                    )
                )
            }
            is Reply -> {
                handleResult(ImagePreviewResult(messageId = message.id, resultType = ImagePreviewResultType.QUOTE))
            }
            is Delete -> imagePreviewViewModel.deleteCurrentImage(attachments[currentPage])
            is SaveImage -> {
                onDownloadHandleRequest(
                    context = context,
                    payload = attachments[currentPage],
                    permissionState = writePermissionState,
                    downloadPayload = downloadPayload
                )
            }
        }
    }

    /**
     * Prepares and sets the result of this Activity and propagates it back to the user.
     *
     * @param result The chosen action result.
     */
    private fun handleResult(result: ImagePreviewResult) {
        val data = Intent().apply {
            putExtra(KeyImagePreviewResult, result)
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
    private fun ImagePreviewContent(
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
            count = attachments.size
        ) { page ->
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val attachment = attachments[page]
                val imageUrl = attachment.imageUrl ?: attachment.thumbUrl ?: ""

                val painter = rememberStreamImagePainter(data = imageUrl)

                val density = LocalDensity.current
                val parentSize = Size(density.run { maxWidth.toPx() }, density.run { maxHeight.toPx() })
                var imageSize by remember { mutableStateOf(Size(0f, 0f)) }

                var currentScale by remember { mutableStateOf(DefaultZoomScale) }
                var translation by remember { mutableStateOf(Offset(0f, 0f)) }

                val scale by animateFloatAsState(targetValue = currentScale)

                val transformModifier = if (painter.state is AsyncImagePainter.State.Success) {
                    val size = painter.intrinsicSize
                    Modifier.aspectRatio(size.width / size.height, true)
                } else {
                    Modifier
                }

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

                if (pagerState.currentPage != page) {
                    currentScale = DefaultZoomScale
                    translation = Offset(0f, 0f)
                }
            }
        }
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
     * Represents the bottom bar which holds more options and information about the current image.
     *
     * @param attachments The attachments to use for the UI state and options.
     * @param pagerState The state of the pager, used for current page information.
     */
    @Composable
    private fun ImagePreviewBottomBar(attachments: List<Attachment>, pagerState: PagerState) {
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
                    onClick = { onShareImageClick(attachments[pagerState.currentPage]) }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.stream_compose_ic_share),
                        contentDescription = stringResource(id = R.string.stream_compose_image_preview_share),
                        tint = ChatTheme.colors.textHighEmphasis,
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
                    onClick = { imagePreviewViewModel.toggleGallery(isShowingGallery = true) }
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
     * Builds the image options based on the given [message]. These options let the user interact more
     * with the images they're observing.
     *
     * @param message The message that holds all the images.
     * @return [List] of options the user can choose from, in the form of [ImagePreviewOption].
     */
    @Composable
    private fun defaultImageOptions(message: Message): List<ImagePreviewOption> {
        val user by imagePreviewViewModel.user.collectAsState()
        val options = mutableListOf(
            ImagePreviewOption(
                title = stringResource(id = R.string.stream_compose_image_preview_reply),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_reply),
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = Reply(message)
            ),
            ImagePreviewOption(
                title = stringResource(id = R.string.stream_compose_image_preview_show_in_chat),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_show_in_chat),
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = ShowInChat(message)
            ),

            ImagePreviewOption(
                title = stringResource(id = R.string.stream_compose_image_preview_save_image),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_download),
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = SaveImage(message)
            )
        )

        if (message.user.id == user?.id) {
            options.add(
                ImagePreviewOption(
                    title = stringResource(id = R.string.stream_compose_image_preview_delete),
                    titleColor = ChatTheme.colors.errorAccent,
                    iconPainter = painterResource(id = R.drawable.stream_compose_ic_delete),
                    iconColor = ChatTheme.colors.errorAccent,
                    action = Delete(message)
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
    private fun onShareImageClick(attachment: Attachment) {
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
     * Represents the image gallery where the user can browse all images and quickly jump to them.
     *
     * @param pagerState The state of the pager, used to navigate to specific images.
     * @param attachments The list of attachments to be displayed.
     * @param modifier Modifier for styling.
     */
    @Composable
    private fun ImageGallery(
        pagerState: PagerState,
        attachments: List<Attachment>,
        modifier: Modifier = Modifier,
    ) {
        val message = imagePreviewViewModel.message

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.overlay)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = { imagePreviewViewModel.toggleGallery(isShowingGallery = false) }
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

                    ImageGalleryHeader()

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(ColumnCount),
                        content = {
                            itemsIndexed(attachments) { index, attachment ->
                                ImageGalleryItem(index, attachment, message.user, pagerState)
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * Represents the header of [ImageGallery] that allows the user to dismiss the component.
     */
    @Composable
    private fun ImageGalleryHeader() {
        Box(modifier = Modifier.fillMaxWidth()) {
            Icon(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(8.dp)
                    .clickable(
                        indication = rememberRipple(),
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = { imagePreviewViewModel.toggleGallery(isShowingGallery = false) }
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
     * Represents each item in the [ImageGallery].
     *
     * @param index The index of the item.
     * @param attachment The attachment data used to load the item image.
     * @param user The user who sent the image.
     * @param pagerState The state of the pager, used to navigate to items when the user selects them.
     */
    @Composable
    private fun ImageGalleryItem(
        index: Int,
        attachment: Attachment,
        user: User,
        pagerState: PagerState,
    ) {
        val coroutineScope = rememberCoroutineScope()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable {
                    coroutineScope.launch {
                        imagePreviewViewModel.toggleGallery(isShowingGallery = false)
                        pagerState.animateScrollToPage(index)
                    }
                }
        ) {
            val painter = rememberStreamImagePainter(attachment.imagePreviewUrl)

            Image(
                modifier = Modifier.fillMaxSize(),
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            UserAvatar(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .size(24.dp),
                user = user
            )
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
        private const val KeyMessageId: String = "messageId"

        /**
         * Represents the key for the starting attachment position based on the clicked attachment.
         */
        private const val KeyAttachmentPosition: String = "attachmentPosition"

        /**
         * Represents the key for the result of the preview, like scrolling to the message.
         */
        public const val KeyImagePreviewResult: String = "imagePreviewResult"

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
         * Used to build an [Intent] to start the [ImagePreviewActivity] with the required data.
         *
         * @param context The context to start the activity with.
         * @param messageId The ID of the message to explore the images of.
         * @param attachmentPosition The initial position of the clicked image.
         */
        public fun getIntent(context: Context, messageId: String, attachmentPosition: Int): Intent {
            return Intent(context, ImagePreviewActivity::class.java).apply {
                putExtra(KeyMessageId, messageId)
                putExtra(KeyAttachmentPosition, attachmentPosition)
            }
        }
    }
}
