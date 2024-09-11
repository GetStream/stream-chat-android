package io.getstream.chat.android.compose.ui.util

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.theme.ComponentPadding

/**
 * Adds padding to the modifier.
 */
internal fun Modifier.padding(padding: ComponentPadding): Modifier {
    return this.padding(
        start = padding.start,
        top = padding.top,
        end = padding.end,
        bottom = padding.bottom,
    )
}