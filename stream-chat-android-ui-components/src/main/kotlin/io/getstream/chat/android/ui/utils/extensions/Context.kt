package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.ContextThemeWrapper
import androidx.annotation.ArrayRes
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager

internal fun Context.getDimension(@DimenRes dimen: Int): Int {
    return resources.getDimensionPixelSize(dimen)
}

internal fun Context.getIntArray(@ArrayRes id: Int): IntArray {
    return resources.getIntArray(id)
}

internal fun Context.getColorCompat(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}

internal fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

internal fun Context?.getFragmentManager(): FragmentManager? {
    return when (this) {
        is AppCompatActivity -> supportFragmentManager
        is ContextThemeWrapper -> baseContext.getFragmentManager()
        else -> null
    }
}
