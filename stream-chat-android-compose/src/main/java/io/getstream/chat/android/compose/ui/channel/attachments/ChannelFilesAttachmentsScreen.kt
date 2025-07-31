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

package io.getstream.chat.android.compose.ui.channel.attachments

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentDescription
import io.getstream.chat.android.compose.ui.attachments.content.FileAttachmentImage
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.viewmodel.channel.ChannelAttachmentsViewModel
import io.getstream.chat.android.compose.viewmodel.channel.ChannelAttachmentsViewModelFactory
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.feature.channel.attachments.ChannelAttachmentsViewAction
import io.getstream.chat.android.ui.common.state.channel.attachments.ChannelAttachmentsViewState

/**
 * Displays the channel files attachments screen.
 *
 * @param viewModelFactory The factory to create the [ChannelAttachmentsViewModel].
 * @param modifier The modifier for styling.
 * @param currentUser The currently logged in user.
 * @param groupKeySelector The function to select the group key for each item and group them in the list.
 * @param stickHeader Whether the header should stick to the top of the list when scrolling.
 * @param onNavigationIconClick The callback to be invoked when the navigation icon is clicked.
 */
@Composable
public fun ChannelFilesAttachmentsScreen(
    viewModelFactory: ChannelAttachmentsViewModelFactory,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    groupKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String =
        ChannelAttachmentsDefaults.GroupKeySelector,
    stickHeader: Boolean = true,
    onNavigationIconClick: () -> Unit = {},
) {
    val viewModel = viewModel<ChannelAttachmentsViewModel>(factory = viewModelFactory)
    val viewState by viewModel.state.collectAsStateWithLifecycle()

    ChannelFilesAttachmentsContent(
        modifier = modifier,
        viewState = viewState,
        currentUser = currentUser,
        groupKeySelector = groupKeySelector,
        stickHeader = stickHeader,
        onNavigationIconClick = onNavigationIconClick,
        onViewAction = viewModel::onViewAction,
    )
}

@Composable
private fun ChannelFilesAttachmentsContent(
    viewState: ChannelAttachmentsViewState,
    modifier: Modifier = Modifier,
    currentUser: User? = ChatClient.instance().getCurrentUser(),
    groupKeySelector: (item: ChannelAttachmentsViewState.Content.Item) -> String =
        ChannelAttachmentsDefaults.GroupKeySelector,
    stickHeader: Boolean = true,
    onNavigationIconClick: () -> Unit = {},
    onViewAction: (action: ChannelAttachmentsViewAction) -> Unit = {},
) {
    val listState = rememberLazyListState()
    Scaffold(
        modifier = modifier,
        topBar = {
            ChatTheme.componentFactory.ChannelFilesAttachmentsTopBar(
                listState = listState,
                onNavigationIconClick = onNavigationIconClick,
            )
        },
        containerColor = ChatTheme.colors.appBackground,
    ) { padding ->
        ChannelFilesAttachmentsList(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            viewState = viewState,
            listState = listState,
            currentUser = currentUser,
            groupKeySelector = groupKeySelector,
            stickHeader = stickHeader,
            onViewAction = onViewAction,
        )
    }
}

@Composable
internal fun LazyItemScope.ChannelFilesAttachmentsItem(
    modifier: Modifier,
    item: ChannelAttachmentsViewState.Content.Item,
    currentUser: User?,
    onClick: () -> Unit,
) {
    val isMine = currentUser?.id == item.message.user.id
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FileAttachmentImage(
            attachment = item.attachment,
            isMine = isMine,
        )
        FileAttachmentDescription(
            attachment = item.attachment,
            isMine = isMine,
            showFileSize = { true },
        )
    }
}

@Preview
@Composable
private fun ChannelFilesAttachmentsLoadingPreview() {
    ChatTheme {
        ChannelFilesAttachmentsLoading()
    }
}

@Composable
internal fun ChannelFilesAttachmentsLoading() {
    ChannelFilesAttachmentsContent(
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
        viewState = ChannelAttachmentsViewState.Loading,
    )
}

@Preview
@Composable
private fun ChannelFilesAttachmentsEmptyPreview() {
    ChatTheme {
        ChannelFilesAttachmentsEmpty()
    }
}

@Composable
internal fun ChannelFilesAttachmentsEmpty() {
    ChannelFilesAttachmentsContent(
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
        viewState = ChannelAttachmentsViewState.Content(
            items = emptyList(),
        ),
    )
}

@Preview
@Composable
private fun ChannelFilesAttachmentsContentPreview() {
    ChatTheme {
        ChannelFilesAttachmentsContent()
    }
}

@Composable
internal fun ChannelFilesAttachmentsContent() {
    ChannelFilesAttachmentsContent(
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
        viewState = ChannelAttachmentsViewState.Content(
            items = PreviewMessageData.messageWithUserAndAttachment.attachments.map { attachment ->
                ChannelAttachmentsViewState.Content.Item(
                    message = PreviewMessageData.message1,
                    attachment = attachment,
                )
            },
        ),
    )
}

@Preview
@Composable
private fun ChannelFilesAttachmentsErrorPreview() {
    ChatTheme {
        ChannelFilesAttachmentsError()
    }
}

@Composable
internal fun ChannelFilesAttachmentsError() {
    ChannelFilesAttachmentsContent(
        modifier = Modifier.fillMaxSize(),
        currentUser = PreviewUserData.user1,
        viewState = ChannelAttachmentsViewState.Error(
            message = "An error occurred while loading attachments.",
        ),
    )
}
