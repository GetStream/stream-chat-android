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

package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ComposerInputFieldTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.buildAnnotatedInputText

/**
 * Custom input field that we use for our UI. It's fairly simple - shows a basic input with clipped
 * corners and a border stroke, with some extra padding on each side.
 *
 * Within it, we allow for custom decoration, so that the user can define what the input field looks like
 * when filled with content.
 *
 * @param value The current input value.
 * @param onValueChange Handler when the value changes as the user types.
 * @param modifier Modifier for styling.
 * @param enabled If the Composable is enabled for text input or not.
 * @param maxLines The number of lines that are allowed in the input, no limit by default.
 * @param border The [BorderStroke] that will appear around the input field.
 * @param innerPadding The padding inside the input field, around the label or input.
 * @param keyboardOptions The [KeyboardOptions] to be applied to the input.
 * @param visualTransformation The [VisualTransformation] to be applied to the input. By default, it applies text
 * styling and link styling.
 * @param decorationBox Composable function that represents the input field decoration as it's filled with content.
 */
@Composable
public fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    border: BorderStroke = BorderStroke(1.dp, ChatTheme.colors.borders),
    innerPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    keyboardOptions: KeyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    visualTransformation: VisualTransformation = DefaultInputFieldVisualTransformation(
        inputFieldTheme = ChatTheme.messageComposerTheme.inputField,
        typography = ChatTheme.typography,
        colors = ChatTheme.colors,
    ),
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
) {
    var textState by remember { mutableStateOf(TextFieldValue(text = value)) }

    if (textState.text != value) {
        // Workaround to move cursor to the end after selecting a suggestion
        LaunchedEffect(value) {
            if (textState.text != value) {
                textState = textState.copy(
                    text = value,
                    selection = TextRange(value.length),
                )
            }
        }
    }

    val theme = ChatTheme.messageComposerTheme.inputField
    val description = stringResource(id = R.string.stream_compose_cd_message_input)

    BasicTextField(
        modifier = modifier
            .border(border = border, shape = theme.borderShape)
            .clip(shape = theme.borderShape)
            .background(theme.backgroundColor)
            .padding(innerPadding)
            .semantics { contentDescription = description }
            .testTag("Stream_ComposerInputField"),
        value = textState,
        onValueChange = {
            textState = it
            if (value != it.text) {
                onValueChange(it.text)
            }
        },
        visualTransformation = visualTransformation,
        textStyle = theme.textStyle,
        cursorBrush = SolidColor(theme.cursorBrushColor),
        decorationBox = { innerTextField -> decorationBox(innerTextField) },
        maxLines = maxLines,
        singleLine = maxLines == 1,
        enabled = enabled,
        keyboardOptions = keyboardOptions,
    )
}

/**
 * Default visual transformation for the [InputField] composable.
 * Applies text styling and link styling to the input text.
 *
 * @param inputFieldTheme The theme for the input field.
 * @param typography The typography styles to be used.
 * @param colors The color palette to be used.
 */
private class DefaultInputFieldVisualTransformation(
    val inputFieldTheme: ComposerInputFieldTheme,
    val typography: StreamTypography,
    val colors: StreamColors,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val textColor = inputFieldTheme.textStyle.color
        val fontStyle = typography.body.fontStyle
        val linkStyle = TextStyle(
            color = colors.primaryAccent,
            textDecoration = TextDecoration.Underline,
        )
        val transformed = buildAnnotatedInputText(
            text = text.text,
            textColor = textColor,
            textFontStyle = fontStyle,
            linkStyle = linkStyle,
        )
        return TransformedText(transformed, OffsetMapping.Identity)
    }
}

@Preview
@Composable
private fun InputFieldPreview() {
    ChatTheme {
        InputField(
            modifier = Modifier.fillMaxWidth(),
            value = "InputFieldPreview",
            onValueChange = { _ -> },
            decorationBox = { innerTextField -> innerTextField.invoke() },
        )
    }
}
