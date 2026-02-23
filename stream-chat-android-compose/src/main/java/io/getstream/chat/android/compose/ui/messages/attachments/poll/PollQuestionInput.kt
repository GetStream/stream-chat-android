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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.poll.PollOptionInput
import io.getstream.chat.android.compose.ui.theme.ChatTheme

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
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = title,
            color = ChatTheme.colors.textPrimary,
            style = ChatTheme.typography.headingMedium,
            fontSize = 16.sp,
        )

        PollOptionInput(
            value = question,
            onValueChange = onQuestionChanged,
            decorationBox = { innerTextField ->
                innerTextField.invoke()
            },
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
                .background(ChatTheme.colors.appBackground),
            question = "This is an amazing question!",
            onQuestionChanged = {},
        )
    }
}
