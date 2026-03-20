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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
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
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.channel.info.AddMembersViewAction
import io.getstream.chat.android.ui.common.state.channel.info.AddMembersViewState

/**
 * A bottom sheet that allows users to search for and add members to a channel.
 *
 * @param viewModel The [AddMembersViewModel] managing the state and actions.
 * @param onDismiss Callback invoked when the bottom sheet is dismissed without adding members.
 * @param onConfirm Callback invoked with the selected [User] list when the user confirms.
 *   The caller is responsible for performing the actual API call and dismissing the sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddMembersBottomSheet(
    viewModel: AddMembersViewModel,
    onDismiss: () -> Unit,
    onConfirm: (List<User>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val state by viewModel.state.collectAsStateWithLifecycle()

    ModalBottomSheet(
        modifier = Modifier.statusBarsPadding(),
        sheetState = sheetState,
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
        onDismissRequest = onDismiss,
    ) {
        AddMembersBottomSheet(
            state = state,
            onQueryChange = { viewModel.onViewAction(AddMembersViewAction.QueryChanged(it)) },
            onUserClick = { viewModel.onViewAction(AddMembersViewAction.UserClick(it)) },
            onLoadMore = { viewModel.onViewAction(AddMembersViewAction.LoadMore) },
            onDismiss = onDismiss,
            onConfirm = { onConfirm(state.selectedUsers) },
        )
    }
}

@Suppress("LongParameterList")
@Composable
internal fun AddMembersBottomSheet(
    state: AddMembersViewState,
    onQueryChange: (String) -> Unit,
    onUserClick: (User) -> Unit,
    onLoadMore: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AddMembersHeader(
            hasSelection = state.selectedUserIds.isNotEmpty(),
            onDismiss = onDismiss,
            onConfirm = onConfirm,
        )
        SearchInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = StreamTokens.spacingMd),
            query = state.query,
            onValueChange = onQueryChange,
        )
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator(modifier = Modifier.size(172.dp))
                }
            }

            state.searchResult.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    AddMembersEmptyState()
                }
            }

            else -> {
                AddMembersList(
                    users = state.searchResult,
                    isSelected = state::isSelected,
                    isAlreadyMember = state::isAlreadyMember,
                    isLoadingMore = state.isLoadingMore,
                    onUserClick = onUserClick,
                    onLoadMore = onLoadMore,
                )
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

@Suppress("LongParameterList")
@Composable
private fun AddMembersList(
    users: List<User>,
    isSelected: (User) -> Boolean,
    isAlreadyMember: (User) -> Boolean,
    isLoadingMore: Boolean,
    onUserClick: (User) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    LoadMoreHandler(
        lazyListState = listState,
        loadMore = onLoadMore,
    )

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            top = StreamTokens.spacingSm,
            bottom = StreamTokens.spacing3xl,
        ),
    ) {
        itemsIndexed(
            items = users,
            key = { _, user -> user.id },
        ) { _, user ->
            AddMembersUserItem(
                user = user,
                isSelected = isSelected(user),
                isAlreadyMember = isAlreadyMember(user),
                onClick = { if (!isAlreadyMember(user)) onUserClick(user) },
            )
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = StreamTokens.spacingMd),
                    contentAlignment = Alignment.Center,
                ) {
                    LoadingIndicator(modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

@Composable
private fun AddMembersUserItem(
    user: User,
    isSelected: Boolean,
    isAlreadyMember: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick, enabled = !isAlreadyMember)
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
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name.takeIf(String::isNotBlank) ?: user.id,
                style = ChatTheme.typography.bodyDefault,
                color = if (isAlreadyMember) ChatTheme.colors.textSecondary else ChatTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (isAlreadyMember) {
                Text(
                    text = stringResource(id = R.string.stream_compose_add_members_already_member),
                    style = ChatTheme.typography.metadataDefault,
                    color = ChatTheme.colors.textTertiary,
                    maxLines = 1,
                )
            }
        }
        if (!isAlreadyMember) {
            RadioCheck(
                checked = isSelected,
                onCheckedChange = null,
            )
        }
    }
}

@Composable
private fun AddMembersEmptyState() {
    Column(
        modifier = Modifier.fillMaxWidth(),
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

// ---- Previews ----

@Preview(showBackground = true)
@Composable
private fun AddMembersBottomSheetLoadingPreview() {
    ChatTheme {
        AddMembersBottomSheetLoading()
    }
}

@Composable
internal fun AddMembersBottomSheetLoading() {
    AddMembersBottomSheet(
        state = AddMembersViewState(isLoading = true),
        onQueryChange = {},
        onUserClick = {},
        onLoadMore = {},
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun AddMembersBottomSheetEmptyPreview() {
    ChatTheme {
        AddMembersBottomSheetEmpty()
    }
}

@Composable
internal fun AddMembersBottomSheetEmpty() {
    AddMembersBottomSheet(
        state = AddMembersViewState(
            isLoading = false,
            query = "Darth Vader",
            searchResult = emptyList(),
        ),
        onQueryChange = {},
        onUserClick = {},
        onLoadMore = {},
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun AddMembersBottomSheetResultsWithQueryPreview() {
    ChatTheme {
        AddMembersBottomSheetResultsWithQuery()
    }
}

@Composable
internal fun AddMembersBottomSheetResultsWithQuery() {
    AddMembersBottomSheet(
        state = AddMembersViewState(
            isLoading = false,
            query = "Han",
            searchResult = previewUsers,
        ),
        onQueryChange = {},
        onUserClick = {},
        onLoadMore = {},
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun AddMembersBottomSheetResultsPreview() {
    ChatTheme {
        AddMembersBottomSheetResults()
    }
}

@Composable
internal fun AddMembersBottomSheetResults() {
    AddMembersBottomSheet(
        state = AddMembersViewState(
            isLoading = false,
            searchResult = previewUsers,
        ),
        onQueryChange = {},
        onUserClick = {},
        onLoadMore = {},
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun AddMembersBottomSheetResultsWithSelectionPreview() {
    ChatTheme {
        AddMembersBottomSheetResultsWithSelection()
    }
}

@Composable
internal fun AddMembersBottomSheetResultsWithSelection() {
    AddMembersBottomSheet(
        state = AddMembersViewState(
            isLoading = false,
            searchResult = previewUsers,
            selectedUserIds = setOf(previewUsers[0].id, previewUsers[2].id),
        ),
        onQueryChange = {},
        onUserClick = {},
        onLoadMore = {},
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun AddMembersBottomSheetResultsWithMemberPreview() {
    ChatTheme {
        AddMembersBottomSheetResultsWithMember()
    }
}

@Composable
internal fun AddMembersBottomSheetResultsWithMember() {
    AddMembersBottomSheet(
        state = AddMembersViewState(
            isLoading = false,
            searchResult = previewUsers,
            loadedMemberIds = setOf(previewUsers[0].id, previewUsers[1].id),
        ),
        onQueryChange = {},
        onUserClick = {},
        onLoadMore = {},
        onDismiss = {},
        onConfirm = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun AddMembersBottomSheetLoadingMorePreview() {
    ChatTheme {
        AddMembersBottomSheetLoadingMore()
    }
}

@Composable
internal fun AddMembersBottomSheetLoadingMore() {
    AddMembersBottomSheet(
        state = AddMembersViewState(
            isLoading = false,
            searchResult = previewUsers,
            isLoadingMore = true,
        ),
        onQueryChange = {},
        onUserClick = {},
        onLoadMore = {},
        onDismiss = {},
        onConfirm = {},
    )
}

private val previewUsers = listOf(
    PreviewUserData.user2,
    PreviewUserData.user3,
    PreviewUserData.user4,
    PreviewUserData.user5,
    PreviewUserData.user6,
)
