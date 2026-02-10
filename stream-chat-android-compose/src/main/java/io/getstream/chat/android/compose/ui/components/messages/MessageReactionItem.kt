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

package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.previewdata.PreviewReactionData
import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.compose.ui.components.reactions.ReactionIconSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a small message reaction item.
 *
 * @param state The reaction option state, holding all information required to render the emoji.
 * @param modifier for styling.
 */
@Composable
public fun MessageReactionItem(
    state: MessageReactionItemState,
    modifier: Modifier = Modifier,
) {
    ChatTheme.componentFactory.ReactionIcon(
        type = state.item.type,
        emoji = state.item.emoji,
        size = ReactionIconSize.Small,
        modifier = modifier,
    )
}

/**
 * Preview for [MessageReactionItem] that's selected.
 */
@Preview
@Composable
public fun MessageReactionItemSelectedPreview() {
    ChatTheme {
        MessageReactionItem(state = PreviewReactionData.messageReaction2())
    }
}

/**
 * Preview for [MessageReactionItem] that's not selected.
 */
@Preview
@Composable
public fun MessageReactionItemNotSelectedPreview() {
    ChatTheme {
        MessageReactionItem(state = PreviewReactionData.messageReaction1())
    }
}
