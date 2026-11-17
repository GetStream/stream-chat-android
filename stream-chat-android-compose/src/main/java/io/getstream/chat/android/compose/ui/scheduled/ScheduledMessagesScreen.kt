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

package io.getstream.chat.android.compose.ui.scheduled

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.components.DefaultListHeaderCenterContent
import io.getstream.chat.android.compose.ui.components.HeaderScaffold
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.scheduled.ScheduledMessageListViewModel
import io.getstream.chat.android.compose.viewmodel.scheduled.ScheduledMessageListViewModelFactory

/**
 * The screen of the current user's scheduled messages: a header with a back button and the [ScheduledMessageList].
 * Apps decide how users reach it, for example from the scheduled messages banner of the channel screen.
 *
 * @param viewModelFactory The factory of the [ScheduledMessageListViewModel]. Pass the `cid` of a channel to it to
 * show the scheduled messages of that channel only.
 * @param title The title of the header. Also the screen's `paneTitle` semantic, announced by TalkBack when the
 * screen appears as a pane.
 * @param onBackPressed Called when the user taps the back button.
 */
@Composable
public fun ScheduledMessagesScreen(
    viewModelFactory: ScheduledMessageListViewModelFactory = ScheduledMessageListViewModelFactory(),
    title: String = stringResource(R.string.stream_compose_scheduled_message_list_title),
    onBackPressed: () -> Unit = {},
) {
    val viewModel: ScheduledMessageListViewModel = viewModel(factory = viewModelFactory)
    val connectionState by viewModel.connectionState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.backgroundCoreApp)
            .semantics { paneTitle = title },
    ) {
        HeaderScaffold(
            leadingContent = {
                BackButton(
                    painter = painterResource(R.drawable.stream_design_ic_arrow_left),
                    onBackPressed = onBackPressed,
                )
            },
            centerContent = { DefaultListHeaderCenterContent(connectionState = connectionState, title = title) },
            trailingContent = { Spacer(modifier = Modifier.size(AvatarSize.ExtraLarge)) },
        )
        ScheduledMessageList(
            viewModel = viewModel,
            modifier = Modifier.fillMaxSize(),
        )
    }
}
