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

package io.getstream.chat.android.compose.sample.feature.channel.add.group

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.MessagesActivity
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.flow.collectLatest

/**
 * Activity hosting the "Add group channel" flow.
 */
class AddGroupChannelActivity : BaseConnectedActivity() {

    private val viewModel by viewModels<AddGroupChannelViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                AddGroupChannelScreen(
                    state = viewModel.state.collectAsStateWithLifecycle().value,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onUserClick = viewModel::onUserClick,
                    onEndReached = viewModel::onEndOfListReached,
                    onNext = viewModel::onNext,
                    onChannelNameChanged = viewModel::onChannelNameChanged,
                    onCreateChannelClick = viewModel::onCreateChannelClick,
                    onBack = viewModel::onBack,
                )
            }
            // Handle navigation events
            LaunchedEffect(Unit) {
                viewModel.navigationEvent.collectLatest {
                    when (it) {
                        is AddGroupChannelViewModel.NavigationEvent.NavigateToChannel -> openChannel(it.cid)
                        is AddGroupChannelViewModel.NavigationEvent.Close -> finish()
                    }
                }
            }
            // Handle error events
            LaunchedEffect(Unit) {
                viewModel.errorEvent.collectLatest {
                    val message = when (it) {
                        is AddGroupChannelViewModel.ErrorEvent.SearchUsersError ->
                            R.string.add_group_channel_error_load_users

                        is AddGroupChannelViewModel.ErrorEvent.CreateChannelError ->
                            R.string.add_group_channel_error_create_channel
                    }
                    Toast.makeText(this@AddGroupChannelActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openChannel(cid: String) {
        val intent = MessagesActivity.createIntent(this, cid)
        startActivity(intent)
        finish()
    }
}
