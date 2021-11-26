package io.getstream.chat.android.compose.ui.messages.composer.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Composable that represents the message composer integrations (special actions).
 *
 * Currently just shows the Attachment picker action.
 *
 * @param onAttachmentsClick Handler when the user selects attachments.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultComposerIntegrations(
    onAttachmentsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier,
        content = {
            Icon(
                painter = painterResource(id = R.drawable.stream_compose_ic_attachments),
                contentDescription = stringResource(id = R.string.stream_compose_attachments),
                tint = ChatTheme.colors.textLowEmphasis,
            )
        },
        onClick = onAttachmentsClick
    )
}
