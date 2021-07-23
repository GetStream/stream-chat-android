package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Custom input field that we use for our UI. It's fairly simple - shows a basic input with clipped
 * corners and a border stroke, with some extra padding on each side.
 *
 * Within it, we allow for custom decoration, so that
 * */
@Composable
fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    decorationBox: @Composable (innerTextField: @Composable () -> Unit) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        modifier = modifier
            .border(2.dp, ChatTheme.colors.borders, shape = ChatTheme.shapes.inputField)
            .clip(ChatTheme.shapes.inputField)
            .background(ChatTheme.colors.inputBackground)
            .padding(8.dp),
        value = value,
        onValueChange = onValueChange,
        decorationBox = { innerTextField -> decorationBox(innerTextField) })
}