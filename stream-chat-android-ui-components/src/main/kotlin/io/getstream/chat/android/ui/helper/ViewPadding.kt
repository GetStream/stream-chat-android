package io.getstream.chat.android.ui.helper

import androidx.annotation.Px

/**
 * Represents the view's padding
 *
 * @param start the start padding of the view
 * @param top the top padding of the view
 * @param end the end padding of the view
 * @param bottom the bottom padding of the view
 */
public data class ViewPadding(
    @Px val start: Int,
    @Px val top: Int,
    @Px val end: Int,
    @Px val bottom: Int,
)
