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

package io.getstream.chat.android.compose.ui.components.poll

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.buildAnnotatedMessageText

/**
 * Custom input field that we use for our Poll option UI. It's fairly simple - shows a basic input with clipped
 * corners and a fully-customizable the input box with the [decorationBox], with some extra padding on each side.
 *
 * Within it, we allow for custom decoration, so that the user can define what the input field looks like
 * when filled with content.
 *
 * @param value The current input value.
 * @param onValueChange Handler when the value changes as the user types.
 * @param modifier Modifier for styling.
 * @param description Description that you want to display if they [value] is blank.
 * @param enabled If the Composable is enabled for text input or not.
 * @param maxLines The number of lines that are allowed in the input.
 * @param maxLength The number of maxLength that are allowed in the input.
 * @param innerPadding The number of letter length that are allowed in the input.
 * @param innerPadding The padding inside the input field, around the label or input.
 * @param keyboardOptions The [KeyboardOptions] to be applied to the input.
 * @param decorationBox Composable function that represents the input field decoration as it's filled with content.
 */
@Suppress("LongMethod")
@Composable
public fun PollOptionInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    description: String = stringResource(id = R.string.stream_compose_poll_questions_description),
    enabled: Boolean = true,
    maxLines: Int = 1,
    maxLength: Int = 80,
    shape: Shape = ChatTheme.shapes.pollOptionInput,
    innerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
) {
    val typography = ChatTheme.typography
    val colors = ChatTheme.colors
    val textColor = ChatTheme.colors.textHighEmphasis
    val focusRequester = remember { FocusRequester() }
    // Using TextFieldValue to manage the text input state,
    // which allows us to set the cursor position to the end of the text
    // when the input is first drawn.
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    Box(modifier = modifier.height(ChatTheme.dimens.pollOptionInputHeight)) {
        BasicTextField(
            modifier = Modifier
                .fillMaxSize()
                .clip(shape = shape)
                .background(ChatTheme.colors.inputBackground)
                .padding(innerPadding)
                .focusRequester(focusRequester)
                .semantics { contentDescription = description },
            value = textFieldValue,
            onValueChange = {
                if (it.text.length <= maxLength) {
                    onValueChange.invoke(it.text)
                }
            },
            visualTransformation = {
                val styledText = buildAnnotatedMessageText(
                    text = it.text,
                    textColor = textColor,
                    textFontStyle = typography.body.fontStyle,
                    linkStyle = TextStyle(
                        color = colors.primaryAccent,
                        textDecoration = TextDecoration.Underline,
                    ),
                    mentionsColor = colors.primaryAccent,
                )
                TransformedText(styledText, OffsetMapping.Identity)
            },
            textStyle = StreamTypography.defaultTypography().body.copy(
                fontSize = 16.sp,
                textDirection = TextDirection.Content,
            ),
            cursorBrush = SolidColor(ChatTheme.colors.primaryAccent),
            decorationBox = { innerTextField -> decorationBox(innerTextField) },
            maxLines = maxLines,
            singleLine = maxLines == 1,
            enabled = enabled,
            keyboardOptions = keyboardOptions,
        )

        if (value.isBlank()) {
            Text(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .align(Alignment.CenterStart)
                    .semantics { contentDescription = description },
                text = description,
                style = ChatTheme.typography.body.copy(
                    color = colors.textLowEmphasis,
                    textDirection = TextDirection.Content,
                ),
                fontSize = 16.sp,
            )
        }
    }

    // Request focus initially when the Input is first drawn.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
public fun PollOptionInputPreview() {
    ChatTheme {
        Column(
            modifier = Modifier
                .background(ChatTheme.colors.appBackground)
                .fillMaxWidth()
                .height(400.dp)
                .padding(16.dp),
        ) {
            PollOptionInput(
                modifier = Modifier.fillMaxWidth(),
                value = "",
                onValueChange = {},
                decorationBox = { innerTextField -> innerTextField.invoke() },
            )

            Spacer(modifier = Modifier.height(16.dp))

            PollOptionInput(
                modifier = Modifier.fillMaxWidth(),
                value = "This is an amazing question!",
                onValueChange = {},
                decorationBox = { innerTextField -> innerTextField.invoke() },
            )
        }
    }
}
