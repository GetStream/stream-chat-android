package io.getstream.chat.android.compose.ui.messages.composer.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
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
internal fun RowScope.DefaultComposerIntegrations(
    onAttachmentsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier
            .align(CenterVertically),
        content = {
            Icon(
                imageVector = Icons.Default.Attachment,
                contentDescription = stringResource(id = R.string.stream_compose_attachments),
                tint = ChatTheme.colors.textLowEmphasis,
            )
        },
        onClick = onAttachmentsClick
    )
}
