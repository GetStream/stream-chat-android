package io.getstream.chat.android.compose.ui.util

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size as composeSize
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.theme.ComponentPadding
import io.getstream.chat.android.compose.ui.theme.ComponentSize

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

/**
 * Adds padding to the modifier.
 */
internal fun Modifier.size(size: ComponentSize): Modifier {
    return this.composeSize(
        width= size.width,
        height = size.height,
    )
}