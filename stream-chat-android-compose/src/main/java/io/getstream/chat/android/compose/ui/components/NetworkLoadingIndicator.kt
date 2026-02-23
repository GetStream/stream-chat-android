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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the default network loading view for the header, in case the network is down.
 *
 * @param modifier Styling for the [Row].
 * @param spinnerSize The size of the spinner.
 * @param textStyle The text style of the inner text.
 * @param textColor The text color of the inner text.
 */
@Composable
public fun NetworkLoadingIndicator(
    modifier: Modifier = Modifier,
    spinnerSize: Dp = 18.dp,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    textColor: Color = ChatTheme.colors.textPrimary,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .size(spinnerSize),
            strokeWidth = 2.dp,
            color = ChatTheme.colors.accentPrimary,
        )

        Text(
            text = stringResource(id = R.string.stream_compose_waiting_for_network),
            style = textStyle,
            color = textColor,
        )
    }
}
