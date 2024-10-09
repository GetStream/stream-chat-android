package io.getstream.chat.ui.sample.util.extensions

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

internal fun Drawable.applyTint(@ColorInt tintColor: Int?): Drawable {
    if (tintColor == null) return this

    val tintedDrawable = DrawableCompat.wrap(this).mutate()
    DrawableCompat.setTint(tintedDrawable, tintColor)
    DrawableCompat.setTintMode(tintedDrawable, PorterDuff.Mode.SRC_IN)
    return tintedDrawable
}