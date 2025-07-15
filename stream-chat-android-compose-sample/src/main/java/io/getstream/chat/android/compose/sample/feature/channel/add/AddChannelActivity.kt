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

package io.getstream.chat.android.compose.sample.feature.channel.add

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.add.group.AddGroupChannelActivity
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Activity hosting the "Add channel" flow.
 */
class AddChannelActivity : ComponentActivity() {

    private val viewModel by viewModels<AddChannelViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                AddChannelScreen(
                    state = viewModel.state.collectAsStateWithLifecycle().value,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onCreateGroupClick = ::openCreateGroup,
                    onUserClick = viewModel::onUserClick,
                    onMessageSent = viewModel::onMessageSent,
                    onEndReached = viewModel::onEndOfListReached,
                    onBack = ::finish,
                )
            }
            // Handle navigation events
            LaunchedEffect(Unit) {
                viewModel.navigationEvent.collectLatest {
                    when (it) {
                        is AddChannelViewModel.NavigationEvent.NavigateToChannel -> openChannel(it.cid)
                    }
                }
            }
            // Handle error events
            LaunchedEffect(Unit) {
                viewModel.errorEvent.collectLatest {
                    val message = when (it) {
                        is AddChannelViewModel.ErrorEvent.SearchUsersError ->
                            R.string.add_channel_error_load_users

                        is AddChannelViewModel.ErrorEvent.CreateDraftChannelError ->
                            R.string.add_channel_error_create_draft_channel

                        is AddChannelViewModel.ErrorEvent.CreateChannelError ->
                            R.string.add_channel_error_create_channel
                    }
                    Toast.makeText(this@AddChannelActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openChannel(cid: String) {
        val intent = MessagesActivity.createIntent(this, cid)
        startActivity(intent)
        finish()
    }

    private fun openCreateGroup() {
        val intent = Intent(this, AddGroupChannelActivity::class.java)
        startActivity(intent)
        finish()
    }
}
