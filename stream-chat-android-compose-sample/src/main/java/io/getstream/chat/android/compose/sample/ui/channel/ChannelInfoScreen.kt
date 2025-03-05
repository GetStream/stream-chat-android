package io.getstream.chat.android.compose.sample.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.SimpleDialog
import io.getstream.chat.android.compose.ui.theme.ChatTheme

@Composable
fun ChannelInfoScreen(
    state: ChannelInfoViewModel.State,
    onBack: () -> Unit,
    onPinnedMessagesClick: () -> Unit,
    onConfirmDelete: () -> Unit,
) {
    Scaffold(
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
                    SimpleDialog(
                        title = stringResource(id = R.string.channel_info_option_delete_conversation),
                        message = stringResource(id = R.string.channel_info_delete_conversation_confirm),
                        onPositiveAction = {
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
