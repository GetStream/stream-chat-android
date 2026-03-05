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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.poll.PollOptionInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableScope

/**
 * The Poll Creation option list that is a Composable that enables users to create and reorder question items easily.
 *
 * @param modifier The [Modifier] for styling.
 * @param title The title of the question list.
 * @param optionItems The list of pre-questions. The type of the list is [PollOptionItem].
 * @param onQuestionsChanged This lambda will be executed when the item of the question list is reordered.
 */
@Composable
public fun PollOptionList(
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.stream_compose_poll_option_title),
    optionItems: List<PollOptionItem> = emptyList(),
    onQuestionsChanged: (List<PollOptionItem>) -> Unit,
) {
    val context = LocalContext.current
    val colors = ChatTheme.colors
    var optionItemList by remember(optionItems) { mutableStateOf(optionItems) }

    Column(
        modifier = modifier
            .padding(vertical = StreamTokens.spacingXs)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        Text(
            modifier = Modifier.padding(top = StreamTokens.spacingMd),
            text = title,
            color = colors.textPrimary,
            style = ChatTheme.typography.headingSmall,
        )

        ReorderableColumn(
            list = optionItemList,
            onSettle = { fromIndex, toIndex ->
                optionItemList = optionItemList.toMutableList().apply {
                    add(toIndex, removeAt(fromIndex))
                }
                onQuestionsChanged(optionItemList)
            },
            verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
        ) { index, item, _ ->
            key(item.key) {
                PollOptionRow(
                    item = item,
                    onTitleChange = { newTitle ->
                        optionItemList = optionItemList.updateOnTitleChange(context, index, newTitle)
                        onQuestionsChanged.invoke(optionItemList)
                    },
                    onRemove = {
                        optionItemList = optionItemList.toMutableList().apply { removeAt(index) }
                        onQuestionsChanged(optionItemList)
                    },
                )
            }
        }

        AddPollOptionButton(
            onClick = {
                optionItemList = optionItemList
                    .toMutableList()
                    .apply {
                        add(PollOptionItem(title = ""))
                        onQuestionsChanged.invoke(this)
                    }
            },
        )
    }
}

@Composable
private fun ReorderableScope.PollOptionRow(
    item: PollOptionItem,
    onTitleChange: (String) -> Unit,
    onRemove: () -> Unit,
) {
    val colors = ChatTheme.colors
    val borderColor = if (item.pollOptionError == null) colors.inputBorderDefault else colors.accentError
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = PollInputShape)
            .clip(shape = PollInputShape),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            modifier = Modifier.draggableHandle(),
            onClick = {},
        ) {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_drag_handle),
                contentDescription = null,
                tint = colors.inputTextIcon,
                modifier = Modifier.size(20.dp),
            )
        }

        PollOptionInput(
            modifier = Modifier.weight(1f),
            value = item.title,
            description = stringResource(id = R.string.stream_compose_poll_option_hint),
            onValueChange = onTitleChange,
            decorationBox = { innerTextField ->
                if (item.pollOptionError == null || item.title.isBlank()) {
                    innerTextField()
                } else {
                    Column {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = StreamTokens.spacing2xs),
                            text = item.pollOptionError.message,
                            color = colors.accentError,
                            fontSize = 12.sp,
                        )
                        innerTextField()
                    }
                }
            },
        )

        IconButton(onClick = onRemove) {
            Icon(
                painter = painterResource(R.drawable.stream_compose_ic_circle_minus),
                contentDescription = null,
                tint = colors.inputTextIcon,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun AddPollOptionButton(onClick: () -> Unit) {
    val colors = ChatTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = PollInputMinHeight)
            .border(
                width = 1.dp,
                color = colors.inputBorderDefault,
                shape = PollInputShape,
            )
            .clip(PollInputShape)
            .clickable(onClick = onClick),
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacingSm),
            text = stringResource(id = R.string.stream_compose_poll_option_description),
            style = ChatTheme.typography.bodyDefault,
            color = colors.inputTextPlaceholder,
        )
    }
}

// Align with IconButton's default min touch target size
internal val PollInputMinHeight @Composable get() = LocalMinimumInteractiveComponentSize.current
internal val PollInputShape = RoundedCornerShape(StreamTokens.radiusLg)

private fun List<PollOptionItem>.updateOnTitleChange(
    context: Context,
    affectedIndex: Int,
    newTitle: String,
): List<PollOptionItem> {
    val isDuplicated = withIndex().any { it.index != affectedIndex && it.value.title.trim() == newTitle.trim() }
    return mapIndexed { i, optionItem ->
        if (i == affectedIndex) {
            optionItem.copy(
                title = newTitle,
                pollOptionError = when {
                    isDuplicated -> PollOptionDuplicated(
                        context.getString(R.string.stream_compose_poll_option_error_duplicated),
                    )

                    else -> null
                },
            )
        } else {
            optionItem
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PollOptionListEmptyPreview() {
    ChatTheme {
        PollOptionListEmpty()
    }
}

@Composable
internal fun PollOptionListEmpty() {
    Column {
        PollOptionList(
            onQuestionsChanged = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PollOptionListBlankPreview() {
    ChatTheme {
        PollOptionListBlank()
    }
}

@Composable
internal fun PollOptionListBlank() {
    Column {
        @Suppress("MagicNumber")
        PollOptionList(
            optionItems = List(4) { PollOptionItem("") },
            onQuestionsChanged = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PollOptionListDuplicatedErrorPreview() {
    ChatTheme {
        PollOptionListDuplicatedError()
    }
}

@Composable
internal fun PollOptionListDuplicatedError() {
    Column {
        @Suppress("MagicNumber")
        PollOptionList(
            optionItems = List(3) { PollOptionItem("This is a poll item $it") } +
                PollOptionItem(
                    title = "This is a poll item with error",
                    pollOptionError = PollOptionDuplicated("duplicated!"),
                ),
            onQuestionsChanged = {},
        )
    }
}
