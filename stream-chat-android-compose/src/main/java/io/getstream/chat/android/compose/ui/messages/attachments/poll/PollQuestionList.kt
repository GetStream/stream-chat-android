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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
public fun PollQuestionList(
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    questions: List<String>,
    itemHeightSize: Dp = 56.dp,
    itemInnerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
    onItemChanged: (Int, Int) -> Unit,
) {
    val reorderableLazyListState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onItemChanged.invoke(from.index, to.index)
    }

    val heightIn = questions.size * (itemHeightSize.value + 8)

    LazyColumn(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .heightIn(max = heightIn.dp),
        userScrollEnabled = false,
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(questions, key = { it }) {
            ReorderableItem(reorderableLazyListState, key = it) { isDragging ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeightSize)
                        .clip(shape = ChatTheme.shapes.pollOptionInput)
                        .background(ChatTheme.colors.inputBackground)
                        .padding(itemInnerPadding),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = it,
                        color = ChatTheme.colors.textHighEmphasis,
                        fontStyle = ChatTheme.typography.body.fontStyle,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )

                    IconButton(
                        modifier = Modifier.draggableHandle(),
                        onClick = {},
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.stream_compose_ic_reply),
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}
