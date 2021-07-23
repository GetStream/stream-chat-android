package io.getstream.chat.android.compose.ui.imagepreview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
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
import io.getstream.chat.android.compose.viewmodel.imagepreview.ImagePreviewViewModelFactory
import io.getstream.chat.android.compose.viewmodel.imagepreview.ImagePreviewViewModel
import io.getstream.chat.android.offline.ChatDomain
import java.text.SimpleDateFormat
import java.util.*

class ImagePreviewActivity : AppCompatActivity() {

    private val factory by lazy {
        ImagePreviewViewModelFactory(ChatClient.instance(), ChatDomain.instance())
    }

    private val imagePreviewViewModel by viewModels<ImagePreviewViewModel>(factoryProducer = { factory })

    @ExperimentalComposeUiApi
    @ExperimentalPagerApi
    @ExperimentalFoundationApi
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

    @ExperimentalComposeUiApi
    @ExperimentalPagerApi
    @Composable
    private fun ImagePreviewContentWrapper(message: Message) {
        val pagerState = rememberPagerState(
            pageCount = message.attachments.size,
            initialOffscreenLimit = 2
        )

        Scaffold(modifier = Modifier.fillMaxSize(),
            topBar = { ImagePreviewTopBar(message) },
            content = { ImagePreviewContent(pagerState, message) },
            bottomBar = { ImagePreviewBottomBar(message, pagerState) })
    }

    @Composable
    private fun ImagePreviewTopBar(message: Message) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            elevation = 4.dp
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
                    contentDescription = stringResource(id = R.string.cancel)
                )

                ImagePreviewHeaderTitle(modifier = Modifier.align(Alignment.Center), message)

                // TODO extra actions
            }
        }
    }

    @Composable
    private fun ImagePreviewHeaderTitle(
        modifier: Modifier = Modifier,
        message: Message
    ) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = message.user.name,
                style = ChatTheme.typography.title3Bold
            )

            Text(
                text = SimpleDateFormat.getTimeInstance().format(message.createdAt ?: Date()),
                style = ChatTheme.typography.tabBar
            ) // TODO format properly
        }
    }

    @ExperimentalComposeUiApi
    @ExperimentalPagerApi
    @Composable
    private fun ImagePreviewContent(pagerState: PagerState, message: Message) {
        HorizontalPager(state = pagerState) { page ->

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

    @ExperimentalPagerApi
    @Composable
    private fun ImagePreviewBottomBar(message: Message, pagerState: PagerState) {
        val attachmentCount = message.attachments.size

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            elevation = 4.dp
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(8.dp),
                    imageVector = Icons.Default.Share,
                    contentDescription = stringResource(id = R.string.share)
                )

                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = stringResource(
                        id = R.string.image_order,
                        pagerState.currentPage + 1,
                        attachmentCount
                    ),
                    style = ChatTheme.typography.title3Bold
                )

                Icon(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(8.dp),
                    imageVector = Icons.Default.Apps,
                    contentDescription = stringResource(id = R.string.photos)
                )
            }
        }
    }

    companion object {
        private const val KEY_MESSAGE_ID = "messageId"

        fun getIntent(context: Context, messageId: String): Intent {
            return Intent(context, ImagePreviewActivity::class.java).apply {
                putExtra(KEY_MESSAGE_ID, messageId)
            }
        }
    }
}