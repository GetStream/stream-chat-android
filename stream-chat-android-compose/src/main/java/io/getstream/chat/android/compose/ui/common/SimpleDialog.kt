package io.getstream.chat.android.compose.ui.common

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R

/**
 * Generic dialog component that allows us to prompt the user.
 *
 * @param title - Title for the dialog.
 * @param message - Message for the dialog.
 * @param onPositiveAction - Handler when the user confirms the dialog.
 * @param onDismiss - Handler when the user dismisses the dialog.
 * @param modifier - Modifier for styling.
 * */
@Composable
fun SimpleDialog(
    title: String,
    message: String,
    onPositiveAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(message) },
        confirmButton = {
            Button(onClick = { onPositiveAction() }) {
                Text(text = stringResource(id = R.string.yes))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}