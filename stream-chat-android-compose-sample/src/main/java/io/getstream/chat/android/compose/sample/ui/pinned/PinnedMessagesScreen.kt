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

package io.getstream.chat.android.compose.sample.ui.pinned

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.ui.component.AppToolbar
import io.getstream.chat.android.compose.ui.pinned.PinnedMessageList
import io.getstream.chat.android.compose.viewmodel.pinned.PinnedMessageListViewModel
import io.getstream.chat.android.models.Message

@Composable
fun PinnedMessagesScreen(
    viewModel: PinnedMessageListViewModel,
    onNavigationIconClick: () -> Unit,
    onMessageClick: (message: Message) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppToolbar(
                title = stringResource(id = R.string.pinned_messages_title),
                onBack = onNavigationIconClick,
            )
        },
        content = { padding ->
            PinnedMessageList(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                viewModel = viewModel,
                onPinnedMessageClick = onMessageClick,
            )
        },
    )
}
