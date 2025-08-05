/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.ui.channel.attachments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.R.string.stream_ui_message_list_video_display_error
import io.getstream.chat.android.compose.ui.channel.attachments.ChannelMediaAttachmentsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelAttachmentsViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelAttachmentsViewModelFactory
import io.getstream.chat.android.models.AttachmentType
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewEvent
import kotlinx.coroutines.flow.collectLatest

class ChannelMediaAttachmentsActivity : ComponentActivity() {
    companion object {
        private const val KEY_CID = "cid"

        fun createIntent(context: Context, cid: String) =
            Intent(context, ChannelMediaAttachmentsActivity::class.java)
                .putExtra(KEY_CID, cid)
    }

    private val viewModelFactory by lazy {
        ChannelAttachmentsViewModelFactory(
            cid = requireNotNull(intent.getStringExtra(KEY_CID)),
            attachmentTypes = listOf(AttachmentType.IMAGE, AttachmentType.VIDEO),
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                val viewModel = viewModel<ChannelAttachmentsViewModel>(factory = viewModelFactory)
                ChannelMediaAttachmentsScreen(
                    modifier = Modifier.statusBarsPadding(),
                    viewModelFactory = viewModelFactory,
                    onNavigationIconClick = ::finish,
                    onVideoPlaybackError = {
                        Toast.makeText(
                            applicationContext,
                            stream_ui_message_list_video_display_error,
                            Toast.LENGTH_SHORT,
                        ).show()
                    },
                )
                LaunchedEffect(viewModel) {
                    viewModel.events.collectLatest { event ->
                        when (event) {
                            is ChannelAttachmentsViewEvent.LoadMoreError ->
                                Toast.makeText(
                                    applicationContext,
                                    R.string.channel_attachments_media_loading_more_error,
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    }
                }
            }
        }
    }
}
