package io.getstream.chat.android.ui.common.extensions.internal

import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.utils.isRtlLayout

internal fun TextView.setTextSizePx(@Px size: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
}

/**
 * Sets the start drawable of a [TextView].
 *
 * @param id Res of drawable.
 */
internal fun TextView.setStartDrawable(@DrawableRes id: Int) {
    setStartDrawable(ContextCompat.getDrawable(context, id))
}

/**
 * Sets the start drawable of a [TextView] with a desired tint color.
 *
 * @param id Res of drawable.
 * @param tintColor Color int.
 */
internal fun TextView.setStartDrawableWithTint(@DrawableRes id: Int, @ColorInt tintColor: Int) {
    setStartDrawableWithTint(ContextCompat.getDrawable(context, id), tintColor)
}

/**
 * Sets the start drawable of a [TextView] with a desired tint color.
 *
 * @param drawable [Drawable].
 * @param tintColor Color int.
 */
internal fun TextView.setStartDrawableWithTint(drawable: Drawable?, @ColorInt tintColor: Int) {
    setStartDrawable(drawable?.apply { setTint(tintColor) })
}

/**
 * Sets the start drawable of a [TextView] with a desired size.
 *
 * @param id Res of drawable.
 * @param sizeRes Dimension id.
 */
internal fun TextView.setStartDrawableWithSize(@DrawableRes id: Int, @DimenRes sizeRes: Int) {
    setStartDrawableWithSize(ContextCompat.getDrawable(context, id), sizeRes)
}

/**
 * Sets the start drawable of a [TextView] with a desired size.
 *
 * @param drawable [Drawable].
 * @param sizeRes Dimension id.
 */
internal fun TextView.setStartDrawableWithSize(drawable: Drawable?, @DimenRes sizeRes: Int) {
    val size = resources.getDimensionPixelSize(sizeRes)
    val isRtl = context.isRtlLayout

    drawable?.setBounds(0, 0, size, size)

    if (isRtl) {
        setCompoundDrawables(null, null, drawable, null)
    } else {
        setCompoundDrawables(drawable, null, null, null)
    }
}

/**
 * Set the [drawable] to appear to the left of the text. If [drawable] is null,
 * nothing will be drawn on the left side of the text.
 */
internal fun TextView.setStartDrawable(drawable: Drawable?) {
    val isRtl = context.isRtlLayout

    if (isRtl) {
        setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
    } else {
        setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
    }
}
