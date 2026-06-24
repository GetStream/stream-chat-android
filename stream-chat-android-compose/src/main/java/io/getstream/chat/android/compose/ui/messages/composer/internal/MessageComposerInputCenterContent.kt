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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.messages.composer.ComposerInputTestTag
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamDesign
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.buildAnnotatedInputText
import io.getstream.chat.android.compose.ui.util.extensions.internal.placeholderRes
import io.getstream.chat.android.ui.common.feature.messages.composer.capabilities.canSendMessage
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.R as UiCommonR

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

    val colors = ChatTheme.colors
    val typography = ChatTheme.typography
    val visualTransformation = rememberVisualTransformation(typography, colors)
    val canSendMessage = state.canSendMessage()

    BasicTextField(
        modifier = modifier
            .fillMaxWidth()
            .testTag(ComposerInputTestTag)
            .heightIn(min = 48.dp),
        value = textState,
        onValueChange = {
            textState = it
            if (value != it.text) onValueChange(it.text)
        },
        visualTransformation = visualTransformation,
        textStyle = typography.bodyDefault.copy(color = colors.textPrimary),
        cursorBrush = SolidColor(colors.accentPrimary),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.padding(
                    start = if (state.activeCommand != null) {
                        StreamTokens.spacingSm
                    } else {
                        StreamTokens.spacingMd
                    },
                    top = StreamTokens.spacingSm,
                    bottom = StreamTokens.spacingSm,
                ),
                contentAlignment = Alignment.CenterStart,
            ) {
                innerTextField()

                if (value.isEmpty()) {
                    TextFieldPlaceholder(
                        canSendMessage = canSendMessage,
                        coolDownTime = state.coolDownTime,
                        activeCommandDescriptionRes = state.activeCommand?.placeholderRes,
                    )
                }
            }
        },
        maxLines = TextFieldMaxLines,
        singleLine = false,
        enabled = canSendMessage && state.coolDownTime <= 0,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
    )
}

@Composable
private fun TextFieldPlaceholder(
    canSendMessage: Boolean,
    coolDownTime: Int,
    @StringRes activeCommandDescriptionRes: Int?,
) {
    val text = when {
        coolDownTime > 0 ->
            stringResource(UiCommonR.string.stream_ui_message_composer_placeholder_slow_mode, coolDownTime)

        canSendMessage ->
            stringResource(
                activeCommandDescriptionRes ?: UiCommonR.string.stream_ui_message_composer_placeholder_default,
            )

        else ->
            stringResource(UiCommonR.string.stream_ui_message_composer_placeholder_cannot_send_messages)
    }
    Text(
        text = text,
        color = ChatTheme.colors.inputTextPlaceholder,
        style = ChatTheme.typography.bodyDefault,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun rememberVisualTransformation(
    typography: StreamDesign.Typography,
    colors: StreamDesign.Colors,
): TextFieldVisualTransformation = remember(typography, colors) {
    TextFieldVisualTransformation(
        typography = typography,
        colors = colors,
    )
}

private const val TextFieldMaxLines = 5

private class TextFieldVisualTransformation(
    val typography: StreamDesign.Typography,
    val colors: StreamDesign.Colors,
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val transformed = buildAnnotatedInputText(
            text = text.text,
            textColor = colors.textPrimary,
            textFontStyle = typography.bodyDefault.fontStyle,
            linkStyle = TextStyle(
                color = colors.accentPrimary,
                textDecoration = TextDecoration.Underline,
            ),
        )
        return TransformedText(transformed, OffsetMapping.Identity)
    }
}
