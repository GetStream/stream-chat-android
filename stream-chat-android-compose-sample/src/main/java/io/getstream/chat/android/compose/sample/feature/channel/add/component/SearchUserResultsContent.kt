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

package io.getstream.chat.android.compose.sample.feature.channel.add.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.handlers.LoadMoreHandler
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.add.SearchUsersViewModel
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.models.User

/**
 * Content for the 'Search users' screen in the "Add channel" / "Add group channel" flows.
 *
 * @param padding The padding values for the content.
 * @param state The state of the select users screen.
 * @param onUserClick Action to be invoked when a user is clicked.
 * @param onEndReached Action to be invoked when the end of the list is reached.
 */
@Composable
fun SearchUserResultsContent(
    padding: PaddingValues,
    state: SearchUsersViewModel.SearchUsersState,
    onUserClick: (User) -> Unit,
    onEndReached: () -> Unit,
) {
    when {
        state.users.isEmpty() && state.isLoading -> {
            SearchUserLoadingContent(padding)
        }

        state.users.isEmpty() && !state.isLoading -> {
            SearchUserEmptyContent(padding)
        }

        else -> {
            SearchUserResultList(
                padding = padding,
                users = state.users,
                selectedUsers = state.selectedUsers,
                isLoadingMore = state.isLoading,
                onUserClick = onUserClick,
                onEndReached = onEndReached,
            )
        }
    }
}

/**
 * Composable component representing the loading state when users are being searched.
 *
 * @param padding The padding values to be applied.
 */
@Composable
private fun SearchUserLoadingContent(padding: PaddingValues) {
    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp, color = ChatTheme.colors.primaryAccent)
    }
}

/**
 * Composable component representing the empty state when no users are found.
 *
 * @param padding The padding values to be applied.
 */
@Composable
private fun SearchUserEmptyContent(padding: PaddingValues) {
    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_user_search),
            contentDescription = null,
        )
        Text(
            text = stringResource(id = R.string.add_channel_no_matches),
            fontSize = 14.sp,
            color = ChatTheme.colors.textLowEmphasis,
        )
    }
}

/**
 * Composable component representing users search results.
 *
 * @param padding The padding values to be applied.
 * @param users The grouped users to be shown.
 * @param selectedUsers The list of selected users.
 * @param isLoadingMore Indicator if more users are being loaded.
 * @param onUserClick Action to be invoked when a user is clicked.
 * @param onEndReached Action to be invoked when the end of the list is reached.
 */
@Suppress("LongParameterList")
@Composable
private fun SearchUserResultList(
    padding: PaddingValues,
    users: Map<Char, List<User>>,
    selectedUsers: List<User>,
    isLoadingMore: Boolean,
    onUserClick: (User) -> Unit,
    onEndReached: () -> Unit,
) {
    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(ChatTheme.colors.appBackground),
        state = listState,
    ) {
        users.forEach { (group, users) ->
            item {
                GroupingItem(group)
            }
            items(users) { user ->
                SearchUserResultItem(
                    user = user,
                    isSelected = selectedUsers.contains(user),
                    onUserClick = onUserClick,
                )
                HorizontalDivider(color = ChatTheme.colors.borders, thickness = 1.dp)
            }
        }
        if (isLoadingMore) {
            item {
                LoadingMoreItem()
            }
        }
    }
    LoadMoreHandler(
        lazyListState = listState,
        loadMore = onEndReached,
    )
}

/**
 * Composable component representing a group header in the search results.
 *
 * @param group The group character to be shown.
 */
@Composable
private fun GroupingItem(group: Char) {
    Text(
        text = if (group == SearchUsersViewModel.EMPTY_NAME_SYMBOL) {
            stringResource(id = R.string.add_channel_others)
        } else {
            group.toString()
        },
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = ChatTheme.colors.textLowEmphasis,
        modifier = Modifier.padding(8.dp),
    )
}

/**
 * Composable component representing a single user in the search results.
 *
 * @param user The [User] data to be shown.
 * @param isSelected Indicator whether the user is selected.
 * @param onUserClick Action to be invoked when the user is clicked.
 */
@Composable
private fun SearchUserResultItem(
    user: User,
    isSelected: Boolean,
    onUserClick: (User) -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(ChatTheme.colors.appBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = { onUserClick(user) },
            )
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            modifier = Modifier.size(AvatarSize.Large),
            user = user,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = user.name,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ChatTheme.colors.textHighEmphasis,
            )
            Text(
                text = user.getLastSeenText(context),
                fontSize = 12.sp,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }
        if (isSelected) {
            Icon(
                tint = ChatTheme.colors.primaryAccent,
                painter = painterResource(id = R.drawable.ic_check_filled),
                contentDescription = null,
            )
        }
    }
}

/**
 * Composable component representing the 'loading more' indicator for the user results list.
 */
@Composable
private fun LoadingMoreItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp, color = ChatTheme.colors.primaryAccent)
    }
}
