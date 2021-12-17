package io.getstream.chat.android.compose.ui.components

import androidx.compose.material.AlertDialog
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Generic dialog component that allows us to prompt the user.
 *
 * @param title Title for the dialog.
 * @param message Message for the dialog.
 * @param onPositiveAction Handler when the user confirms the dialog.
 * @param onDismiss Handler when the user dismisses the dialog.
 * @param modifier Modifier for styling.
 */
@Composable
public fun SimpleDialog(
    title: String,
    message: String,
    onPositiveAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = ChatTheme.colors.textHighEmphasis,
                style = ChatTheme.typography.title3Bold,
            )
        },
        text = {
            Text(
                text = message,
                color = ChatTheme.colors.textHighEmphasis,
                style = ChatTheme.typography.body,
            )
        },
        confirmButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
                onClick = { onPositiveAction() }
            ) {
                Text(text = stringResource(id = R.string.stream_compose_ok))
            }
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = ChatTheme.colors.primaryAccent),
                onClick = onDismiss
            ) {
                Text(text = stringResource(id = R.string.stream_compose_cancel))
            }
        },
        backgroundColor = ChatTheme.colors.barsBackground,
    )
}
