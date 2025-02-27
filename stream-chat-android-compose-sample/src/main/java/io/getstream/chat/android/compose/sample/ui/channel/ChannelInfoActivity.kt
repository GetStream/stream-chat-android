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

package io.getstream.chat.android.compose.sample.ui.channel

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.BaseConnectedActivity
import io.getstream.chat.android.compose.sample.ui.pinned.PinnedMessagesActivity
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Activity showing information about a 1-to-1 channel (chat).
 */
class ChannelInfoActivity : BaseConnectedActivity() {

    companion object {
        private const val KEY_CHANNEL_ID = "channelId"

        /**
         * Intent key notifying the caller that the channel was deleted.
         */
        const val KEY_CHANNEL_DELETED = "channelDeleted"

        /**
         * Creates an [Intent] for starting the [ChannelInfoActivity].
         *
         * @param context The calling [Context], used for building the [Intent].
         * @param channelId The ID of the channel for which the pinned messages are shown.
         */
        fun createIntent(context: Context, channelId: String) =
            Intent(context, ChannelInfoActivity::class.java).putExtra(KEY_CHANNEL_ID, channelId)
    }

    private val viewModelFactory by lazy {
        ChannelInfoViewModelFactory(
            cid = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
        )
    }

    private val viewModel by viewModels<ChannelInfoViewModel>(factoryProducer = { viewModelFactory })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChatTheme {
                val state by viewModel.state.collectAsState()
                ChannelInfoScreen(
                    state = state,
                    onBack = ::finish,
                    onPinnedMessagesClick = ::openPinnedMessages,
                    onConfirmDelete = viewModel::onDeleteChannel,
                )
            }
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    viewModel.error.collectLatest(::showError)
                }
            }
            LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    viewModel.channelDeleted.collectLatest {
                        val data = Intent().putExtra(KEY_CHANNEL_DELETED, true)
                        setResult(RESULT_OK, data)
                        finish()
                    }
                }
            }
        }
    }

    private fun openPinnedMessages() {
        val intent = PinnedMessagesActivity.createIntent(
            context = this,
            channelId = requireNotNull(intent.getStringExtra(KEY_CHANNEL_ID)),
        )
        startActivity(intent)
    }

    private fun showError(error: ChannelInfoViewModel.ErrorEvent) {
        val message = when (error) {
            ChannelInfoViewModel.ErrorEvent.DeleteError -> R.string.channel_info_error_load_channel_details
            ChannelInfoViewModel.ErrorEvent.LoadingError -> R.string.channel_info_error_delete_channel
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    @Composable
    private fun ChannelInfoScreen(
        state: ChannelInfoViewModel.State,
        onBack: () -> Unit,
        onPinnedMessagesClick: () -> Unit,
        onConfirmDelete: () -> Unit,
    ) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            topBar = {
                state.member?.let { member ->
                    ChannelInfoHeader(member = member, onBack = onBack)
                }
            },
            content = { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ChatTheme.colors.appBackground)
                        .padding(padding),
                ) {
                    var showConfirmDeleteDialog by remember { mutableStateOf(false) }
                    LazyColumn {
                        // Spacer
                        item {
                            ChannelInfoContentDivider(height = 8.dp)
                        }
                        // Pinned messages
                        item {
                            ChannelInfoOptionItem(
                                icon = R.drawable.stream_compose_ic_message_pinned,
                                text = stringResource(id = R.string.channel_info_option_pinned_messages),
                                onClick = onPinnedMessagesClick,
                            )
                            ChannelInfoContentDivider(height = 1.dp)
                        }
                        // Delete channel
                        if (state.canDeleteChannel) {
                            item {
                                ChannelInfoContentDivider(height = 8.dp)
                                ChannelInfoOptionItem(
                                    icon = R.drawable.stream_compose_ic_delete_red,
                                    iconTint = ChatTheme.colors.errorAccent,
                                    text = stringResource(id = R.string.channel_info_option_delete_conversation),
                                    textColor = ChatTheme.colors.errorAccent,
                                    onClick = { showConfirmDeleteDialog = true },
                                    trailingContent = {},
                                )
                                ChannelInfoContentDivider(height = 1.dp)
                            }
                        }
                    }

                    if (showConfirmDeleteDialog) {
                        ConfirmDeleteDialog(
                            onConfirm = {
                                onConfirmDelete()
                                showConfirmDeleteDialog = false
                            },
                            onDismiss = {
                                showConfirmDeleteDialog = false
                            },
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun ConfirmDeleteDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    SimpleDialog(
        title = stringResource(id = R.string.channel_info_option_delete_conversation),
        message = stringResource(id = R.string.channel_info_delete_conversation_confirm),
        onPositiveAction = onConfirm,
        onDismiss = onDismiss,
    )
}
