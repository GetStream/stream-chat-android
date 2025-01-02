/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.feature.channel.add.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Composable component representing a search field for users.
 *
 * @param query The current query in the search field.
 * @param onQueryChanged The action to be invoked when the query is changed.
 * @param leadingContent The content to be shown at the start of the search field.
 */
@Composable
fun SearchUserTextField(
    query: String,
    onQueryChanged: (String) -> Unit,
    leadingContent: @Composable (() -> Unit)? = null,
) {
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = query,
        singleLine = true,
        maxLines = 1,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = ChatTheme.colors.barsBackground,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            cursorColor = ChatTheme.colors.primaryAccent,
            textColor = ChatTheme.colors.textHighEmphasis,
        ),
        onValueChange = onQueryChanged,
        placeholder = {
            Text(
                text = stringResource(id = R.string.add_channel_type_name),
                fontSize = 14.sp,
                color = ChatTheme.colors.textLowEmphasis,
            )
        },
        leadingIcon = leadingContent,
    )
}
