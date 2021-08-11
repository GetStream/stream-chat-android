package io.getstream.chat.android.ui.common.extensions.internal

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat

internal fun TextView.setTextSizePx(@Px size: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
}

internal fun TextView.setLeftDrawable(@DrawableRes id: Int) {
    setLeftDrawable(ContextCompat.getDrawable(context, id))
}

internal fun TextView.setLeftDrawableWithTint(@DrawableRes id: Int, @ColorInt tintColor: Int) {
    setLeftDrawableWithTint(ContextCompat.getDrawable(context, id), tintColor)
}

internal fun TextView.setLeftDrawableWithTint(drawable: Drawable?, @ColorInt tintColor: Int) {
    setLeftDrawable(drawable?.apply { setTint(tintColor) })
}

internal fun TextView.setLeftDrawableWithSize(@DrawableRes id: Int, @DimenRes sizeRes: Int) {
    setLeftDrawableWithSize(ContextCompat.getDrawable(context, id), sizeRes)
}

internal fun TextView.setLeftDrawableWithSize(drawable: Drawable?, @DimenRes sizeRes: Int) {
    val size = resources.getDimensionPixelSize(sizeRes)
    drawable?.setBounds(0, 0, size, size)
    setCompoundDrawables(drawable, null, null, null)
}

/**
 * Set the [drawable] to appear to the left of the text. If [drawable] is null,
 * nothing will be drawn on the left side of the text.
 */
internal fun TextView.setLeftDrawable(drawable: Drawable?) {
    setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}
