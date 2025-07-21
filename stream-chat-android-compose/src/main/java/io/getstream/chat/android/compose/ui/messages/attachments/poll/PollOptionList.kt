/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

@file:OptIn(ExperimentalFoundationApi::class)

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.poll.PollOptionInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.ui.common.utils.PollsConstants
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

/**
 * The Poll Creation option list that is a Composable that enables users to create and reorder question items easily.
 *
 * @param modifier The [Modifier] for styling.
 * @param lazyListState State of the lazy list that represents the list of messages. Useful for controlling the
 * scroll state.
 * @param title The title of the question list.
 * @param optionItems The list of pre-questions. The type of the list is [PollOptionItem].
 * @param onQuestionsChanged This lambda will be executed when the item of the question list is reordered.
 * @param itemHeightSize The height size of the question item.
 * @param itemInnerPadding The inner padding size of the question item.
 */
@Composable
public fun PollOptionList(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    title: String = stringResource(id = R.string.stream_compose_poll_option_title),
    optionItems: List<PollOptionItem> = emptyList(),
    onQuestionsChanged: (List<PollOptionItem>) -> Unit,
    itemHeightSize: Dp = ChatTheme.dimens.pollOptionInputHeight,
    itemInnerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
) {
    val context = LocalContext.current
    var optionItemList by remember(optionItems) { mutableStateOf(optionItems) }

    val reorderableLazyListState = rememberReorderableLazyListState(
        lazyListState = lazyListState,
        scrollThreshold = itemHeightSize,
    ) { from, to ->
        optionItemList = optionItemList.toMutableList().apply {
            add(to.index, removeAt(from.index))
            onQuestionsChanged.invoke(optionItemList)
        }
    }

    val heightIn = optionItemList.size * (itemHeightSize.value + 8)

    Text(
        modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp),
        text = title,
        color = ChatTheme.colors.textHighEmphasis,
        style = ChatTheme.typography.title3,
        fontSize = 16.sp,
    )

    LazyColumn(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .heightIn(max = heightIn.dp),
        userScrollEnabled = false,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(optionItemList, key = { _, item -> item.key }) { index, item ->
            ReorderableItem(reorderableLazyListState, key = item.key) { _ ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightSize)
                        .border(
                            width = 1.dp,
                            color = if (item.pollOptionError == null) {
                                ChatTheme.colors.inputBackground
                            } else {
                                ChatTheme.colors.errorAccent
                            },
                            shape = ChatTheme.shapes.pollOptionInput,
                        )
                        .clip(shape = ChatTheme.shapes.pollOptionInput)
                        .background(ChatTheme.colors.inputBackground),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PollOptionInput(
                        modifier = Modifier.weight(1f),
                        value = item.title,
                        description = stringResource(id = R.string.stream_compose_poll_option_hint),
                        innerPadding = if (item.pollOptionError == null) {
                            PaddingValues(horizontal = 16.dp, vertical = 18.dp)
                        } else {
                            PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        },
                        onValueChange = { newTitle ->
                            optionItemList.toMutableList().apply {
                                val duplicated = this.any { it.title == newTitle }
                                if (duplicated) {
                                    this[index] = item.copy(
                                        title = newTitle,
                                        pollOptionError = PollOptionDuplicated(
                                            context.getString(R.string.stream_compose_poll_option_error_duplicated),
                                        ),
                                    )
                                } else {
                                    this[index] =
                                        item.copy(title = newTitle, pollOptionError = null)
                                }

                                optionItemList = this
                                onQuestionsChanged.invoke(this)
                            }
                        },
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
                                        color = ChatTheme.colors.errorAccent,
                                        fontSize = 12.sp,
                                    )
                                    innerTextField.invoke()
                                }
                            }
                        },
                    )

                    IconButton(
                        modifier = Modifier.draggableHandle(),
                        onClick = {},
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.stream_compose_ic_drag_handle),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeightSize)
            .padding(horizontal = 16.dp)
            .clip(shape = ChatTheme.shapes.pollOptionInput)
            .background(ChatTheme.messageComposerTheme.inputField.backgroundColor)
            .clickable(enabled = optionItemList.size < PollsConstants.MAX_NUMBER_OF_VOTES_PER_USER) {
                optionItemList = optionItemList
                    .toMutableList()
                    .apply {
                        add(PollOptionItem(title = ""))
                        onQuestionsChanged.invoke(this)
                    }
            },
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterStart)
                .padding(itemInnerPadding),
            text = stringResource(id = R.string.stream_compose_poll_option_description),
            color = ChatTheme.colors.textLowEmphasis,
            fontSize = 16.sp,
        )
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
