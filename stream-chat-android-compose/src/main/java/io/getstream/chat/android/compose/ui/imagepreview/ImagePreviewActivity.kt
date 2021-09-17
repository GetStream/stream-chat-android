package io.getstream.chat.android.compose.ui.imagepreview

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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PanoramaFishEye
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.compose.rememberImagePainter
import com.getstream.sdk.chat.StreamFileUtil
import com.getstream.sdk.chat.images.StreamImageLoader
import com.getstream.sdk.chat.utils.extensions.imagePreviewUrl
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.imagepreview.Delete
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewAction
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewOption
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResult
import io.getstream.chat.android.compose.state.imagepreview.ImagePreviewResultType
import io.getstream.chat.android.compose.state.imagepreview.Reply
import io.getstream.chat.android.compose.state.imagepreview.SaveImage
import io.getstream.chat.android.compose.state.imagepreview.ShowInChat
import io.getstream.chat.android.compose.ui.common.Timestamp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.imagepreview.ImagePreviewViewModel
import io.getstream.chat.android.compose.viewmodel.imagepreview.ImagePreviewViewModelFactory
import io.getstream.chat.android.offline.ChatDomain
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalPagerApi::class)
public class ImagePreviewActivity : AppCompatActivity() {

    /**
     * Factory used to build the screen ViewModel given the received message ID.
     */
    private val factory by lazy {
        ImagePreviewViewModelFactory(
            chatClient = ChatClient.instance(),
            chatDomain = ChatDomain.instance(),
            messageId = intent?.getStringExtra(KEY_MESSAGE_ID) ?: ""
        )
    }

    /**
     * The ViewModel that exposes screen data.
     */
    private val imagePreviewViewModel by viewModels<ImagePreviewViewModel>(factoryProducer = { factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val messageId = intent?.getStringExtra(KEY_MESSAGE_ID) ?: ""
        val attachmentPosition = intent?.getIntExtra(KEY_ATTACHMENT_POSITION, 0) ?: 0

        if (messageId.isBlank()) {
            throw IllegalArgumentException("Missing messageId to load images.")
        }

        setContent {
            ChatTheme {
                val message = imagePreviewViewModel.message

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
        val pageCount = message.attachments.size
        val startingPosition = if (initialAttachmentPosition >= pageCount) 0 else initialAttachmentPosition

        val pagerState = rememberPagerState(
            pageCount = pageCount,
            initialPage = startingPosition,
            initialOffscreenLimit = 2
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = { ImagePreviewTopBar(message) },
                content = { ImagePreviewContent(pagerState, message.attachments) },
                bottomBar = { ImagePreviewBottomBar(message.attachments, pagerState) }
            )

            AnimatedVisibility(
                visible = imagePreviewViewModel.isShowingOptions,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ImagePreviewOptions(
                    options = defaultImageOptions(message = message),
                    pagerState = pagerState,
                    modifier = Modifier.animateEnterExit(
                        enter = slideInVertically(),
                        exit = slideOutVertically()
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
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { finish() },
                    imageVector = Icons.Default.Cancel,
                    contentDescription = stringResource(id = R.string.stream_compose_cancel),
                    tint = ChatTheme.colors.textHighEmphasis
                )

                ImagePreviewHeaderTitle(
                    modifier = Modifier
                        .weight(8f),
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
            imageVector = Icons.Default.MoreVert,
            contentDescription = stringResource(R.string.stream_compose_image_options),
            tint = ChatTheme.colors.textHighEmphasis
        )
    }

    @Composable
    private fun ImagePreviewOptions(
        options: List<ImagePreviewOption>,
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
                        ImagePreviewOptionItem(option, pagerState)

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
     */
    @Composable
    public fun ImagePreviewOptionItem(
        imagePreviewOption: ImagePreviewOption,
        pagerState: PagerState,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.barsBackground)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = rememberRipple(),
                    onClick = {
                        imagePreviewViewModel.toggleImageOptions(isShowingOptions = false)
                        handleImageAction(imagePreviewOption.action, pagerState.currentPage)
                    }
                )
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                modifier = Modifier
                    .size(18.dp),
                imageVector = imagePreviewOption.icon,
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
     */
    private fun handleImageAction(
        imagePreviewAction: ImagePreviewAction,
        currentPage: Int,
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
            is Delete -> imagePreviewViewModel.deleteCurrentImage(message.attachments[currentPage])
            is SaveImage -> {
                ChatDomain
                    .instance()
                    .downloadAttachment(message.attachments[currentPage])
                    .enqueue()
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
            putExtra(KEY_IMAGE_PREVIEW_RESULT, result)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    /**
     * Renders a horizontal pager that shows images and allows the user to swipe them and zoom in.
     *
     * @param pagerState The state of the pager, that represents the page count and the current page.
     * @param attachments The attachments to show.
     */
    @Composable
    private fun ImagePreviewContent(pagerState: PagerState, attachments: List<Attachment>) {
        HorizontalPager(modifier = Modifier.background(ChatTheme.colors.appBackground), state = pagerState) { page ->

            if (attachments.isNotEmpty()) {
                val painter = rememberImagePainter(data = attachments[page].imagePreviewUrl)

                var currentScale by remember { mutableStateOf(1f) }

                val transformableState = rememberTransformableState { zoomChange, _, _ ->
                    val newScale = (currentScale * zoomChange)
                        .coerceAtLeast(1f)
                        .coerceAtMost(3f)

                    currentScale = newScale
                }

                val scale by animateFloatAsState(targetValue = currentScale)

                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                        .transformable(state = transformableState)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    val newScale = when {
                                        currentScale == 3f -> 1f
                                        currentScale >= 2f -> 3f
                                        else -> 2f
                                    }

                                    currentScale = newScale
                                }
                            )
                        },
                    painter = painter,
                    contentDescription = null
                )

                if (pagerState.currentPage != page) {
                    currentScale = 1f
                }
            }
        }
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
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                        .clickable { onShareImageClick(attachments[pagerState.currentPage]) },
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.stream_compose_image_preview_share),
                    tint = ChatTheme.colors.textHighEmphasis
                )

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

                // TODO we need to open a gallery of images here
//                Icon(
//                    modifier = Modifier
//                        .align(Alignment.CenterEnd)
//                        .padding(8.dp),
//                    imageVector = Icons.Default.Apps,
//                    contentDescription = stringResource(id = R.string.stream_compose_image_preview_photos),
//                    tint = ChatTheme.colors.textHighEmphasis
//                )
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
                icon = Icons.Default.Reply,
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = Reply(message)
            ),
            ImagePreviewOption(
                title = stringResource(id = R.string.stream_compose_image_preview_show_in_chat),
                titleColor = ChatTheme.colors.textHighEmphasis,
                icon = Icons.Default.PanoramaFishEye,
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = ShowInChat(message)
            ),

            ImagePreviewOption(
                title = stringResource(id = R.string.stream_compose_image_preview_save_image),
                titleColor = ChatTheme.colors.textHighEmphasis,
                icon = Icons.Default.Download,
                iconColor = ChatTheme.colors.textHighEmphasis,
                action = SaveImage(message)
            )
        )

        if (message.user.id == user?.id) {
            options.add(
                ImagePreviewOption(
                    title = stringResource(id = R.string.stream_compose_image_preview_delete),
                    titleColor = ChatTheme.colors.errorAccent,
                    icon = Icons.Default.Delete,
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

    public companion object {
        private const val KEY_MESSAGE_ID: String = "messageId"
        private const val KEY_ATTACHMENT_POSITION: String = "attachmentPosition"
        public const val KEY_IMAGE_PREVIEW_RESULT: String = "imagePreviewResult"

        public fun getIntent(context: Context, messageId: String, attachmentPosition: Int): Intent {
            return Intent(context, ImagePreviewActivity::class.java).apply {
                putExtra(KEY_MESSAGE_ID, messageId)
                putExtra(KEY_ATTACHMENT_POSITION, attachmentPosition)
            }
        }
    }
}
