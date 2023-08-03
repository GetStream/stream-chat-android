/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
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
 */
@Composable
public fun SearchInput(
    query: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onSearchStarted: () -> Unit = {},
    leadingIcon: @Composable RowScope.() -> Unit = { DefaultSearchLeadingIcon() },
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
                        painter = painterResource(id = R.drawable.stream_compose_ic_clear),
                        contentDescription = stringResource(id = R.string.stream_compose_search_input_cancel),
                        tint = ChatTheme.colors.textLowEmphasis,
                    )
                },
            )
        }
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

                Box(modifier = Modifier.weight(8f)) {
                    if (query.isEmpty()) {
                        label()
                    }

                    innerTextField()
                }

                trailingIcon?.invoke(this)
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
internal fun RowScope.DefaultSearchLeadingIcon() {
    Icon(
        modifier = Modifier.weight(1f),
        painter = painterResource(id = R.drawable.stream_compose_ic_search),
        contentDescription = null,
        tint = ChatTheme.colors.textLowEmphasis,
    )
}

/**
 * Default search input field label.
 */
@Composable
internal fun DefaultSearchLabel() {
    Text(
        text = stringResource(id = R.string.stream_compose_search_input_hint),
        style = ChatTheme.typography.body,
        color = ChatTheme.colors.textLowEmphasis,
    )
}

@Preview(name = "Search input")
@Composable
private fun SearchInputPreview() {
    ChatTheme {
        var searchQuery by rememberSaveable { mutableStateOf("") }

        SearchInput(
            modifier = Modifier
                .background(color = ChatTheme.colors.appBackground)
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
