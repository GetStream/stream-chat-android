package io.getstream.chat.android.compose.sample.ui.channel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The default divider used in the "Channel Info" screens.
 *
 * @param height The height of the divider.
 */
@Composable
fun ChannelInfoContentDivider(height: Dp) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(height)
            .background(color = ChatTheme.colors.borders),
    )
}
