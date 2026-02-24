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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.composer.InputField
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The search component that allows the user to fill in a search query and filter their items.
 *
 * It also holds a clear action, that is active when the search is in focus and not empty, to clear
 * the input.
 *
 * @param query Current query value.
 * @param onValueChange Handler when the value changes.
 * @param modifier Modifier for styling.
 * @param onSearchStarted Handler when the search starts, by focusing the input field.
 * @param leadingIcon The icon at the start of the search component that's customizable, but shows
 * [DefaultSearchLeadingIcon] by default.
 * @param label The label shown in the search component, when there's no input.
 * @param clearButton The clear button shown when the search is focused and not empty.
 */
@Composable
public fun SearchInput(
    query: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchStarted: () -> Unit = {},
    leadingIcon: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            SearchInputLeadingIcon()
        }
    },
    label: @Composable () -> Unit = {
        ChatTheme.componentFactory.SearchInputLabel()
    },
    clearButton: (@Composable RowScope.() -> Unit) = {
        with(ChatTheme.componentFactory) {
            SearchInputClearButton(onClick = { onValueChange("") })
        }
    },
) {
    var isFocused by remember { mutableStateOf(false) }

    val trailingContent: (@Composable RowScope.() -> Unit)? = if (isFocused && query.isNotEmpty()) {
        clearButton
    } else {
        null
    }

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
                verticalAlignment = Alignment.CenterVertically,
            ) {
                leadingIcon()

                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        label()
                    }

                    innerTextField()
                }

                trailingContent?.invoke(this)
            }
        },
        maxLines = 1,
        innerPadding = PaddingValues(4.dp),
    )
}

/**
 * Default search input field leading "search" icon.
 */
@Composable
internal fun DefaultSearchLeadingIcon() {
    Icon(
        modifier = Modifier.padding(horizontal = 6.dp),
        painter = painterResource(id = R.drawable.stream_compose_ic_search),
        contentDescription = null,
        tint = ChatTheme.colors.textTertiary,
    )
}

/**
 * Default search input field label.
 */
@Composable
internal fun DefaultSearchLabel() {
    Text(
        text = stringResource(id = R.string.stream_compose_search_input_hint),
        style = ChatTheme.typography.bodyDefault,
        color = ChatTheme.colors.textTertiary,
    )
}

/**
 * Default clear button for the search input field when the input is focused and not empty.
 */
@Composable
internal fun DefaultSearchClearButton(onClick: () -> Unit) {
    IconButton(
        modifier = Modifier
            .padding(horizontal = 4.dp)
            .size(24.dp),
        onClick = onClick,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.stream_compose_ic_clear),
            contentDescription = stringResource(id = R.string.stream_compose_search_input_cancel),
            tint = ChatTheme.colors.textTertiary,
        )
    }
}

@Preview(name = "Search input")
@Composable
private fun SearchInputPreview() {
    ChatTheme {
        var searchQuery by rememberSaveable { mutableStateOf("") }

        SearchInput(
            modifier = Modifier
                .background(color = ChatTheme.colors.backgroundCoreApp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .fillMaxWidth(),
            query = searchQuery,
            onSearchStarted = {},
            onValueChange = {
                searchQuery = it
            },
        )
    }
}
