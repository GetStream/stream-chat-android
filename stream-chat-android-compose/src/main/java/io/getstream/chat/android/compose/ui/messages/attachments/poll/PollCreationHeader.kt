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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.mirrorRtl

/**
 * Poll's creation header (toolbar) Composable that consist of several components.
 *
 * @param modifier The [Modifier] for styling.
 * @param color The color of the surface.
 * @param shape The shape of the surface.
 * @param elevation The elevation of the surface.
 * @param onBackPressed A lambda that will be executed if users click the back button on the default [leadingContent].
 * @param enabledCreation Represents if user can click the creation button or not.
 * @param onPollCreateClicked A lambda that will be executed if users click the poll creation button.
 * @param leadingContent Customizable composable function that represents the leading content of a poll creation item, usually
 * holding a back action button.
 * @param centerContent Customizable composable function that represents the center content of a poll creation item, usually
 * holding information about the title.
 * @param trailingContent Customizable composable function that represents the trailing content of a poll creation item,
 * usually holding the creation action button.
 */
@Composable
public fun PollCreationHeader(
    modifier: Modifier = Modifier,
    color: Color = ChatTheme.colors.appBackground,
    shape: Shape = ChatTheme.shapes.header,
    elevation: Dp = 0.dp,
    onBackPressed: () -> Unit = {},
    enabledCreation: Boolean,
    onPollCreateClicked: () -> Unit,
    leadingContent: @Composable (RowScope.() -> Unit)? = null,
    centerContent: @Composable (RowScope.() -> Unit)? = null,
    trailingContent: @Composable (RowScope.() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = elevation,
        color = color,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent?.invoke(this) ?: DefaultPollOptionsHeaderLeadingContent(onBackPressed = onBackPressed)

            centerContent?.invoke(this) ?: DefaultPollOptionsHeaderCenterContent(
                modifier = Modifier.weight(1f),
                title = stringResource(id = R.string.stream_compose_poll_title),
            )

            trailingContent?.invoke(this) ?: DefaultPollOptionsHeaderTrailingContent(
                enabled = enabledCreation,
                onPollCreateClicked = onPollCreateClicked,
            )
        }
    }
}

@Composable
internal fun DefaultPollOptionsHeaderLeadingContent(onBackPressed: () -> Unit) {
    val layoutDirection = LocalLayoutDirection.current

    BackButton(
        modifier = Modifier
            .mirrorRtl(layoutDirection = layoutDirection)
            .padding(end = 32.dp),
        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
        onBackPressed = onBackPressed,
    )
}

@Composable
internal fun DefaultPollOptionsHeaderCenterContent(modifier: Modifier, title: String) {
    Column(
        modifier = modifier.height(IntrinsicSize.Max),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }
}

@Composable
internal fun DefaultPollOptionsHeaderTrailingContent(
    enabled: Boolean,
    onPollCreateClicked: () -> Unit,
) {
    IconButton(
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = ChatTheme.colors.primaryAccent,
            disabledContentColor = ChatTheme.colors.textLowEmphasis,
        ),
        onClick = onPollCreateClicked,
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_compose_ic_send),
            contentDescription = stringResource(R.string.stream_compose_send_message),
        )
    }
}

@Preview(name = "PollOptionHeader Light Mode")
@Preview(name = "PollOptionHeader Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PollOptionHeaderPreview() {
    ChatTheme {
        Column {
            PollCreationHeader(enabledCreation = true, onPollCreateClicked = {})
            PollCreationHeader(enabledCreation = false, onPollCreateClicked = {})
        }
    }
}
