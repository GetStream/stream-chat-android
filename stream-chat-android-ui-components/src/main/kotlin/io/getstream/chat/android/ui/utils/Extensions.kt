package io.getstream.chat.android.ui.utils

import android.content.Context
import android.view.View

/**
 * Helper method to check is the system requests RTL direction.
 */
internal val Context.isRtlLayout: Boolean
    get() = resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
