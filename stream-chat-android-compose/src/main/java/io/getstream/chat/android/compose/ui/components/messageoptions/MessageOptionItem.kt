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

package io.getstream.chat.android.compose.ui.components.messageoptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messageoptions.MessageOptionItemState
import io.getstream.chat.android.compose.ui.components.common.MenuOptionItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.ui.common.state.messages.Reply

/**
 * Each option item in the column of options.
 *
 * @param option The option to show.
 * @param modifier Modifier for styling.
 * @param verticalAlignment Used to apply vertical alignment.
 * @param horizontalArrangement Used to apply horizontal arrangement.
 */
@Composable
@Deprecated(
    message = "This class is deprecated and will be removed in the next version.",
    replaceWith = ReplaceWith("GenericMenuOptionItem"),
    level = DeprecationLevel.WARNING,
)
public fun MessageOptionItem(
    option: MessageOptionItemState,
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    onMessageOptionItemSelected: (MessageOptionItemState) -> Unit = {},
) {
    val title = stringResource(id = option.title)
    MenuOptionItem(
        modifier = modifier,
        title = title,
        titleColor = option.titleColor,
        leadingIcon = {
            Icon(
                modifier = Modifier.padding(horizontal = 16.dp),
                painter = option.iconPainter,
                tint = option.iconColor,
                contentDescription = title,
            )
        },
        onClick = { onMessageOptionItemSelected(option) },
        style = ChatTheme.typography.body,
        itemHeight = 56.dp,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
    )
}

/**
 * Preview of [MessageOptionItem].
 */
@Preview(showBackground = true, name = "MessageOptionItem Preview")
@Composable
private fun MessageOptionItemPreview() {
    ChatTheme {
        val option = MessageOptionItemState(
            title = R.string.stream_compose_reply,
            iconPainter = painterResource(R.drawable.stream_compose_ic_reply),
            action = Reply(PreviewMessageData.message1),
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconColor = ChatTheme.colors.textLowEmphasis,
        )

        MessageOptionItem(
            modifier = Modifier.fillMaxWidth(),
            option = option,
        )
    }
}
