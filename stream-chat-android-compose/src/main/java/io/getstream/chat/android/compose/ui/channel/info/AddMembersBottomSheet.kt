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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.LoadingIndicator
import io.getstream.chat.android.compose.ui.components.SearchInput
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.components.common.RadioCheck
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.viewmodel.channel.AddMembersViewModel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.channel.info.AddMembersViewAction
import io.getstream.chat.android.ui.common.feature.channel.info.AddMembersViewEvent
import kotlinx.coroutines.flow.collectLatest

/**
 * A bottom sheet that allows users to search for and add members to a channel.
 *
 * @param viewModel The [AddMembersViewModel] managing the state and actions.
 * @param onDismiss Callback invoked when the bottom sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddMembersBottomSheet(
    viewModel: AddMembersViewModel,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                AddMembersViewEvent.MembersAdded -> onDismiss()
            }
        }
    }

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        sheetState = sheetState,
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
        onDismissRequest = onDismiss,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AddMembersHeader(
                hasSelection = state.selectedUsers.isNotEmpty(),
                onDismiss = onDismiss,
                onConfirm = { viewModel.onViewAction(AddMembersViewAction.ConfirmClick) },
            )
            SearchInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = StreamTokens.spacingMd),
                query = state.query,
                onValueChange = { viewModel.onViewAction(AddMembersViewAction.QueryChanged(it)) },
            )
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingIndicator(modifier = Modifier.size(172.dp))
                    }
                }
                state.searchResult.isEmpty() -> {
                    AddMembersEmptyState()
                }
                else -> {
                    AddMembersUserList(
                        users = state.searchResult,
                        selectedUsers = state.selectedUsers,
                        onUserClick = { user -> viewModel.onViewAction(AddMembersViewAction.UserClick(user)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AddMembersHeader(
    hasSelection: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(StreamTokens.spacingSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
    ) {
        StreamButton(
            onClick = onDismiss,
            style = StreamButtonStyleDefaults.secondaryOutline,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.stream_compose_ic_close),
                contentDescription = stringResource(id = R.string.stream_compose_cancel),
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.stream_compose_add_members_title),
            style = ChatTheme.typography.headingSmall,
            color = ChatTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        StreamButton(
            onClick = onConfirm,
            style = StreamButtonStyleDefaults.primarySolid,
            enabled = hasSelection,
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
                contentDescription = stringResource(id = R.string.stream_compose_add_members_title),
            )
        }
    }
}

@Composable
private fun AddMembersUserList(
    users: List<User>,
    selectedUsers: List<User>,
    onUserClick: (User) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            top = StreamTokens.spacingSm,
            bottom = StreamTokens.spacing3xl,
        ),
    ) {
        items(
            items = users,
            key = User::id,
        ) { user ->
            AddMembersUserItem(
                user = user,
                isSelected = selectedUsers.contains(user),
                onClick = { onUserClick(user) },
            )
        }
    }
}

@Composable
private fun AddMembersUserItem(
    user: User,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = StreamTokens.spacing2xs)
            .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingSm),
    ) {
        UserAvatar(
            modifier = Modifier.size(AvatarSize.Medium),
            user = user,
            showIndicator = false,
            showBorder = false,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = user.name.takeIf(String::isNotBlank) ?: user.id,
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        RadioCheck(
            checked = isSelected,
            onCheckedChange = null,
        )
    }
}

@Composable
private fun AddMembersEmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = StreamTokens.spacing3xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            painter = painterResource(id = R.drawable.stream_compose_ic_search),
            tint = ChatTheme.colors.textTertiary,
            contentDescription = null,
        )
        Text(
            text = stringResource(id = R.string.stream_compose_add_members_no_results),
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}
