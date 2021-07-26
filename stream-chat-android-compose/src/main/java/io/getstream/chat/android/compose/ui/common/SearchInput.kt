package io.getstream.chat.android.compose.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The search component that allows the user to fill in a search query and filter their items.
 *
 * It also holds a clear action, that is active when the search is in focus and not empty, to clear
 * the input.
 *
 * @param modifier - Modifier for styling.
 * @param query - Current query value.
 * @param onValueChange - Handler when the value changes.
 * @param onSearchStarted - Handler when the search starts, by focusing the input field.
 * @param leadingIcon - The icon at the start of the search component that's customizable, but shows
 * [DefaultSearchLeadingIcon] by default.
 * @param label - The label shown in the search component, when there's no input.
 * */
@Composable
fun SearchInput(
    query: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchStarted: () -> Unit = {},
    leadingIcon: @Composable RowScope.() -> Unit = { DefaultSearchLeadingIcon(empty = query.isEmpty()) },
    label: @Composable () -> Unit = { DefaultSearchLabel() },
) {
    var isFocused by remember { mutableStateOf(false) }

    val trailingIcon: (@Composable RowScope.() -> Unit)? = if (isFocused && query.isNotEmpty()) {
        @Composable {
            IconButton(
                modifier = Modifier
                    .weight(1f)
                    .size(24.dp),
                onClick = { onValueChange("") },
                content = {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = stringResource(id = R.string.cancel_search),
                        tint = ChatTheme.colors.textLowEmphasis,
                    )
                }
            )
        }
    } else null

    InputField(
        modifier = modifier
            .onFocusEvent { newState ->
                val wasPreviouslyFocused = isFocused

                if (!wasPreviouslyFocused && newState.isFocused) {
                    onSearchStarted()
                }

                isFocused = newState.isFocused
            },
        value = query,
        onValueChange = onValueChange,
        decorationBox = { innerTextField ->
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon()

                Box(modifier = Modifier.weight(8f)) {
                    if (query.isEmpty()) {
                        label()
                    }

                    innerTextField()
                }

                trailingIcon?.invoke(this)
            }
        }
    )
}

/**
 * Default search input field leading "search" icon.
 * */
@Composable
internal fun RowScope.DefaultSearchLeadingIcon(empty: Boolean) {
    Icon(
        modifier = Modifier.weight(1f),
        imageVector = Icons.Default.Search,
        contentDescription = null,
        tint = if (!empty) ChatTheme.colors.textHighEmphasis else ChatTheme.colors.textLowEmphasis,
    )
}

/**
 * Default search input field label.
 * */
@Composable
internal fun DefaultSearchLabel() {
    Text(
        text = stringResource(id = R.string.query_channels),
        color = ChatTheme.colors.textLowEmphasis,
    )
}
