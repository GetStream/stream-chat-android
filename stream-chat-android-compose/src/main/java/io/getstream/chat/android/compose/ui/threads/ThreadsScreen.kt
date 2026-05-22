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

package io.getstream.chat.android.compose.ui.threads

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.api.models.QueryThreadsRequest
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ThreadListHeaderParams
import io.getstream.chat.android.compose.viewmodel.threads.ThreadListViewModel
import io.getstream.chat.android.compose.viewmodel.threads.ThreadsViewModelFactory
import io.getstream.chat.android.models.Thread

/**
 * Default root Threads screen component, that provides the necessary ViewModel.
 *
 * It can be used without most parameters for default behavior, that can be tweaked if necessary.
 *
 * @param viewModelFactory The factory used to build the [ThreadListViewModel].
 * @param title Header title. Also drives the screen's `paneTitle` semantic, announced by TalkBack
 * when the screen appears as a pane (e.g. an adaptive-layout pane or a Compose Navigation route).
 * @param onHeaderAvatarClick Handle for when the user clicks on the header avatar.
 * @param onThreadClick Handler for Thread item clicks.
 */
@Composable
public fun ThreadsScreen(
    viewModelFactory: ThreadsViewModelFactory = ThreadsViewModelFactory(query = QueryThreadsRequest()),
    title: String = stringResource(R.string.stream_compose_thread_list_header_title),
    onHeaderAvatarClick: () -> Unit = {},
    onThreadClick: (Thread) -> Unit = {},
) {
    val listViewModel: ThreadListViewModel = viewModel(factory = viewModelFactory)

    val user by listViewModel.user.collectAsState()
    val connectionState by listViewModel.connectionState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().semantics { paneTitle = title }) {
        ChatTheme.componentFactory.ThreadListHeader(
            params = ThreadListHeaderParams(
                title = title,
                currentUser = user,
                connectionState = connectionState,
                onAvatarClick = { onHeaderAvatarClick() },
            ),
        )

        ThreadList(
            viewModel = listViewModel,
            modifier = Modifier.fillMaxSize(),
            onThreadClick = onThreadClick,
        )
    }
}
