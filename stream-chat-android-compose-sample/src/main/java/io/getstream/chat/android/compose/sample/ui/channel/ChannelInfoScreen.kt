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

package io.getstream.chat.android.compose.sample.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Suppress("LongMethod")
@Composable
fun ChannelInfoScreen(
    state: ChannelInfoViewModel.State,
    modifier: Modifier = Modifier,
    onNavigationIconClick: () -> Unit = {},
    onPinnedMessagesClick: () -> Unit,
    onConfirmDelete: () -> Unit,
    navigationIcon: @Composable () -> Unit = {
        DefaultChannelInfoNavigationIcon(onClick = onNavigationIconClick)
    },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            state.member?.let { member ->
                ChannelInfoHeader(
                    member = member,
                    navigationIcon = navigationIcon,
                )
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

@Composable
fun DefaultChannelInfoNavigationIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
            contentDescription = "Back",
            tint = ChatTheme.colors.textHighEmphasis,
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
