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

package io.getstream.chat.android.compose.ui.components.moderatedmessage

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.state.messages.list.ModeratedMessageOption

/**
 * Composable that represents a single option inside the [ModeratedMessageDialog].
 * By default shows only text of the action a user can perform.
 *
 * @param option The option that the user can choose for the moderated message.
 * @param modifier The [Modifier] for styling.
 */
@Composable
public fun ModeratedMessageOptionItem(
    option: ModeratedMessageOption,
    modifier: Modifier = Modifier,
) {
    HorizontalDivider(color = ChatTheme.colors.borderCoreDefault)
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(id = option.text),
            style = ChatTheme.typography.bodyDefault,
            color = ChatTheme.colors.accentPrimary,
        )
    }
}
