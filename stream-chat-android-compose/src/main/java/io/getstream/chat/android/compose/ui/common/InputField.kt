package io.getstream.chat.android.compose.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

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
 * @param maxLines The number of lines that are allowed in the input, no limit by default.
 * @param decorationBox Composable function that represents the input field decoration as it's filled with content.
 */
@Composable
public fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
) {
    BasicTextField(
        modifier = modifier
            .border(2.dp, ChatTheme.colors.borders, shape = ChatTheme.shapes.inputField)
            .clip(ChatTheme.shapes.inputField)
            .background(ChatTheme.colors.appBackground)
            .padding(8.dp),
        value = value,
        onValueChange = onValueChange,
        textStyle = ChatTheme.typography.body.copy(
            color = ChatTheme.colors.textHighEmphasis,
        ),
        cursorBrush = SolidColor(ChatTheme.colors.primaryAccent),
        decorationBox = { innerTextField -> decorationBox(innerTextField) },
        maxLines = maxLines
    )
}
