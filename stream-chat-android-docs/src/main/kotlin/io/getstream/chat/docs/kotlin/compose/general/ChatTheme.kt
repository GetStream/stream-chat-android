// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose.general

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.audio.recording.DefaultStreamMediaRecorder
import com.getstream.sdk.chat.audio.recording.StreamMediaRecorder
import io.getstream.chat.android.compose.state.messages.attachments.StatefulStreamMediaRecorder
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamShapes
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * [Usage](https://getstream.io/chat/docs/sdk/android/compose/general-customization/chat-theme/#usage)
 */
private object ChatThemeUsageSnippet {

    class MessageListActivity : AppCompatActivity() {

        //TODO add this and related entries to docs when documentation effort occurs
        private val streamMediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(applicationContext)
        private val statefulStreamMediaRecorder = StatefulStreamMediaRecorder(streamMediaRecorder)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme {
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = "messaging:123",
                            messageLimit = 30
                        ),
                        onBackPressed = { finish() },
                        onHeaderTitleClick = {},
                        //TODO add this and related entries to docs when documentation effort occurs
                        statefulStreamMediaRecorder = statefulStreamMediaRecorder,
                    )
                }
            }
        }
    }
}

/**
 * [Customization](https://getstream.io/chat/docs/sdk/android/compose/general-customization/chat-theme/#customization)
 */
private object ChatThemeCustomizationSnippet {

    class MyActivity : AppCompatActivity() {

        //TODO add this and related entries to docs when documentation effort occurs
        private val streamMediaRecorder: StreamMediaRecorder = DefaultStreamMediaRecorder(applicationContext)
        private val statefulStreamMediaRecorder = StatefulStreamMediaRecorder(streamMediaRecorder)

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            setContent {
                ChatTheme(
                    shapes = StreamShapes.defaultShapes().copy( // Customizing the shapes
                        avatar = RoundedCornerShape(8.dp),
                        attachment = RoundedCornerShape(16.dp),
                        inputField = RectangleShape,
                        myMessageBubble = RoundedCornerShape(16.dp),
                        otherMessageBubble = RoundedCornerShape(16.dp),
                        bottomSheet = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                ) {
                    MessagesScreen(
                        viewModelFactory = MessagesViewModelFactory(
                            context = this,
                            channelId = "messaging:123",
                        ),
                        onBackPressed = { finish() },
                        onHeaderTitleClick = {},
                        //TODO add this and related entries to docs when documentation effort occurs
                        statefulStreamMediaRecorder = statefulStreamMediaRecorder,
                    )
                }
            }
        }
    }
}
