package io.getstream.chat.docs.kotlin.compose.guides

import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.MessageComposerAttachmentMediaItemOverlayParams
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.docs.R

/**
 * [Customizing Image and Video Previews](https://getstream.io/chat/docs/sdk/android/compose/guides/guides/customizing-image-and-video-previews/)
 */
private object CustomizingImageAndVideoPreviewsSnippet {

    class MessagesActivity : AppCompatActivity() {
        private val messageListViewModelFactory by lazy {
            MessagesViewModelFactory(
                context = this,
                channelId = "channelId",
            )
        }

        override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
            super.onCreate(savedInstanceState, persistentState)

            setContent {
                // Override the default component factory to customize attachment previews
                ChatTheme(componentFactory = CustomMediaComponentFactory) {
                    MessagesScreen(
                        viewModelFactory = messageListViewModelFactory,
                        onBackPressed = { finish() },
                    )
                }
            }
        }

        @Composable
        private fun CustomPlayButton(modifier: Modifier) {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                Icon(
                    modifier = Modifier
                        .padding(2.dp)
                        .fillMaxSize(0.8f),
                    painter = painterResource(id = R.drawable.stream_compose_ic_play),
                    tint = Color.White,
                    contentDescription = null,
                )
            }
        }

        /**
         * A [ChatComponentFactory] that renders a custom play button overlay above video
         * attachments in the message composer preview.
         *
         * To customize the overlay in the message list, override MessageComposerAttachmentMediaItemOverlay.
         */
        val CustomMediaComponentFactory = object : ChatComponentFactory {

            @Composable
            override fun MessageComposerAttachmentMediaItemOverlay(
                params: MessageComposerAttachmentMediaItemOverlayParams,
            ) {
                if (params.attachmentType == AttachmentType.VIDEO) {
                    // Render a custom play button above video items in the composer
                    CustomPlayButton(
                        modifier = Modifier
                            .padding(2.dp)
                            .background(
                                color = Color(red = 255, blue = 255, green = 255, alpha = 220),
                                shape = RoundedCornerShape(8.dp),
                            )
                            .fillMaxWidth(0.35f)
                            .aspectRatio(1.20f),
                    )
                }
            }
        }
    }
}
