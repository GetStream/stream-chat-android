package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Represents the options user can take after selecting a message.
 *
 * @param modifier Modifier for styling.
 * @param headerContent Leading vertical Composable that allows the user to customize the content shown in [SelectedMessageOptions].
 * @param bodyContent Trailing vertical Composable that allows the user to customize the content shown in [SelectedMessageOptions].
 */
@Composable
public fun SelectedMessageOptions(
    modifier: Modifier = Modifier,
    headerContent: @Composable ColumnScope.() -> Unit,
    bodyContent: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier) {
        headerContent()
        bodyContent()
    }
}
