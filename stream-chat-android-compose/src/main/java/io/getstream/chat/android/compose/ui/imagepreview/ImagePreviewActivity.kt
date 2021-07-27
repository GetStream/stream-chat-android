package io.getstream.chat.android.compose.ui.imagepreview

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.imagepreview.ImagePreviewViewModel
import io.getstream.chat.android.compose.viewmodel.imagepreview.ImagePreviewViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalPagerApi::class)
public class ImagePreviewActivity : AppCompatActivity() {

    private val factory by lazy {
        ImagePreviewViewModelFactory(ChatClient.instance())
    }

    private val imagePreviewViewModel by viewModels<ImagePreviewViewModel>(factoryProducer = { factory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val messageId = intent?.getStringExtra(KEY_MESSAGE_ID) ?: ""

        if (messageId.isBlank()) {
            finish()
        }

        imagePreviewViewModel.start(messageId)

        setContent {
            ChatTheme {
                val message = imagePreviewViewModel.message

                ImagePreviewContentWrapper(message)
            }
        }
    }

    @Composable
    private fun ImagePreviewContentWrapper(message: Message) {
        val pagerState = rememberPagerState(
            pageCount = message.attachments.size,
            initialOffscreenLimit = 2
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = { ImagePreviewTopBar(message) },
            content = { ImagePreviewContent(pagerState, message) },
            bottomBar = { ImagePreviewBottomBar(message, pagerState) }
        )
    }

    @Composable
    private fun ImagePreviewTopBar(message: Message) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            elevation = 4.dp,
            color = ChatTheme.colors.barsBackground
        ) {
            Box(
                Modifier.fillMaxWidth()
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { finish() },
                    imageVector = Icons.Default.Cancel,
                    contentDescription = stringResource(id = R.string.cancel),
                    tint = ChatTheme.colors.textHighEmphasis
                )

                ImagePreviewHeaderTitle(modifier = Modifier.align(Alignment.Center), message)

                // TODO extra actions (reply, show in chat, save image)
            }
        }
    }

    @Composable
    private fun ImagePreviewHeaderTitle(
        modifier: Modifier = Modifier,
        message: Message,
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

            Text(
                text = SimpleDateFormat.getTimeInstance().format(message.createdAt ?: Date()),
                style = ChatTheme.typography.footnote,
                color = ChatTheme.colors.textLowEmphasis
            ) // TODO format properly
        }
    }

    @Composable
    private fun ImagePreviewContent(pagerState: PagerState, message: Message) {
        HorizontalPager(modifier = Modifier.background(ChatTheme.colors.appBackground), state = pagerState) { page ->

            if (message.attachments.isNotEmpty()) {
                val painter = rememberImagePainter(data = message.attachments[page].imagePreviewUrl)

                // TODO apply transformations and allow gestures
                Image(
                    modifier = Modifier
                        .fillMaxSize(),
                    painter = painter,
                    contentDescription = null
                )
            }
        }
    }

    @Composable
    private fun ImagePreviewBottomBar(message: Message, pagerState: PagerState) {
        val attachmentCount = message.attachments.size

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
                        .clickable { onShareImageClick(message, pagerState.currentPage) },
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.share),
                    tint = ChatTheme.colors.textHighEmphasis
                )

                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(
                        id = R.string.image_order,
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
//                    contentDescription = stringResource(id = R.string.photos),
//                    tint = ChatTheme.colors.textHighEmphasis
//                )
            }
        }
    }

    private fun onShareImageClick(message: Message, currentPage: Int) {
        lifecycleScope.launch {
            val uri = StreamImageLoader.instance().loadAsBitmap(
                context = applicationContext,
                url = message.attachments[currentPage].imagePreviewUrl!!
            )?.let {
                StreamFileUtil.writeImageToSharableFile(applicationContext, it)
            }

            if (uri != null) {
                shareImage(uri)
            }
        }
    }

    private fun shareImage(imageUri: Uri) {
        ContextCompat.startActivity(
            this,
            Intent.createChooser(
                Intent(Intent.ACTION_SEND).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                },
                getString(R.string.stream_ui_attachment_gallery_share),
            ),
            null
        )
    }

    public companion object {
        private const val KEY_MESSAGE_ID = "messageId"

        public fun getIntent(context: Context, messageId: String): Intent {
            return Intent(context, ImagePreviewActivity::class.java).apply {
                putExtra(KEY_MESSAGE_ID, messageId)
            }
        }
    }
}
