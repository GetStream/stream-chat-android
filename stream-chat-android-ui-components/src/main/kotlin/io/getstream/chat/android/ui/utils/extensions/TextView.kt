package io.getstream.chat.android.ui.utils.extensions

import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat

internal fun TextView.setTextSizePx(@Px size: Float) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
}

internal fun TextView.leftDrawable(@DrawableRes id: Int = 0, @DimenRes sizeRes: Int) {
    val drawable = ContextCompat.getDrawable(context, id)
    val size = resources.getDimensionPixelSize(sizeRes)
    drawable?.setBounds(0, 0, size, size)
    this.setCompoundDrawables(drawable, null, null, null)
}

internal fun TextView.leftDrawable(@DrawableRes id: Int = 0) {
    val drawable = ContextCompat.getDrawable(context, id)
    this.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)
}

internal fun TextView.setLeftDrawable(icon: Int, iconTint: Int) {
    setCompoundDrawablesWithIntrinsicBounds(
        ResourcesCompat.getDrawable(resources, icon, null)?.apply { setTint(iconTint) },
        null,
        null,
        null
    )
}
