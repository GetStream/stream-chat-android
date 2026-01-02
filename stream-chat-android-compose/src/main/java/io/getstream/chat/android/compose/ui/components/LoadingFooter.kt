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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the loading footer UI in lists.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun LoadingFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = ChatTheme.colors.appBackground)
            .padding(top = 8.dp, bottom = 48.dp),
    ) {
        LoadingIndicator(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center),
        )
    }
}
