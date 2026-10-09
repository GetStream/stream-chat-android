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

package io.getstream.chat.android.ai.compose.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A horizontally scrolling row of suggestion chips, usually placed above [ChatComposer] on a
 * new chat.
 *
 * Each chip shows its text on up to 2 lines. Tapping a chip calls [onSuggestionClick] with the
 * chip's exact text. The component doesn't send anything itself, so the caller decides what
 * happens, for example sending the text right away. All chips in the row have the same height.
 * Customize the chips through [ChatAiComponentFactory.AISuggestionsChip].
 *
 * @param suggestions The texts shown as chips, in order.
 * @param onSuggestionClick Called with the chip's text when the user taps a chip.
 * @param modifier Modifier to be applied to the row.
 * @param itemMaxWidth The maximum width of a chip. Longer text wraps to a second line, then ends
 * with an ellipsis.
 * @param contentPadding Padding around the chips. It scrolls with the chips.
 */
@Composable
public fun AISuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    itemMaxWidth: Dp = 160.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .horizontalScroll(rememberScrollState())
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val componentFactory = LocalChatAiComponentFactory.current
        suggestions.forEach { suggestion ->
            key(suggestion) {
                // Gives every chip, custom ones too, the row's height and at most itemMaxWidth.
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = itemMaxWidth),
                    propagateMinConstraints = true,
                ) {
                    componentFactory.AISuggestionsChip(
                        AISuggestionsChipParams(
                            text = suggestion,
                            onClick = { onSuggestionClick(suggestion) },
                        ),
                    )
                }
            }
        }
    }
}

@Composable
internal fun DefaultAISuggestionsChip(params: AISuggestionsChipParams) {
    SuggestionChip(
        onClick = params.onClick,
        label = {
            Text(
                text = params.text,
                modifier = Modifier.padding(vertical = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
    )
}

@Composable
internal fun AISuggestionsSample(itemMaxWidth: Dp = 160.dp) {
    AISuggestions(
        suggestions = listOf(
            "What are the docs for the AI SDK?",
            "Plan a weekend trip",
            "Explain how streaming responses work in simple words",
        ),
        onSuggestionClick = {},
        itemMaxWidth = itemMaxWidth,
    )
}

@Preview(showBackground = true)
@Composable
private fun AISuggestionsPreview() {
    AISuggestionsSample()
}
