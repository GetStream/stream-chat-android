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

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.previewdata.PreviewReactionOptionData
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Individual reaction item.
 *
 * @param option The reaction to show.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ReactionOptionItem(
    option: ReactionOptionItemState,
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier.testTag("Stream_Reaction_${option.type}"),
        painter = option.painter,
        contentDescription = option.type,
    )
}

/**
 * Preview of [ReactionOptionItem] in its non selected state.
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Not Selected)")
@Composable
private fun ReactionOptionItemNotSelectedPreview() {
    ChatTheme {
        ReactionOptionItem(option = PreviewReactionOptionData.reactionOption1())
    }
}

/**
 * Preview of [ReactionOptionItem] in its selected state.
 */
@Preview(showBackground = true, name = "ReactionOptionItem Preview (Selected)")
@Composable
private fun ReactionOptionItemSelectedPreview() {
    ChatTheme {
        ReactionOptionItem(option = PreviewReactionOptionData.reactionOption2())
    }
}
