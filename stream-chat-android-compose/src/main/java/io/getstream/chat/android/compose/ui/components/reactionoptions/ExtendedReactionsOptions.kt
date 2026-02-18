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

package io.getstream.chat.android.compose.ui.components.reactionoptions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Reaction

/**
 * Displays all available reactions a user can set on a message.
 *
 * @param ownReactions A list of user's own reactions.
 * @param onReactionOptionSelected Handler that propagates click events on each item.
 * @param modifier Modifier for styling.
 * @param cells Describes the way cells are formed inside [ExtendedReactionsOptions].
 */
@Composable
internal fun ExtendedReactionsOptions(
    ownReactions: List<Reaction>,
    onReactionOptionSelected: (ReactionOptionItemState) -> Unit,
    modifier: Modifier = Modifier,
    cells: GridCells = GridCells.Adaptive(minSize = 48.dp),
) {
    val resolver = ChatTheme.reactionResolver
    val options = resolver.supportedReactions.map { type ->
        val isSelected = ownReactions.any { it.type == type }
        ReactionOptionItemState(
            type = type,
            isSelected = isSelected,
            emojiCode = resolver.emojiCode(type),
        )
    }

    LazyVerticalGrid(modifier = modifier, columns = cells) {
        items(options, key = ReactionOptionItemState::type) { item ->
            ChatTheme.componentFactory.ReactionMenuOptionItem(
                modifier = Modifier.padding(vertical = 8.dp),
                onReactionOptionSelected = onReactionOptionSelected,
                option = item,
            )
        }
    }
}

/**
 * Preview for [ExtendedReactionsOptions] with no reaction selected.
 */
@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ExtendedReactionOptions Preview")
@Composable
internal fun ExtendedReactionOptionsPreview() {
    ChatTheme {
        ExtendedReactionsOptions(
            ownReactions = listOf(),
            onReactionOptionSelected = {},
        )
    }
}

/**
 * Preview for [ExtendedReactionsOptions] with a selected reaction.
 */
@ExperimentalFoundationApi
@Preview(showBackground = true, name = "ExtendedReactionOptions Preview (With Own Reaction)")
@Composable
internal fun ExtendedReactionOptionsWithOwnReactionPreview() {
    ChatTheme {
        ExtendedReactionsOptions(
            ownReactions = listOf(
                Reaction(
                    messageId = "messageId",
                    type = "haha",
                ),
            ),
            onReactionOptionSelected = {},
        )
    }
}
