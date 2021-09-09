package io.getstream.chat.android.compose.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The view that's shown when there's no data available.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun EmptyView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.background(color = ChatTheme.colors.appBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            painter = painterResource(id = R.drawable.stream_compose_empty_state_bubble),
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis
        )

        Spacer(Modifier.size(16.dp))

        Text(
            text = stringResource(id = R.string.stream_compose_empty_state_title),
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.size(8.dp))

        Text(
            text = stringResource(id = R.string.stream_compose_empty_state_message),
            style = ChatTheme.typography.body,
            color = ChatTheme.colors.textLowEmphasis,
            textAlign = TextAlign.Center
        )
    }
}
