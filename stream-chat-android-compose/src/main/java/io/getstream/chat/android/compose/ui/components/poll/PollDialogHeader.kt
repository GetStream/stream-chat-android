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

package io.getstream.chat.android.compose.ui.components.poll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
@Composable
public fun PollDialogHeader(
    title: String,
    onBackPressed: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackButton(
            modifier = Modifier.padding(end = 32.dp),
            painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
            onBackPressed = onBackPressed,
        )

        Text(
            text = title,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }
}

@Preview
@Composable
private fun PollDialogHeaderPreview() {
    ChatTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ChatTheme.colors.appBackground),
        ) {
            PollDialogHeader(
                title = stringResource(id = R.string.stream_compose_poll_results),
                onBackPressed = {},
            )
        }
    }
}
