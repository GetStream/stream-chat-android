/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewReactionOptionData
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a reaction bubble with a list of reactions this message has.
 *
 * @param options The list of reactions to display.
 * @param modifier Modifier for styling.
 * @param itemContent Composable that represents each reaction item in the row of message reactions.
 */
@Composable
public fun MessageReactions(
    options: List<ReactionOptionItemState>,
    modifier: Modifier = Modifier,
    itemContent: @Composable RowScope.(ReactionOptionItemState) -> Unit = { option ->
        MessageReactionItem(
            modifier = Modifier
                .semantics {
                    testTag = "Stream_MessageReaction_${option.type}"
                    contentDescription = option.type
                }
                .size(20.dp)
                .padding(2.dp)
                .align(Alignment.CenterVertically),
            option = option,
        )
    },
) {
    val description = pluralStringResource(
        R.plurals.stream_ui_message_list_semantics_message_reactions,
        options.size,
        options.size,
    )
    Row(
        modifier = modifier
            .semantics {
                testTag = "Stream_MessageReaction"
                contentDescription = description
            }
            .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.barsBackground)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        options.forEach { option ->
            itemContent(option)
        }
    }
}

/**
 * Preview of the [MessageReactions] with one reaction.
 */
@Preview
@Composable
private fun OneMessageReactionPreview() {
    ChatTheme {
        MessageReactions(options = PreviewReactionOptionData.oneReaction())
    }
}

/**
 * Preview of the [MessageReactions] with many reactions.
 */
@Preview
@Composable
private fun ManyMessageReactionsPreview() {
    ChatTheme {
        MessageReactions(options = PreviewReactionOptionData.manyReactions())
    }
}
