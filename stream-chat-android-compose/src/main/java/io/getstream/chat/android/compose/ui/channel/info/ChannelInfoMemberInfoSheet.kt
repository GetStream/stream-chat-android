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

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.ui.components.ContentBox
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoMemberViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelInfoMemberViewModelFactory
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoMemberViewEvent
import io.getstream.chat.android.ui.common.feature.channel.info.ChannelInfoViewEvent
import io.getstream.chat.android.ui.common.state.channel.info.ChannelInfoMemberViewState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChannelInfoMemberInfoSheet(
    modal: ChannelInfoViewEvent.MemberInfoModal,
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

    fun hideSheet(onHide: () -> Unit) {
        // We need to handle the dismissal of the sheet ourselves
        // because we are hiding it manually.
        scope.launch { sheetState.hide() }
            .invokeOnCompletion {
                if (!sheetState.isVisible) {
                    onHide()
                }
            }
    }

    ModalBottomSheet(
        sheetState = sheetState,
        containerColor = ChatTheme.colors.barsBackground,
        onDismissRequest = onDismiss,
    ) {
        val state by viewModel.state.collectAsStateWithLifecycle()

        ChannelInfoMemberInfoSheetContent(
            state = state,
            onViewAction = { action ->
                viewModel.onViewAction(action)
                hideSheet(onDismiss)
            },
        )
    }

    var modal by remember { mutableStateOf<ChannelInfoMemberViewEvent.Modal?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            if (event is ChannelInfoMemberViewEvent.Modal) {
                hideSheet { modal = event }
            }
        }
    }

    ChannelInfoMemberInfoConfirmationModal(
        modal = modal,
        onViewAction = viewModel::onViewAction,
        onDismiss = {
            modal = null
            onDismiss()
        },
    )
}

@Composable
private fun ChannelInfoMemberInfoSheetContent(
    state: ChannelInfoMemberViewState,
    onViewAction: (action: ChannelInfoMemberViewAction) -> Unit,
) {
    val isLoading = state is ChannelInfoMemberViewState.Loading
    ContentBox(
        contentAlignment = if (isLoading) Alignment.Center else Alignment.TopCenter,
        isLoading = isLoading,
    ) {
        val content = state as ChannelInfoMemberViewState.Content
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChannelInfoMemberInfo(
                user = content.member.user,
                avatarAlignment = Alignment.Bottom,
            )
            LazyColumn {
                items(content.options) { option ->
                    ChannelInfoMemberOption(
                        option = option,
                        onViewAction = onViewAction,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ChannelInfoMemberInfoSheetContentPreview() {
    ChatTheme {
        Card(
            shape = BottomSheetDefaults.ExpandedShape,
            colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.barsBackground),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                BottomSheetDefaults.DragHandle()
                val member = Member(user = PreviewUserData.user1.copy(lastActive = Date()))
                ChannelInfoMemberInfoSheetContent(
                    state = ChannelInfoMemberViewState.Content(
                        member = member,
                        options = listOf(
                            ChannelInfoMemberViewState.Content.Option.MessageMember(member),
                            ChannelInfoMemberViewState.Content.Option.BanMember(member),
                            ChannelInfoMemberViewState.Content.Option.UnbanMember(member),
                            ChannelInfoMemberViewState.Content.Option.RemoveMember(member),
                        ),
                    ),
                    onViewAction = {},
                )
            }
        }
    }
}
