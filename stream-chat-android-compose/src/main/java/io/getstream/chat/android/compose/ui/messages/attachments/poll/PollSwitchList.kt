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

import android.content.Context
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.poll.PollOptionInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.ui.common.utils.PollsConstants

/**
 * The Poll switch list is that a Composable that enables users to create a poll with configurations.
 *
 * @param modifier The [Modifier] for styling.
 * @param pollSwitchItems A list of [PollSwitchItem] to create the configuration list.
 * @param onSwitchesChanged A lambda that will be executed when a switch on the list is changed.
 * @param itemHeightSize The height size of the question item.
 * @param itemInnerPadding The inner padding size of the question item.
 */
@Composable
public fun PollSwitchList(
    modifier: Modifier = Modifier,
    pollSwitchItems: List<PollSwitchItem>,
    onSwitchesChanged: (List<PollSwitchItem>) -> Unit,
    itemHeightSize: Dp = ChatTheme.dimens.pollOptionInputHeight,
    itemInnerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
) {
    @Suppress("SpreadOperator")
    val switchItemList = remember(pollSwitchItems) { mutableStateListOf(*pollSwitchItems.toTypedArray()) }
    val heightIn = pollSwitchItems.size * (itemHeightSize.value * 2 + 8)

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .heightIn(max = heightIn.dp),
        state = rememberLazyListState(),
        userScrollEnabled = false,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(switchItemList, key = { _, item -> item.key }) { index, item ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .height(
                        if (item.pollSwitchInput != null && item.enabled) {
                            itemHeightSize * 2
                        } else {
                            itemHeightSize
                        },
                    )
                    .border(
                        width = 1.dp,
                        color = if (item.pollOptionError == null) {
                            ChatTheme.colors.backgroundCoreSurface
                        } else {
                            ChatTheme.colors.accentError
                        },
                        shape = RoundedCornerShape(StreamTokens.radiusXl),
                    )
                    .clip(shape = RoundedCornerShape(StreamTokens.radiusXl))
                    .background(ChatTheme.colors.backgroundCoreSurface),
            ) {
                val layoutDirection = LocalLayoutDirection.current
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(
                            start = itemInnerPadding.calculateStartPadding(layoutDirection = layoutDirection),
                            end = itemInnerPadding.calculateEndPadding(layoutDirection = layoutDirection),
                        ),
                ) {
                    val context = LocalContext.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            text = item.title,
                            color = ChatTheme.colors.textPrimary,
                            fontSize = 16.sp,
                        )

                        Switch(
                            colors = SwitchDefaults.colors().copy(
                                checkedTrackColor = ChatTheme.colors.accentSuccess,
                                checkedBorderColor = ChatTheme.colors.accentSuccess,
                                uncheckedTrackColor = ChatTheme.colors.textSecondary,
                                uncheckedBorderColor = ChatTheme.colors.textSecondary,
                            ),
                            thumbContent = {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            color = if (item.enabled) {
                                                Color.White
                                            } else {
                                                ChatTheme.colors.backgroundCoreDisabled
                                            },
                                        ),
                                )
                            },
                            checked = item.enabled,
                            onCheckedChange = { checked ->
                                switchItemList[index] = item.copy(
                                    enabled = checked,
                                    // Validate the poll switch input even on checked state change
                                    pollOptionError = item.pollSwitchInput?.run {
                                        if (checked) {
                                            errorOrNull(context, value.toString())
                                        } else {
                                            null
                                        }
                                    },
                                )
                                onSwitchesChanged(switchItemList)
                            },
                        )
                    }

                    if (item.pollSwitchInput != null && item.enabled) {
                        val switchInput = item.pollSwitchInput
                        PollOptionInput(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            value = switchInput.value.toString(),
                            description = switchInput.description,
                            shape = RoundedCornerShape(0.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = switchInput.keyboardType),
                            onValueChange = { newValue ->
                                switchItemList[index] = item.copy(
                                    pollSwitchInput = switchInput.copy(value = newValue),
                                    pollOptionError = switchInput.errorOrNull(context, newValue),
                                )
                                onSwitchesChanged(switchItemList)
                            },
                            innerPadding = PaddingValues(vertical = 4.dp),
                            decorationBox = { innerTextField ->
                                if (item.pollOptionError == null) {
                                    innerTextField.invoke()
                                } else if (item.title.isNotBlank()) {
                                    Column {
                                        Text(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp),
                                            text = item.pollOptionError.message,
                                            color = ChatTheme.colors.accentError,
                                            fontSize = 12.sp,
                                        )
                                        innerTextField.invoke()
                                    }
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

internal fun PollSwitchInput.errorOrNull(
    context: Context,
    input: String,
): PollOptionError? {
    if (input.isBlank()) return null

    return when (keyboardType) {
        KeyboardType.Number -> validateRange(
            context = context,
            input = input,
            parse = String::toIntOrNull,
            defaultValue = -1,
        )
        KeyboardType.Decimal -> validateRange(
            context = context,
            input = input,
            parse = String::toFloatOrNull,
            defaultValue = -1f,
        )
        else -> null
    }
}

private inline fun <T : Comparable<T>> PollSwitchInput.validateRange(
    context: Context,
    input: String,
    parse: (String) -> T?,
    defaultValue: T,
): PollOptionError? {
    val value = parse(input) ?: defaultValue
    val min = minValue?.toString()?.let(parse) ?: defaultValue
    val max = maxValue?.toString()?.let(parse) ?: defaultValue

    return if (value < min || value > max) {
        PollOptionNumberExceed(
            message = context.getString(R.string.stream_ui_poll_multiple_answers_error, min, max),
        )
    } else {
        null
    }
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
        modifier = Modifier.fillMaxWidth(),
        pollSwitchItems = listOf(
            PollSwitchItem(
                title = stringResource(id = R.string.stream_compose_poll_option_switch_multiple_answers),
                pollSwitchInput = PollSwitchInput(
                    keyboardType = KeyboardType.Number,
                    description = stringResource(id = R.string.stream_compose_poll_option_max_number_of_answers_hint),
                    maxValue = 10,
                    value = 11,
                ),
                pollOptionError = PollOptionNumberExceed(
                    message = stringResource(
                        R.string.stream_ui_poll_multiple_answers_error,
                        PollsConstants.MIN_NUMBER_OF_MULTIPLE_ANSWERS,
                        PollsConstants.MAX_NUMBER_OF_MULTIPLE_ANSWERS,
                    ),
                ),
                enabled = true,
            ),
            PollSwitchItem(
                title = stringResource(id = R.string.stream_compose_poll_option_switch_multiple_answers),
                pollSwitchInput = PollSwitchInput(
                    keyboardType = KeyboardType.Text,
                    value = "",
                    maxValue = "",
                ),
                enabled = true,
            ),
            PollSwitchItem(
                title = stringResource(id = R.string.stream_compose_poll_option_switch_anonymous_poll),
                enabled = false,
            ),
            PollSwitchItem(
                title = stringResource(id = R.string.stream_compose_poll_option_switch_suggest_option),
                enabled = false,
            ),
        ),
        onSwitchesChanged = {},
    )
}
