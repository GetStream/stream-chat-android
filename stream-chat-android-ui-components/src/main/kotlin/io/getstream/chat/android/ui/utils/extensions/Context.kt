package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.annotation.ArrayRes
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager

internal fun Context.getDimension(@DimenRes dimen: Int): Int {
    return resources.getDimensionPixelSize(dimen)
}

internal fun Context.getIntArray(@ArrayRes id: Int): IntArray {
    return resources.getIntArray(id)
}

internal fun Context?.getFragmentManager(): FragmentManager? {
    return when (this) {
        is AppCompatActivity -> supportFragmentManager
        is ContextThemeWrapper -> baseContext.getFragmentManager()
        else -> null
    }
}
