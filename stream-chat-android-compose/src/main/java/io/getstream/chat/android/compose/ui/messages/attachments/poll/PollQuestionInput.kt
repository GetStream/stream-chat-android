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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.poll.PollOptionInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens

/**
 * Poll option's question Composable that consist of the title and [PollOptionInput].
 *
 * @param modifier The [Modifier] for styling.
 * @param title Title to display on the top.
 * @param question The question strings to reflect user inputs.
 * @param onQuestionChanged The lambda parameter to apply user input changes.
 */
@Composable
public fun PollQuestionInput(
    modifier: Modifier = Modifier,
    title: String = stringResource(id = R.string.stream_compose_poll_questions_title),
    question: String,
    onQuestionChanged: (String) -> Unit,
) {
    val colors = ChatTheme.colors

    Column(modifier, verticalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs)) {
        Text(
            text = title,
            color = colors.textPrimary,
            style = ChatTheme.typography.headingSmall,
        )

        PollOptionInput(
            value = question,
            onValueChange = onQuestionChanged,
            description = stringResource(R.string.stream_compose_poll_questions_description),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = colors.inputBorderDefault,
                    shape = PollInputShape,
                )
                .clip(shape = PollInputShape)
                .defaultMinSize(minHeight = PollInputMinHeight)
                .padding(horizontal = StreamTokens.spacingMd, vertical = StreamTokens.spacingSm),
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PollOptionQuestionsPreview() {
    ChatTheme {
        PollQuestionInput(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.backgroundCoreApp),
            question = "This is an amazing question!",
            onQuestionChanged = {},
        )
    }
}
