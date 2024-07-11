/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * A dialog that should be shown if a user taps the seeing more options on the poll message.
 *
 * @param onDismissRequest Handler for dismissing the dialog.
 * @param onBackPressed Handler for pressing a back button.
 */
@Composable
public fun PollMoreOptionsDialog(
    onDismissRequest: () -> Unit,
    onBackPressed: () -> Unit,
) {
    Popup(
        onDismissRequest = onDismissRequest,
    ) {
        BackHandler { onBackPressed.invoke() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ChatTheme.colors.appBackground),
        ) {
            Text(text = "poll option details!")
        }
    }
}
