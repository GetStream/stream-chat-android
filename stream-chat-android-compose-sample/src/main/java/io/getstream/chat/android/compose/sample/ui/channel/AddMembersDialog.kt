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

package io.getstream.chat.android.compose.sample.ui.channel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.adaptivelayout.AdaptiveLayoutInfo
import io.getstream.chat.android.models.User
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

@Composable
fun AddMembersDialog(
    cid: String,
    onDismiss: () -> Unit,
) {
    val viewModelKey by remember { mutableStateOf(UUID.randomUUID().toString()) }

    @Suppress("MagicNumber")
    val resultLimit = if (AdaptiveLayoutInfo.singlePaneWindow()) 6 else 10
    val viewModel = viewModel(
        AddMembersViewModel::class,
        key = viewModelKey,
        factory = AddMembersViewModelFactory(cid, resultLimit),
    )
    val state by viewModel.state.collectAsStateWithLifecycle()
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            AddMembersContent(
                state = state,
                onViewAction = viewModel::onViewAction,
            )
        },
        confirmButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.accentPrimary),
                onClick = { viewModel.onViewAction(AddMembersViewAction.ConfirmClick) },
            ) {
                Text(text = stringResource(id = io.getstream.chat.android.compose.R.string.stream_compose_ok))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.textSecondary),
                onClick = onDismiss,
            ) {
                Text(text = stringResource(id = io.getstream.chat.android.compose.R.string.stream_compose_cancel))
            }
        },
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
    )
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AddMembersViewEvent.MembersAdded -> onDismiss()
            }
        }
    }
}

@Composable
private fun AddMembersContent(
    state: AddMembersViewState,
    onViewAction: (AddMembersViewAction) -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SearchInput(
            modifier = Modifier
                .focusRequester(focusRequester)
                .fillMaxWidth(),
            query = state.query,
            onValueChange = { onViewAction(AddMembersViewAction.QueryChanged(it)) },
        )
        Box(
            contentAlignment = Alignment.Center,
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(64.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(
                    items = state.searchResult,
                    key = User::id,
                ) { user ->
                    SearchResultItem(
                        user = user,
                        isSelected = state.selectedUsers.contains(user),
                        onViewAction = onViewAction,
                    )
                }
            }
            if (state.isLoading) {
                LoadingIndicator(
                    modifier = Modifier
                        .size(172.dp),
                )
            }
        }
        if (!state.isLoading && state.searchResult.isEmpty()) {
            SearchResultEmpty(
                modifier = Modifier,
            )
        }
    }
}

@Composable
private fun SearchResultItem(
    user: User,
    isSelected: Boolean,
    onViewAction: (AddMembersViewAction) -> Unit,
) {
    Box {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UserAvatar(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onViewAction(AddMembersViewAction.UserClick(user)) },
                user = user,
            )
            Text(
                text = user.name,
                style = ChatTheme.typography.bodyBold,
                maxLines = 2,
                textAlign = TextAlign.Center,
            )
        }
        AnimatedVisibility(
            visible = isSelected,
            enter = fadeIn() + expandIn(initialSize = { fullSize -> fullSize }),
            exit = shrinkOut(targetSize = { fullSize -> fullSize }) + fadeOut(),
        ) {
            Icon(
                modifier = Modifier.background(ChatTheme.colors.backgroundElevationElevation1, CircleShape),
                tint = ChatTheme.colors.accentPrimary,
                painter = painterResource(id = R.drawable.ic_check_filled),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun SearchResultEmpty(
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_user_search),
            contentDescription = null,
        )
        Text(
            text = stringResource(id = R.string.add_channel_no_matches),
            color = ChatTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddMembersLoadingPreview() {
    ChatTheme {
        AddMembersContent(
            state = AddMembersViewState(
                isLoading = true,
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddMembersContentPreview() {
    ChatTheme {
        AddMembersContent(
            state = AddMembersViewState(
                isLoading = false,
                query = "query",
                searchResult = listOf(user1, user2),
                selectedUsers = listOf(user2),
            ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddMembersEmptyPreview() {
    ChatTheme {
        AddMembersContent(
            state = AddMembersViewState(
                isLoading = false,
            ),
        )
    }
}

private val user1: User = User(
    id = "jc",
    name = "Jc Miñarro",
    image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128",
    online = true,
)
private val user2: User = User(
    id = "leia_organa",
    name = "Leia Organa",
    image = "https://vignette.wikia.nocookie.net/starwars/images/f/fc/Leia_Organa_TLJ.png",
)
