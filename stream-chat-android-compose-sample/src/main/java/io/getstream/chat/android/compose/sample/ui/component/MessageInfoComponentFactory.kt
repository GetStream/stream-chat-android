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

package io.getstream.chat.android.compose.sample.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.deliveredReadsOf
import io.getstream.chat.android.client.extensions.readsOf
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatComponentFactory
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.ui.common.state.messages.CustomAction
import io.getstream.chat.android.ui.common.state.messages.MessageAction
import io.getstream.chat.android.ui.common.utils.extensions.initials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Date
import kotlin.time.Duration.Companion.hours

/**
 * Factory for creating components related to message info.
 */
class MessageInfoComponentFactory : ChatComponentFactory {

    /**
     * Creates a message menu with option for message info.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("LongMethod")
    @Composable
    override fun MessageMenu(
        modifier: Modifier,
        message: Message,
        messageOptions: List<MessageOptionItemState>,
        ownCapabilities: Set<String>,
        onMessageAction: (MessageAction) -> Unit,
        onShowMore: () -> Unit,
        onDismiss: () -> Unit,
    ) {
        var showMessageInfoDialog by remember { mutableStateOf(false) }

        val allOptions = listOf(
            MessageOptionItemState(
                title = R.string.message_option_message_info,
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = rememberVectorPainter(Icons.Outlined.Info),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = CustomAction(message, mapOf("message_info" to true)),
            ),
        ) + messageOptions

        val extendedOnMessageAction: (MessageAction) -> Unit = { action ->
            when {
                action is CustomAction && action.extraProperties.contains("message_info") ->
                    showMessageInfoDialog = true

                else -> onMessageAction(action)
            }
        }

        var dismissed by remember { mutableStateOf(false) }

        if (showMessageInfoDialog) {
            ModalBottomSheet(
                onDismissRequest = {
                    showMessageInfoDialog = false
                    onDismiss()
                    dismissed = true // Mark as dismissed to avoid animating the menu again
                },
                containerColor = ChatTheme.colors.appBackground,
            ) {
                val coroutineScope = rememberCoroutineScope()
                val state by readsOf(message, coroutineScope).collectAsState(null)
                state?.let {
                    val (reads, deliveredReads) = it
                    MessageInfoContent(
                        reads = reads,
                        deliveredReads = deliveredReads,
                    )
                }
            }
        } else if (!dismissed) {
            super.MessageMenu(
                modifier = modifier,
                message = message,
                messageOptions = allOptions,
                ownCapabilities = ownCapabilities,
                onMessageAction = extendedOnMessageAction,
                onShowMore = onShowMore,
                onDismiss = onDismiss,
            )
        }
    }

    @Composable
    private fun readsOf(
        message: Message,
        coroutineScope: CoroutineScope,
    ): Flow<Pair<List<ChannelUserRead>, List<ChannelUserRead>>> = ChatClient.instance()
        .watchChannelAsState(
            cid = message.cid,
            messageLimit = 0,
            coroutineScope = coroutineScope,
        ).filterNotNull()
        .flatMapLatest { it.reads }
        .map {
            val channel = Channel(read = it)

            val reads = channel.readsOf(message)
                .sortedByDescending(ChannelUserRead::lastRead)

            val deliveredReads = channel.deliveredReadsOf(message)
                .sortedByDescending { it.lastDeliveredAt ?: Date(0) } - reads

            reads to deliveredReads
        }
}

@Composable
private fun MessageInfoContent(
    reads: List<ChannelUserRead>,
    deliveredReads: List<ChannelUserRead>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = 16.dp,
        ),
    ) {
        // Read by section
        section(
            items = reads,
            labelResId = R.string.message_info_read_by,
            skipTopPadding = true,
        )
        // Delivered to section
        section(
            items = deliveredReads,
            labelResId = R.string.message_info_delivered_to,
        )
    }
}

private fun LazyListScope.section(
    items: List<ChannelUserRead>,
    @StringRes labelResId: Int,
    skipTopPadding: Boolean = false,
) {
    if (items.isNotEmpty()) {
        item {
            if (skipTopPadding) {
                PaneTitle(
                    text = stringResource(labelResId, items.size),
                    padding = PaddingValues(
                        start = 16.dp,
                        bottom = 8.dp,
                        end = 16.dp,
                    ),
                )
            } else {
                PaneTitle(text = stringResource(labelResId, items.size))
            }
        }
        itemsIndexed(
            items = items,
            key = { _, item -> item.user.id },
        ) { index, item ->
            PaneRow(
                index = index,
                lastIndex = items.lastIndex,
            ) {
                ReadItem(userRead = item)
            }
        }
    }
}

@Composable
private fun ReadItem(userRead: ChannelUserRead) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            modifier = Modifier.size(48.dp),
            imageUrl = userRead.user.image,
            initials = userRead.user.initials,
            contentDescription = userRead.user.name,
        )

        Text(
            text = userRead.user.name.takeIf(String::isNotBlank) ?: userRead.user.id,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Suppress("MagicNumber")
@Preview
@Composable
private fun MessageInfoScreenPreview() {
    val sentDate = Calendar.getInstance().apply {
        set(2025, Calendar.AUGUST, 15, 8, 15)
    }.time
    val user1 = User(id = "jane", name = "Jane Doe")
    val user2 = User(id = "bob", name = "Bob Smith")
    val user3 = User(id = "alice", name = "Alice Johnson")
    val reads = listOf(
        ChannelUserRead(
            user = user1,
            lastReceivedEventDate = Date(),
            unreadMessages = 0,
            lastRead = sentDate.apply { time += 2.hours.inWholeMilliseconds },
            lastReadMessageId = null,
        ),
        ChannelUserRead(
            user = user2,
            lastReceivedEventDate = Date(),
            unreadMessages = 0,
            lastRead = sentDate.apply { time += 3.hours.inWholeMilliseconds },
            lastReadMessageId = null,
        ),
    )
    val deliveredReads = listOf(
        ChannelUserRead(
            user = user3,
            lastReceivedEventDate = Date(),
            unreadMessages = 0,
            lastRead = Date(),
            lastReadMessageId = null,
            lastDeliveredAt = sentDate.apply { time += 1.hours.inWholeMilliseconds },
            lastDeliveredMessageId = null,
        ),
    )
    ChatTheme {
        MessageInfoContent(
            deliveredReads = deliveredReads,
            reads = reads,
        )
    }
}
