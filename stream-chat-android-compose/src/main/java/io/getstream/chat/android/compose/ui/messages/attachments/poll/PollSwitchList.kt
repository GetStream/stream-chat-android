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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamPrimitiveColors
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * Displays the poll feature toggles (multiple votes, anonymous poll, suggest an option, allow comments).
 *
 * @param items The list of [PollSwitchItem]s to render. Only include items that should be visible.
 */
@Composable
internal fun PollSwitchList(items: List<PollSwitchItem>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingMd),
    ) {
        items.forEach { item ->
            when (item) {
                is PollSwitchItem.MultipleVotes -> MultipleVotesItem(item)
                is PollSwitchItem.AnonymousPoll -> SimpleSwitchItem(
                    title = stringResource(R.string.stream_compose_poll_option_switch_anonymous_poll),
                    description = stringResource(R.string.stream_compose_poll_option_switch_anonymous_poll_description),
                    item = item,
                )

                is PollSwitchItem.SuggestAnOption -> SimpleSwitchItem(
                    title = stringResource(R.string.stream_compose_poll_option_switch_suggest_option),
                    description = stringResource(R.string.stream_compose_poll_option_switch_suggest_option_description),
                    item = item,
                )

                is PollSwitchItem.AllowComments -> SimpleSwitchItem(
                    title = stringResource(R.string.stream_compose_poll_option_switch_add_comment),
                    description = stringResource(R.string.stream_compose_poll_option_switch_add_comment_description),
                    item = item,
                )
            }
        }
    }
}

@Composable
private fun MultipleVotesItem(item: PollSwitchItem.MultipleVotes) {
    PollSwitchListItem(
        title = stringResource(R.string.stream_compose_poll_option_switch_multiple_answers),
        description = stringResource(R.string.stream_compose_poll_option_switch_multiple_answers_description),
        enabled = item.enabled,
        onCheckedChange = item.onCheckedChange,
    ) {
        MaxVotesInput(
            maxVotesPerUser = item.maxVotesPerUser,
            onMaxVotesCommit = item.onMaxVotesCommit,
        )
    }
}

@Composable
private fun SimpleSwitchItem(
    title: String,
    description: String,
    item: PollSwitchItem,
) {
    PollSwitchListItem(
        title = title,
        description = description,
        enabled = item.enabled,
        onCheckedChange = item.onCheckedChange,
    )
}

@Composable
private fun PollSwitchListItem(
    title: String,
    description: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    childContent: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .border(
                width = 1.dp,
                color = ChatTheme.colors.backgroundCoreSurface,
                shape = RoundedCornerShape(StreamTokens.radiusXl),
            )
            .clip(shape = RoundedCornerShape(StreamTokens.radiusXl))
            .background(ChatTheme.colors.backgroundCoreSurface)
            .padding(StreamTokens.spacingMd),
    ) {
        PollSwitchHeader(
            title = title,
            description = description,
            enabled = enabled,
            onCheckedChange = onCheckedChange,
        )

        if (enabled && childContent != null) {
            childContent()
        }
    }
}

@Composable
private fun PollSwitchHeader(
    title: String,
    description: String,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
        ) {
            Text(
                text = title,
                style = ChatTheme.typography.headingSmall,
                color = ChatTheme.colors.textPrimary,
            )
            Text(
                text = description,
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textTertiary,
            )
        }

        PollSwitch(enabled = enabled, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MaxVotesInput(
    maxVotesPerUser: Int,
    onMaxVotesCommit: (String) -> Unit,
) {
    val colors = ChatTheme.colors
    var text by remember(maxVotesPerUser) { mutableStateOf(maxVotesPerUser.toString()) }
    BasicTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = StreamTokens.spacingXs)
            .border(
                width = 1.dp,
                color = colors.borderCoreDefault,
                shape = RoundedCornerShape(StreamTokens.radiusMd),
            )
            .padding(StreamTokens.spacingMd)
            .onFocusChanged { focusState ->
                if (!focusState.isFocused) {
                    onMaxVotesCommit(text)
                }
            },
        textStyle = ChatTheme.typography.bodyDefault.copy(color = colors.textPrimary),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        cursorBrush = SolidColor(colors.accentPrimary),
    )
}

@Composable
private fun PollSwitch(enabled: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Switch(
        colors = SwitchDefaults.colors(
            checkedTrackColor = ChatTheme.colors.accentPrimary,
            checkedBorderColor = ChatTheme.colors.accentPrimary,
            uncheckedTrackColor = ChatTheme.colors.textSecondary,
            uncheckedBorderColor = ChatTheme.colors.textSecondary,
        ),
        thumbContent = {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        color = if (enabled) {
                            StreamPrimitiveColors.baseWhite
                        } else {
                            ChatTheme.colors.backgroundCoreDisabled
                        },
                    ),
            )
        },
        checked = enabled,
        onCheckedChange = onCheckedChange,
    )
}

@Preview
@Composable
private fun PollSwitchListPreview() {
    ChatTheme {
        PollSwitchList()
    }
}

@Composable
internal fun PollSwitchList() {
    PollSwitchList(
        items = listOf(
            PollSwitchItem.MultipleVotes(
                enabled = true,
                onCheckedChange = {},
                maxVotesPerUser = 5,
                onMaxVotesCommit = {},
            ),
            PollSwitchItem.AnonymousPoll(enabled = false, onCheckedChange = {}),
            PollSwitchItem.SuggestAnOption(enabled = false, onCheckedChange = {}),
            PollSwitchItem.AllowComments(enabled = false, onCheckedChange = {}),
        ),
    )
}
