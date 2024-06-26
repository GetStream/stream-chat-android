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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The Poll switch list is that a Composable that enables users to create a poll with configurations.
 *
 * @param modifier The [Modifier] for styling.
 * @param pollSwitchItems A list of [PollSwitchItem] to create the configuration list.
 * @param onSwitchesChanged A lambda that will be executed when a switch on the list is changed.
 * @param itemHeightSize The height size of the question item.
 * @param itemInnerPadding The inner padding size of the question item.
 * It provides the index information [from] and [to] as a receiver, so you must swap the item of the [questions] list.
 */
@Composable
public fun PollSwitchList(
    modifier: Modifier = Modifier,
    pollSwitchItems: List<PollSwitchItem>,
    onSwitchesChanged: (List<PollSwitchItem>) -> Unit,
    itemHeightSize: Dp = ChatTheme.dimens.pollOptionInputHeight,
    itemInnerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
) {
    val context = LocalContext.current
    var switchItemList by remember(pollSwitchItems) { mutableStateOf(pollSwitchItems) }
    val heightIn = pollSwitchItems.size * (itemHeightSize.value + 8)

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
        }
    }
}
