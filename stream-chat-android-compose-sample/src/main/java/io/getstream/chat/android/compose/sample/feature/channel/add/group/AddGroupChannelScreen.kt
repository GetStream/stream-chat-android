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

package io.getstream.chat.android.compose.sample.feature.channel.add.group

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.add.SearchUsersViewModel
import io.getstream.chat.android.compose.sample.feature.channel.add.component.SearchUserResultsContent
import io.getstream.chat.android.compose.sample.feature.channel.add.component.SearchUserTextField
import io.getstream.chat.android.compose.sample.ui.component.AppToolbar
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User

/**
 * Composable component rendering the "Add group channel" screen.
 *
 * @param state The state of the screen.
 * @param onSearchQueryChanged Action to be invoked when the search query is changed.
 * @param onUserClick Action to be invoked when a user is clicked.
 * @param onEndReached Action to be invoked when the end of the list is reached.
 * @param onNext Action to be invoked when the next button is clicked.
 * @param onChannelNameChanged Action to be invoked when the channel name is changed.
 * @param onCreateChannelClick Action to be invoked when the create channel button is clicked.
 * @param onBack Action to be invoked when the back button is clicked.
 */
@Suppress("LongParameterList")
@Composable
fun AddGroupChannelScreen(
    state: AddGroupChannelViewModel.AddGroupChannelState,
    onSearchQueryChanged: (String) -> Unit,
    onUserClick: (User) -> Unit,
    onEndReached: () -> Unit,
    onNext: () -> Unit,
    onChannelNameChanged: (String) -> Unit,
    onCreateChannelClick: () -> Unit,
    onBack: () -> Unit,
) {
    BackHandler { onBack() }
    Scaffold(
        modifier = Modifier
            .safeDrawingPadding(),
        topBar = {
            when (state.step) {
                AddGroupChannelViewModel.AddGroupChannelStep.SELECT_USERS -> {
                    SelectUsersToolbar(
                        state = state.searchUsersState,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onUserClick = onUserClick,
                        onBack = onBack,
                    )
                }

                AddGroupChannelViewModel.AddGroupChannelStep.ENTER_NAME -> {
                    EnterNameToolbar(
                        channelName = state.channelName,
                        onChannelNameChanged = onChannelNameChanged,
                        onBack = onBack,
                    )
                }
            }
        },
        floatingActionButton = {
            when (state.step) {
                AddGroupChannelViewModel.AddGroupChannelStep.SELECT_USERS -> {
                    if (state.searchUsersState.selectedUsers.isNotEmpty()) {
                        NextFab(onClick = onNext)
                    }
                }

                AddGroupChannelViewModel.AddGroupChannelStep.ENTER_NAME -> {
                    if (state.searchUsersState.selectedUsers.isNotEmpty() && state.channelName.isNotEmpty()) {
                        AddGroupChannelFab(onClick = onCreateChannelClick)
                    }
                }
            }
        },
    ) { padding ->
        when (state.step) {
            AddGroupChannelViewModel.AddGroupChannelStep.SELECT_USERS -> {
                SearchUserResultsContent(padding, state.searchUsersState, onUserClick, onEndReached)
            }

            AddGroupChannelViewModel.AddGroupChannelStep.ENTER_NAME -> {
                EnterNameContent(padding, state.searchUsersState.selectedUsers, onUserClick)
            }
        }
    }
}

/**
 * Toolbar for the 'Select users' step.
 *
 * @param state The state of the search users screen.
 * @param onSearchQueryChanged Action to be invoked when the search query is changed.
 * @param onUserClick Action to be invoked when a user is clicked.
 * @param onBack Action to be invoked when the back button is clicked.
 */
@Composable
private fun SelectUsersToolbar(
    state: SearchUsersViewModel.SearchUsersState,
    onSearchQueryChanged: (String) -> Unit,
    onUserClick: (User) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundElevationElevation1),
    ) {
        AppToolbar(
            title = stringResource(id = R.string.add_group_channel_members_title),
            onBack = onBack,
        )
        SearchUserTextField(
            query = state.query,
            onQueryChanged = onSearchQueryChanged,
            leadingContent = {
                Icon(
                    tint = ChatTheme.colors.textSecondary,
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = null,
                )
            },
        )
        if (state.selectedUsers.isNotEmpty()) {
            SelectedUsersList(
                selectedUsers = state.selectedUsers,
                onRemoveClick = onUserClick,
            )
        }
        HorizontalDivider(color = ChatTheme.colors.borderCoreDefault, thickness = 1.dp)
    }
}

/**
 * Toolbar for the 'Enter name' step.
 *
 * @param channelName The currently entered name of the channel.
 * @param onChannelNameChanged Action to be invoked when the channel name is changed.
 * @param onBack Action to be invoked when the back button is clicked.
 */
@Composable
private fun EnterNameToolbar(
    channelName: String,
    onChannelNameChanged: (String) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundElevationElevation1),
    ) {
        AppToolbar(
            title = stringResource(id = R.string.add_group_channel_name_title),
            onBack = onBack,
        )
        ChannelNameTextField(
            channelName = channelName,
            onChannelNameChanged = onChannelNameChanged,
        )
        HorizontalDivider(color = ChatTheme.colors.borderCoreDefault, thickness = 1.dp)
    }
}

/**
 * Content for the 'Enter name' step.
 *
 * @param selectedUsers The list of selected users.
 * @param onRemoveUserClick Action to be invoked when the (X) button in an user item is clicked.
 */
@Composable
private fun EnterNameContent(
    padding: PaddingValues,
    selectedUsers: List<User>,
    onRemoveUserClick: (User) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.backgroundCoreApp)
            .padding(padding),
    ) {
        val text = if (selectedUsers.isEmpty()) {
            stringResource(id = R.string.add_group_channel_members_empty)
        } else {
            val membersCount = selectedUsers.size
            pluralStringResource(id = R.plurals.add_group_channel_members_count, membersCount, membersCount)
        }
        Text(
            modifier = Modifier.padding(8.dp),
            text = text,
            color = ChatTheme.colors.textSecondary,
            fontSize = 14.sp,
        )
        LazyColumn {
            items(selectedUsers) { user ->
                GroupChannelUserPreviewItem(
                    user = user,
                    onRemoveClick = { onRemoveUserClick(user) },
                )
                HorizontalDivider(color = ChatTheme.colors.borderCoreDefault, thickness = 1.dp)
            }
        }
    }
}

/**
 * Next button for the 'select users' step.
 *
 * @param onClick Action to be invoked when the next button is clicked.
 */
@Composable
private fun NextFab(onClick: () -> Unit) {
    FloatingActionButton(
        shape = CircleShape,
        containerColor = ChatTheme.colors.accentPrimary,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_arrow_right),
            contentDescription = null,
            tint = ChatTheme.colors.backgroundCoreApp,
        )
    }
}

/**
 * Create channel button.
 *
 * @param onClick Action to be invoked when the create channel button is clicked.
 */
@Composable
private fun AddGroupChannelFab(onClick: () -> Unit) {
    FloatingActionButton(
        shape = CircleShape,
        containerColor = ChatTheme.colors.accentPrimary,
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_check),
            contentDescription = null,
            tint = ChatTheme.colors.backgroundCoreApp,
        )
    }
}

/**
 * Text field for the channel name.
 *
 * @param channelName The current channel name.
 * @param onChannelNameChanged Action to be invoked when the channel name is changed.
 */
@Composable
private fun ChannelNameTextField(
    channelName: String,
    onChannelNameChanged: (String) -> Unit,
) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = channelName,
        singleLine = true,
        maxLines = 1,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = ChatTheme.colors.backgroundElevationElevation1,
            unfocusedContainerColor = ChatTheme.colors.backgroundElevationElevation1,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = ChatTheme.colors.textPrimary,
            unfocusedTextColor = ChatTheme.colors.textPrimary,
            cursorColor = ChatTheme.colors.accentPrimary,
        ),
        onValueChange = onChannelNameChanged,
        placeholder = {
            Text(
                text = stringResource(id = R.string.add_group_channel_name_placeholder),
                fontSize = 14.sp,
                color = ChatTheme.colors.textSecondary,
            )
        },
        leadingIcon = {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(id = R.string.add_group_channel_name_label),
                fontSize = 14.sp,
                color = ChatTheme.colors.textSecondary,
            )
        },
    )
}

/**
 * Item for the members preview in the 'Enter name' step.
 *
 * @param user The user to be displayed.
 * @param onRemoveClick Action to be invoked when the remove button is clicked.
 */
@Composable
private fun GroupChannelUserPreviewItem(
    user: User,
    onRemoveClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        UserAvatar(
            modifier = Modifier.size(AvatarSize.Large),
            user = user,
        )
        Text(
            modifier = Modifier
                .padding(start = 8.dp)
                .weight(1f),
            text = user.name,
            color = ChatTheme.colors.textPrimary,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        IconButton(
            onClick = onRemoveClick,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = null,
                tint = ChatTheme.colors.textPrimary,
            )
        }
    }
}

/**
 * List of selected users in the 'Select users' toolbar.
 *
 * @param selectedUsers The list of selected users.
 * @param onRemoveClick Action to be invoked when the remove button is clicked.
 */
@Composable
private fun SelectedUsersList(
    selectedUsers: List<User>,
    onRemoveClick: (User) -> Unit,
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.backgroundElevationElevation1),
        contentPadding = PaddingValues(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(selectedUsers) { user ->
            SelectedUserItem(
                user = user,
                onRemoveClick = { onRemoveClick(user) },
            )
        }
    }
}

/**
 * Item for the selected user in the 'Select users' toolbar.
 *
 * @param user The user to be displayed.
 * @param onRemoveClick Action to be invoked when the remove button is clicked.
 */
@Composable
private fun SelectedUserItem(
    user: User,
    onRemoveClick: () -> Unit,
) {
    Box {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            UserAvatar(
                modifier = Modifier.size(60.dp),
                user = user,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                modifier = Modifier.width(60.dp),
                text = user.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textPrimary,
            )
        }
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(ChatTheme.colors.backgroundElevationElevation1)
                .align(Alignment.TopEnd)
                .clickable { onRemoveClick() },
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = null,
                tint = ChatTheme.colors.textPrimary,
            )
        }
    }
}
