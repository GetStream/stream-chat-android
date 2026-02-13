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

package io.getstream.chat.android.compose.ui.messages.composer.internal

import androidx.compose.foundation.layout.Box
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
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ComposerInputFieldTheme
import io.getstream.chat.android.compose.ui.theme.StreamColors
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.StreamTypography
import io.getstream.chat.android.compose.ui.util.buildAnnotatedInputText
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.feature.messages.composer.mention.Mention
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState

@Composable
internal fun MessageComposerInputCenterContent(
    state: MessageComposerState,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val value = state.inputValue

    // Manages the String ↔ TextFieldValue bridge for BasicTextField
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

    val description = stringResource(id = R.string.stream_compose_cd_message_input)
    val inputFieldTheme = ChatTheme.messageComposerTheme.inputField
    val visualTransformation = rememberVisualTransformation(state.selectedMentions)
    val canSendMessage = state.canSendMessage()

    BasicTextField(
        modifier = modifier
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
        textStyle = inputFieldTheme.textStyle,
        cursorBrush = SolidColor(inputFieldTheme.cursorBrushColor),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.padding(
                    start = StreamTokens.spacingMd,
                    bottom = StreamTokens.spacingMd,
                ),
                contentAlignment = Alignment.CenterStart,
            ) {
                innerTextField()

                if (value.isEmpty()) {
                    TextFieldPlaceholder(canSendMessage)
                }
            }
        },
        maxLines = TextFieldMaxLines,
        singleLine = false,
        enabled = canSendMessage,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )
}

@Composable
private fun TextFieldPlaceholder(canSendMessage: Boolean) {
    val text = if (canSendMessage) {
        stringResource(id = R.string.stream_compose_message_label)
    } else {
        stringResource(id = R.string.stream_compose_cannot_send_messages_label)
    }
    Text(
        text = text,
        color = ChatTheme.colors.textLowEmphasis,
        style = ChatTheme.messageComposerTheme.inputField.textStyle,
    )
}

@Composable
private fun rememberVisualTransformation(mentions: Set<Mention>): TextFieldVisualTransformation {
    val inputFieldTheme = ChatTheme.messageComposerTheme.inputField
    val typography = ChatTheme.typography
    val colors = ChatTheme.colors
    return remember(inputFieldTheme, typography, colors, mentions) {
        TextFieldVisualTransformation(
            inputFieldTheme = inputFieldTheme,
            typography = typography,
            colors = colors,
            mentions = mentions,
        )
    }
}

private const val TextFieldMaxLines = 5

private class TextFieldVisualTransformation(
    val inputFieldTheme: ComposerInputFieldTheme,
    val typography: StreamTypography,
    val colors: StreamColors,
    val mentions: Set<Mention>,
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
            mentions = mentions,
            mentionStyleFactory = inputFieldTheme.mentionStyleFactory,
        )
        return TransformedText(transformed, OffsetMapping.Identity)
    }
}
