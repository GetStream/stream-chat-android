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

package io.getstream.chat.android.compose.sample.feature.channel.draft

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.flow.collectLatest

class DraftChannelActivity : ComponentActivity() {

    companion object {
        private const val KEY_MEMBER_IDS = "memberIds"

        /**
         * Creates an [Intent] for starting the [DraftChannelActivity].
         *
         * @param context The calling [Context], used for building the [Intent].
         * @param memberIds The list of member IDs to be used for creating the channel.
         */
        fun createIntent(context: Context, memberIds: List<String>) =
            Intent(context, DraftChannelActivity::class.java)
                .putExtra(KEY_MEMBER_IDS, memberIds.toTypedArray())
    }

    private val viewModelFactory by lazy {
        DraftChannelViewModelFactory(
            memberIds = requireNotNull(intent.getStringArrayExtra(KEY_MEMBER_IDS)).toList(),
        )
    }
    private val viewModel by viewModels<DraftChannelViewModel> { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                DraftChannelScreen(
                    modifier = Modifier.systemBarsPadding(),
                    viewModel = viewModel,
                    onNavigationIconClick = ::finish,
                )
                LaunchedEffect(viewModel) {
                    viewModel.events.collectLatest { event ->
                        when (event) {
                            is DraftChannelViewEvent.NavigateToChannel -> {
                                startActivity(
                                    MessagesActivity.createIntent(
                                        context = applicationContext,
                                        channelId = event.cid,
                                    ),
                                )
                                finish()
                            }

                            is DraftChannelViewEvent.DraftChannelError ->
                                Toast.makeText(
                                    applicationContext,
                                    R.string.draft_channel_error,
                                    Toast.LENGTH_SHORT,
                                ).show()
                        }
                    }
                }
            }
        }
    }
}
