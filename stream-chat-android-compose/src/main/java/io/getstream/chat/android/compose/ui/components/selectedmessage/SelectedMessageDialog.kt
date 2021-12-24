package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import io.getstream.chat.android.compose.handlers.SystemBackPressedHandler
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a modal dialog that is shown when either message or message reactions are selected.
 *
 * @param modifier Modifier for styling.
 * @param shape Changes the shape of the dialog.
 * @param overlayColor The color applied to the overlay.
 * @param onDismiss Handler called when the dialog is dismissed.
 * @param headerContent The content shown at the top of the dialog.
 * @param centerContent The content shown in the dialog.
 */
@Composable
internal fun SelectedMessageDialog(
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
    overlayColor: Color = ChatTheme.colors.overlay,
    onDismiss: () -> Unit = {},
    headerContent: @Composable ColumnScope.() -> Unit = {},
    centerContent: @Composable ColumnScope.() -> Unit = {},
) {
    Box(
        modifier = Modifier
            .background(overlayColor)
            .fillMaxSize()
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Card(
            modifier = modifier
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            shape = shape,
            backgroundColor = ChatTheme.colors.barsBackground
        ) {
            Column(modifier) {
                headerContent()

                centerContent()
            }
        }
    }

    SystemBackPressedHandler(isEnabled = true) {
        onDismiss()
    }
}
