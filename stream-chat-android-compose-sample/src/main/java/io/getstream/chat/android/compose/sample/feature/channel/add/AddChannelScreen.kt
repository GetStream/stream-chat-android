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

package io.getstream.chat.android.compose.sample.feature.channel.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.sample.feature.channel.add.component.SearchUserResultsContent
import io.getstream.chat.android.compose.sample.feature.channel.add.component.SearchUserTextField
import io.getstream.chat.android.compose.sample.ui.component.AppToolbar
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.messages.composer.MessageComposer
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory
import io.getstream.chat.android.models.User

/**
 * Composable component rendering the "Add channel" screen.
 *
 * @param state The render-able state of the screen.
 * @param onSearchQueryChanged Action to be invoked when the search query is changed.
 * @param onCreateGroupClick Action to be invoked when the "Create a group" section is clicked.
 * @param onUserClick Action to be invoked when a user is clicked.
 * @param onMessageSent Action to be invoked when a message is sent to the draft channel.
 * @param onEndReached Action to be invoked when the end of the list is reached.
 * @param onBack Action to be invoked when the back button is clicked.
 */
@Suppress("LongParameterList")
@Composable
fun AddChannelScreen(
    state: AddChannelViewModel.AddChannelState,
    onSearchQueryChanged: (String) -> Unit,
    onCreateGroupClick: () -> Unit,
    onUserClick: (User) -> Unit,
    onMessageSent: () -> Unit,
    onEndReached: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .safeDrawingPadding(),
        topBar = {
            AddChannelToolbar(
                state = state.searchUsersState,
                onSearchQueryChanged = onSearchQueryChanged,
                onCreateGroupClick = onCreateGroupClick,
                onUserClick = onUserClick,
                onBack = onBack,
            )
        },
        bottomBar = {
            state.draftCid?.let { cid ->
                val context = LocalContext.current
                val viewModel = remember(cid) {
                    val factory = MessagesViewModelFactory(context, cid)
                    factory.create(MessageComposerViewModel::class.java)
                }
                MessageComposer(
                    viewModel = viewModel,
                    leadingContent = {
                        Spacer(Modifier.width(8.dp))
                    },
                    onSendMessage = {
                        viewModel.sendMessage(it)
                        onMessageSent()
                    },
                )
            }
        },
    ) { padding ->
        SearchUserResultsContent(
            padding = padding,
            state = state.searchUsersState,
            onUserClick = onUserClick,
            onEndReached = onEndReached,
        )
    }
}

/**
 * Composable component rendering the toolbar for the "Add channel" screen.
 *
 * @param state The render-able state of the screen.
 * @param onSearchQueryChanged Action to be invoked when the search query is changed.
 * @param onCreateGroupClick Action to be invoked when the "Create a group" section is clicked.
 * @param onUserClick Action to be invoked when a user is clicked.
 * @param onBack Action to be invoked when the back button is clicked.
 */
@Composable
private fun AddChannelToolbar(
    state: SearchUsersViewModel.SearchUsersState,
    onSearchQueryChanged: (String) -> Unit,
    onCreateGroupClick: () -> Unit,
    onUserClick: (User) -> Unit,
    onBack: () -> Unit,
) {
    val hasSelectedUsers = state.selectedUsers.isNotEmpty()
    var searchFieldVisible by remember((state.selectedUsers)) {
        mutableStateOf(!hasSelectedUsers)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.barsBackground),
    ) {
        AppToolbar(title = stringResource(id = R.string.add_channel_title), onBack = onBack, elevation = 0.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.barsBackground)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // TO: prefix
            Text(
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(top = 20.dp),
                text = stringResource(id = R.string.add_channel_to),
                fontSize = 14.sp,
                color = ChatTheme.colors.textLowEmphasis,
            )
            Spacer(modifier = Modifier.size(8.dp))
            // Selected users + Search field
            Column(modifier = Modifier.weight(1f)) {
                if (hasSelectedUsers) {
                    SelectedUsersList(selectedUsers = state.selectedUsers, onUserClick = onUserClick)
                }
                if (searchFieldVisible) {
                    SearchUserTextField(query = state.query, onQueryChanged = onSearchQueryChanged)
                }
            }
            // Member icon
            IconButton(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(bottom = 8.dp),
                enabled = !searchFieldVisible,
                onClick = { searchFieldVisible = true },
            ) {
                val iconId = if (searchFieldVisible) R.drawable.ic_member else R.drawable.ic_member_add
                Icon(
                    painter = painterResource(iconId),
                    contentDescription = null,
                    tint = ChatTheme.colors.textLowEmphasis,
                )
            }
        }
        HorizontalDivider(color = ChatTheme.colors.borders, thickness = 1.dp)
        if (!hasSelectedUsers) {
            CreateGroupSection(onClick = onCreateGroupClick)
            HorizontalDivider(color = ChatTheme.colors.borders, thickness = 1.dp)
        }
    }
}

/**
 * Composable component rendering the "Create a group" section.
 *
 * @param onClick Action to be invoked when the section is clicked.
 */
@Composable
private fun CreateGroupSection(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.barsBackground)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            tint = ChatTheme.colors.primaryAccent,
            painter = painterResource(id = R.drawable.ic_create_group),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = stringResource(id = R.string.add_channel_create_group),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }
}

/**
 * Composable component rendering the list of selected users.
 *
 * @param selectedUsers The list of selected users.
 * @param onUserClick Action to be invoked when a user is clicked.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedUsersList(
    selectedUsers: List<User>,
    onUserClick: (User) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.barsBackground)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        selectedUsers.forEach { user ->
            SelectedUserChip(
                user = user,
                onClick = { onUserClick(user) },
            )
        }
    }
}

/**
 * Composable component rendering a selected user chip.
 *
 * @param user The selected user.
 * @param onClick Action to be invoked when the chip is clicked.
 */
@Composable
private fun SelectedUserChip(
    user: User,
    onClick: () -> Unit,
) {
    SuggestionChip(
        modifier = Modifier.height(32.dp),
        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = ChatTheme.colors.appBackground),
        shape = RoundedCornerShape(16.dp),
        icon = {
            UserAvatar(
                modifier = Modifier.size(AvatarSize.Small),
                user = user,
            )
        },
        border = null,
        onClick = onClick,
        label = {
            Text(
                text = user.name,
                fontSize = 12.sp,
                color = ChatTheme.colors.textHighEmphasis,
            )
        },
    )
}
