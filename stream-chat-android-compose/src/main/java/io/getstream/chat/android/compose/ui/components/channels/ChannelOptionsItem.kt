package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Default component for channel info options.
 *
 * @param title The text title of the action.
 * @param titleColor The color of the title.
 * @param leadingIcon The composable that defines the leading icon for the action.
 * @param onClick The action to perform once the user taps on any option.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun ChannelOptionsItem(
    title: String,
    titleColor: Color,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        leadingIcon()

        Text(
            text = title,
            style = ChatTheme.typography.bodyBold,
            color = titleColor
        )
    }
}

/**
 * Preview of [ChannelOptionsItem].
 *
 * Should show a channel action item with an icon and a name.
 */
@Preview(showBackground = true, name = "ChannelOptionsItem Preview")
@Composable
private fun ChannelOptionsItemPreview() {
    ChatTheme {
        ChannelOptionsItem(
            title = stringResource(id = R.string.stream_compose_channel_info_view_info),
            titleColor = ChatTheme.colors.textHighEmphasis,
            leadingIcon = {
                Icon(
                    modifier = Modifier
                        .size(56.dp)
                        .padding(16.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_person),
                    tint = ChatTheme.colors.textLowEmphasis,
                    contentDescription = null
                )
            },
            onClick = {}
        )
    }
}
