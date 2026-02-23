/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.channel.info

import android.content.Context
import android.text.format.DateUtils
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoMemberViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoMemberViewModelFactory
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
import io.getstream.chat.android.ui.common.utils.extensions.shouldShowOnlineIndicator
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChannelInfoMemberInfoModalSheet(
    modal: ChannelInfoViewEvent.MemberInfoModal,
    onMemberViewEvent: (event: ChannelInfoMemberViewEvent) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModelFactory = ChannelInfoMemberViewModelFactory(
        cid = modal.cid,
        memberId = modal.member.getUserId(),
    )
    val viewModel = viewModel<ChannelInfoMemberViewModel>(
        key = modal.member.getUserId(),
        factory = viewModelFactory,
    )

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
        onDismissRequest = onDismiss,
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()

        ChannelInfoMemberInfoModalSheetContent(
            state = state,
            onViewAction = { action ->
                viewModel.onViewAction(action)
                scope.launch { sheetState.hide() }
            },
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest(onMemberViewEvent)
    }
}

@Composable
private fun ChannelInfoMemberInfoModalSheetContent(
    state: ChannelInfoMemberViewState,
    onViewAction: (action: ChannelInfoMemberViewAction) -> Unit,
) {
    val isLoading = state is ChannelInfoMemberViewState.Loading
    ContentBox(
        modifier = Modifier.fillMaxWidth(),
        isLoading = isLoading,
    ) {
        val content = state as ChannelInfoMemberViewState.Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ChatTheme.componentFactory.ChannelInfoMemberInfoModalSheetTopBar(content.member)
            LazyColumn {
                items(content.options) { option ->
                    with(ChatTheme.componentFactory) {
                        ChannelInfoMemberOptionItem(
                            option = option,
                            onViewAction = onViewAction,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ChannelInfoMemberInfoModalSheetTopBar(member: Member) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val context = LocalContext.current
        val user = member.user
        Text(
            text = user.name.takeIf(String::isNotBlank) ?: user.id,
            style = ChatTheme.typography.headingMedium,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = user.getLastSeenText(context),
            style = ChatTheme.typography.metadataDefault,
            color = ChatTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (member.banned) {
            Text(
                text = member.getBanExpirationText(context),
                style = ChatTheme.typography.metadataDefault,
                color = ChatTheme.colors.accentError,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ChatTheme.componentFactory.UserAvatar(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(72.dp),
            user = user,
            showIndicator = user.shouldShowOnlineIndicator(
                userPresence = ChatTheme.userPresence,
                currentUser = null,
            ),
            showBorder = false,
        )
    }
}

private fun Member.getBanExpirationText(context: Context): String {
    val expires = banExpires
        ?: return context.getString(R.string.stream_ui_channel_info_member_modal_ban_no_expiration)

    val currentTime = System.currentTimeMillis()
    val diffInMillis = expires.time - currentTime

    return if (diffInMillis <= 0) {
        context.getString(R.string.stream_ui_channel_info_member_modal_ban_expired)
    } else {
        context.getString(
            R.string.stream_ui_channel_info_member_modal_ban_expires_at,
            DateUtils.getRelativeTimeSpanString(
                expires.time,
                currentTime,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE,
            ).toString().lowercase(),
        )
    }
}

@Preview
@Composable
private fun ChannelInfoMemberInfoModalSheetContentBannedPreview() {
    ChatTheme {
        ExpandedSheet {
            ChannelInfoMemberInfoModalSheetContent(banned = true)
        }
    }
}

@Preview
@Composable
private fun ChannelInfoMemberInfoModalSheetContentNotBannedPreview() {
    ChatTheme {
        ExpandedSheet {
            ChannelInfoMemberInfoModalSheetContent(banned = false)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedSheet(content: @Composable () -> Unit) {
    // Preview doesn't render modal bottom sheet,
    // so we need to mimic it with a card.
    Card(
        shape = BottomSheetDefaults.ExpandedShape,
        colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.backgroundElevationElevation1),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BottomSheetDefaults.DragHandle()
            content()
        }
    }
}

@Composable
internal fun ChannelInfoMemberInfoModalSheetContent(banned: Boolean) {
    val user = PreviewUserData.user1.copy(lastActive = Date())
    val member = if (banned) {
        Member(
            user = user,
            banned = true,
            banExpires = Calendar.getInstance().apply {
                add(Calendar.HOUR_OF_DAY, -1)
            }.time,
        )
    } else {
        Member(user)
    }

    ChannelInfoMemberInfoModalSheetContent(
        state = ChannelInfoMemberViewState.Content(
            member = member,
            options = buildList {
                add(ChannelInfoMemberViewState.Content.Option.MessageMember(member))
                if (banned) {
                    add(ChannelInfoMemberViewState.Content.Option.UnbanMember(member))
                } else {
                    add(ChannelInfoMemberViewState.Content.Option.BanMember(member))
                }
                add(ChannelInfoMemberViewState.Content.Option.RemoveMember(member))
            },
        ),
        onViewAction = {},
    )
}
