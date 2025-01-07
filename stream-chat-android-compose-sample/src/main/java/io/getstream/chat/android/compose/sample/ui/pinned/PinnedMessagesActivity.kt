/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.pinned

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.ui.pinned.PinnedMessageList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModel
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModelFactory
import io.getstream.chat.android.models.Message

/**
 * Activity displaying the list of pinned messages from a channel.
 */
class PinnedMessagesActivity : BaseConnectedActivity() {

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        /**
         * Creates an [Intent] for starting the [PinnedMessagesActivity].
         *
         * @param context The calling [Context], used for building the [Intent].
         * @param channelId The ID of the channel for which the pinned messages are shown.
         */
        fun createIntent(context: Context, channelId: String) =
            Intent(context, PinnedMessagesActivity::class.java)
                .putExtra(KEY_CHANNEL_ID, channelId)
    }

    private val viewModelFactory by lazy {
        PinnedMessageListViewModelFactory(
            cid = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
        )
    }
    private val viewModel by viewModels<PinnedMessageListViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                Surface {
                    Scaffold(
                        topBar = {
                            PinnedMessageListHeader(onBack = ::finish)
                        },
                        content = { padding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(padding),
                            ) {
                                PinnedMessageList(
                                    modifier = Modifier.fillMaxSize(),
                                    viewModel = viewModel,
                                    onPinnedMessageClick = ::openMessage,
                                )
                            }
                        },
                    )
                }
            }
        }
    }

    private fun openMessage(message: Message) {
        val intent = MessagesActivity.createIntent(
            context = this,
            channelId = message.cid,
            messageId = message.id,
            parentMessageId = message.parentId,
        )
        startActivity(intent)
    }

    @Composable
    private fun PinnedMessageListHeader(onBack: () -> Unit) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .background(ChatTheme.colors.barsBackground),
            shadowElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ChatTheme.colors.barsBackground)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
                        contentDescription = "Back",
                        tint = ChatTheme.colors.textHighEmphasis,
                    )
                }
                Text(
                    text = stringResource(id = R.string.channel_info_option_pinned_messages),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = ChatTheme.colors.textHighEmphasis,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.width(44.dp))
            }
        }
    }
}
