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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.components.button.StreamButtonStyleDefaults
import io.getstream.chat.android.compose.ui.theme.ChannelAvatarParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.bottomBorder
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.previewdata.PreviewChannelData

@Composable
internal fun GroupChannelEditScreen(
    channel: Channel,
    onNavigationIconClick: () -> Unit = {},
    onSaveChangesClick: (channelName: String) -> Unit = {},
) {
    var channelName by remember {
        val initialValue = channel.name
        mutableStateOf(TextFieldValue(text = initialValue, selection = TextRange(initialValue.length)))
    }
    Scaffold(
        topBar = {
            GroupChannelEditTopBar(
                onNavigationIconClick = onNavigationIconClick,
                onSaveActionClick = { onSaveChangesClick(channelName.text) },
            )
        },
        containerColor = ChatTheme.colors.backgroundCoreApp,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacing2xl),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xl, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ChatTheme.componentFactory.ChannelAvatar(
                params = ChannelAvatarParams(
                    modifier = Modifier.size(AvatarSize.ExtraExtraLarge),
                    channel = channel,
                ),
            )
            ChannelNameField(
                value = channelName,
                onValueChange = { channelName = it },
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GroupChannelEditTopBar(
    onNavigationIconClick: () -> Unit,
    onSaveActionClick: () -> Unit,
) {
    CenterAlignedTopAppBar(
        modifier = Modifier.bottomBorder(color = ChatTheme.colors.borderCoreSubtle),
        title = {
            Text(
                text = stringResource(R.string.stream_ui_channel_info_edit_title),
                style = ChatTheme.typography.headingMedium,
                maxLines = 1,
            )
        },
        navigationIcon = { ChannelInfoNavigationIcon(onClick = onNavigationIconClick) },
        actions = {
            StreamButton(
                style = StreamButtonStyleDefaults.primarySolid,
                onClick = onSaveActionClick,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
                    contentDescription = stringResource(id = R.string.stream_ui_channel_info_edit_save_action),
                )
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = ChatTheme.colors.backgroundCoreApp,
            scrolledContainerColor = ChatTheme.colors.backgroundCoreApp,
            titleContentColor = ChatTheme.colors.textPrimary,
            navigationIconContentColor = ChatTheme.colors.textPrimary,
            actionIconContentColor = ChatTheme.colors.textPrimary,
        ),
    )
}

@Composable
private fun ChannelNameField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    OutlinedTextField(
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxWidth(),
        textStyle = ChatTheme.typography.bodyDefault,
        placeholder = {
            Text(
                text = stringResource(R.string.stream_ui_channel_info_edit_name_field_placeholder),
                style = ChatTheme.typography.bodyDefault,
            )
        },
        value = value,
        singleLine = true,
        onValueChange = onValueChange,
        shape = RoundedCornerShape(StreamTokens.radiusLg),
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = ChatTheme.colors.systemCaret,
            focusedBorderColor = ChatTheme.colors.borderUtilityActive,
            focusedTextColor = ChatTheme.colors.inputTextDefault,
            unfocusedBorderColor = ChatTheme.colors.borderCoreDefault,
            unfocusedTextColor = ChatTheme.colors.inputTextDefault,
        ),
    )
}

@Preview
@Composable
private fun GroupChannelEditPlaceholderPreview() {
    ChatTheme {
        GroupChannelEditPlaceholder()
    }
}

@Composable
internal fun GroupChannelEditPlaceholder() {
    GroupChannelEditScreen(
        channel = PreviewChannelData.channelWithImage,
    )
}

@Preview
@Composable
private fun GroupChannelEditFilledPreview() {
    ChatTheme {
        GroupChannelEditFilled()
    }
}

@Composable
internal fun GroupChannelEditFilled() {
    GroupChannelEditScreen(
        channel = PreviewChannelData.channelWithImage.copy(name = "Channel Name"),
    )
}
