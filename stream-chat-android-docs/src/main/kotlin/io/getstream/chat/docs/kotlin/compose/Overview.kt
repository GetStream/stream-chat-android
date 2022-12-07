// ktlint-disable filename

package io.getstream.chat.docs.kotlin.compose

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.messages.list.MessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageListViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * [ViewModels](https://getstream.io/chat/docs/sdk/android/compose/overview/#viewmodels)
 */
private object OverviewViewModelsSnippet {

    class MyActivity : AppCompatActivity() {
        // 1
        val factory by lazy {
            MessagesViewModelFactory(
                context = this,
                chatClient = ChatClient.instance(),
                channelId = "messaging:123",
                enforceUniqueReactions = true,
                messageLimit = 30
            )
        }

        // 2
        val listViewModel: MessageListViewModel by viewModels { factory }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            // 3
            setContent {
                ChatTheme {
                    MessageList(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize(),
                        viewModel = listViewModel // 4
                    )
                }
            }
        }
    }
}
