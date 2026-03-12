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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamPrimitiveColors
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.ui.common.utils.PollsConstants

/**
 * Displays the poll feature toggles (multiple votes, anonymous poll, suggest an option, allow comments).
 *
 * @param multipleVotes Toggle for allowing multiple votes.
 * @param limitVotesPerPerson Toggle for limiting votes per person (child of multiple votes).
 * @param maxVotesPerPersonText Current text value for the max votes stepper.
 * @param onMaxVotesChanged Called when the max votes stepper value changes.
 * @param onMaxVotesFocusLost Called when the max votes stepper loses focus.
 * @param anonymousPoll Toggle for anonymous poll.
 * @param suggestAnOption Toggle for suggesting an option.
 * @param allowComments Toggle for allowing comments.
 */
@Suppress("LongParameterList")
@Composable
internal fun PollSwitchList(
    multipleVotes: PollSwitchItem,
    limitVotesPerPerson: PollSwitchItem,
    maxVotesPerPersonText: String,
    onMaxVotesChanged: (String) -> Unit,
    onMaxVotesFocusLost: () -> Unit,
    anonymousPoll: PollSwitchItem,
    suggestAnOption: PollSwitchItem,
    allowComments: PollSwitchItem,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingMd),
    ) {
        if (multipleVotes.visible) {
            PollSwitchListItem(
                title = stringResource(R.string.stream_compose_poll_option_switch_multiple_answers),
                description = stringResource(R.string.stream_compose_poll_option_switch_multiple_answers_description),
                enabled = multipleVotes.enabled,
                onCheckedChange = multipleVotes.onCheckedChange,
                childContent = if (limitVotesPerPerson.visible) {
                    {
                        LimitVotesPerPerson(
                            enabled = limitVotesPerPerson.enabled,
                            onCheckedChange = limitVotesPerPerson.onCheckedChange,
                            maxVotesPerPersonText = maxVotesPerPersonText,
                            onMaxVotesChange = onMaxVotesChanged,
                            onMaxVotesFocusLost = onMaxVotesFocusLost,
                        )
                    }
                } else {
                    null
                },
            )
        }
        if (anonymousPoll.visible) {
            PollSwitchListItem(
                title = stringResource(R.string.stream_compose_poll_option_switch_anonymous_poll),
                description = stringResource(R.string.stream_compose_poll_option_switch_anonymous_poll_description),
                enabled = anonymousPoll.enabled,
                onCheckedChange = anonymousPoll.onCheckedChange,
            )
        }
        if (suggestAnOption.visible) {
            PollSwitchListItem(
                title = stringResource(R.string.stream_compose_poll_option_switch_suggest_option),
                description = stringResource(R.string.stream_compose_poll_option_switch_suggest_option_description),
                enabled = suggestAnOption.enabled,
                onCheckedChange = suggestAnOption.onCheckedChange,
            )
        }
        if (allowComments.visible) {
            PollSwitchListItem(
                title = stringResource(R.string.stream_compose_poll_option_switch_add_comment),
                description = stringResource(R.string.stream_compose_poll_option_switch_add_comment_description),
                enabled = allowComments.enabled,
                onCheckedChange = allowComments.onCheckedChange,
            )
        }
    }
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
private fun LimitVotesPerPerson(
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    maxVotesPerPersonText: String,
    onMaxVotesChange: (String) -> Unit,
    onMaxVotesFocusLost: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = StreamTokens.spacingMd),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
            ) {
                Text(
                    text = stringResource(R.string.stream_compose_poll_option_limit_votes),
                    style = ChatTheme.typography.headingSmall,
                    color = ChatTheme.colors.textPrimary,
                )
                Text(
                    text = stringResource(R.string.stream_compose_poll_option_limit_votes_description),
                    style = ChatTheme.typography.captionDefault,
                    color = ChatTheme.colors.textTertiary,
                )
            }

            PollSwitch(enabled = enabled, onCheckedChange = onCheckedChange)
        }

        if (enabled) {
            Stepper(
                modifier = Modifier.padding(top = StreamTokens.spacingXs),
                text = maxVotesPerPersonText,
                onTextChange = onMaxVotesChange,
                onFocusLost = onMaxVotesFocusLost,
                range = PollsConstants.MULTIPLE_ANSWERS_RANGE,
            )
        }
    }
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

@Composable
private fun Stepper(
    text: String,
    onTextChange: (String) -> Unit,
    onFocusLost: () -> Unit,
    range: IntRange,
    modifier: Modifier = Modifier,
) {
    val colors = ChatTheme.colors
    val currentValue = (text.toIntOrNull() ?: range.first).coerceIn(range)
    val inputContentDescription = stringResource(R.string.stream_compose_poll_option_limit_votes_input)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StepperButton(
            iconRes = R.drawable.stream_compose_ic_minus,
            contentDescription = stringResource(R.string.stream_compose_poll_option_limit_votes_decrease),
            enabled = currentValue > range.first,
            onClick = { onTextChange((currentValue - 1).coerceIn(range).toString()) },
        )
        BasicTextField(
            value = text,
            onValueChange = { newValue -> onTextChange(newValue.filter(Char::isDigit)) },
            modifier = Modifier
                .width(40.dp * LocalDensity.current.fontScale)
                .semantics { this.contentDescription = inputContentDescription }
                .onFocusChanged { focusState ->
                    if (!focusState.isFocused) {
                        onFocusLost()
                    }
                },
            textStyle = ChatTheme.typography.headingSmall.copy(
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            cursorBrush = SolidColor(colors.accentPrimary),
        )
        StepperButton(
            iconRes = R.drawable.stream_compose_ic_plus,
            contentDescription = stringResource(R.string.stream_compose_poll_option_limit_votes_increase),
            enabled = currentValue < range.last,
            onClick = { onTextChange((currentValue + 1).coerceIn(range).toString()) },
        )
    }
}

@Composable
private fun StepperButton(
    iconRes: Int,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(
                width = 1.dp,
                color = if (enabled) ChatTheme.colors.borderCoreDefault else ChatTheme.colors.borderUtilityDisabled,
                shape = CircleShape,
            )
            .clip(CircleShape)
            .applyIf(enabled) { clickable(onClick = onClick) },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = if (enabled) ChatTheme.colors.buttonSecondaryText else ChatTheme.colors.textDisabled,
        )
    }
}

@Preview
@Composable
private fun PollSwitchListPreview() {
    ChatTheme {
        PollSwitchListPreviewContent()
    }
}

@Composable
internal fun PollSwitchListPreviewContent() {
    Column(Modifier.verticalScroll(rememberScrollState())) {
        PollSwitchList(
            multipleVotes = PollSwitchItem(visible = true, enabled = true, onCheckedChange = {}),
            limitVotesPerPerson = PollSwitchItem(visible = true, enabled = true, onCheckedChange = {}),
            maxVotesPerPersonText = "5",
            onMaxVotesChanged = {},
            onMaxVotesFocusLost = {},
            anonymousPoll = PollSwitchItem(visible = true, enabled = false, onCheckedChange = {}),
            suggestAnOption = PollSwitchItem(visible = true, enabled = false, onCheckedChange = {}),
            allowComments = PollSwitchItem(visible = true, enabled = false, onCheckedChange = {}),
        )
    }
}
