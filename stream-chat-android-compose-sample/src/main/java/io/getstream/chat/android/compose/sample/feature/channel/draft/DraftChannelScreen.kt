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

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.messages.header.MessageListHeader
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channel.ChannelHeaderViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelHeaderViewModelFactory
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.ui.common.feature.channel.draft.DraftChannelViewAction
import io.getstream.chat.android.ui.common.state.channel.draft.DraftChannelViewState
import io.getstream.chat.android.ui.common.state.messages.list.ChannelHeaderViewState

@Composable
fun DraftChannelScreen(
    viewModel: DraftChannelViewModel,
    onNavigationIconClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DraftChannelContent(
        modifier = modifier,
        state = state,
        onNavigationIconClick = onNavigationIconClick,
        onViewAction = viewModel::onViewAction,
    )
}

@Composable
private fun DraftChannelContent(
    state: DraftChannelViewState,
    onNavigationIconClick: () -> Unit,
    onViewAction: (action: DraftChannelViewAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is DraftChannelViewState.Loading -> LoadingIndicator(
            modifier = modifier.fillMaxSize(),
        )

        is DraftChannelViewState.Content -> Scaffold(
            modifier = modifier,
            topBar = {
                DraftChannelTopBar(
                    cid = state.channel.cid,
                    onNavigationIconClick = onNavigationIconClick,
                )
            },
            bottomBar = {
                DraftChannelBottomBar(
                    cid = state.channel.cid,
                    onMessageSent = { onViewAction(DraftChannelViewAction.MessageSent) },
                )
            },
            containerColor = ChatTheme.colors.appBackground,
        ) { padding ->
            ChatTheme.componentFactory.MessageListEmptyContent(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
            )
        }
    }
}

@Composable
private fun DraftChannelTopBar(
    cid: String,
    onNavigationIconClick: () -> Unit,
) {
    val viewModel = viewModel<ChannelHeaderViewModel>(factory = ChannelHeaderViewModelFactory(cid))
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (val content = state) {
        is ChannelHeaderViewState.Loading -> LoadingIndicator(
            modifier = Modifier.fillMaxWidth(),
        )

        is ChannelHeaderViewState.Content -> MessageListHeader(
            channel = content.channel,
            currentUser = content.currentUser,
            connectionState = content.connectionState,
            onBackPressed = onNavigationIconClick,
        )
    }
}

@Composable
private fun DraftChannelBottomBar(
    cid: String,
    onMessageSent: () -> Unit,
) {
    val context = LocalContext.current
    val viewModel = viewModel<MessageComposerViewModel>(key = cid, factory = MessagesViewModelFactory(context, cid))
    MessageComposer(
        viewModel = viewModel,
        onSendMessage = { message ->
            viewModel.sendMessage(message)
            onMessageSent()
        },
    )
}
