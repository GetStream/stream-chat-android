package io.getstream.chat.android.ui.common.extensions.internal

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import androidx.annotation.ArrayRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.UiMode

@Px
internal fun Context.getDimension(@DimenRes dimen: Int): Int {
    return resources.getDimensionPixelSize(dimen)
}

internal fun Context.getIntArray(@ArrayRes id: Int): IntArray {
    return resources.getIntArray(id)
}

@ColorInt
internal fun Context.getColorCompat(@ColorRes color: Int): Int {
    return ContextCompat.getColor(this, color)
}

internal fun Context.getColorStateListCompat(@ColorRes color: Int): ColorStateList? {
    return ContextCompat.getColorStateList(this, color)
}

internal fun Context.getDrawableCompat(@DrawableRes id: Int): Drawable? {
    return ContextCompat.getDrawable(this, id)
}

internal fun Context?.getFragmentManager(): FragmentManager? {
    return when (this) {
        is AppCompatActivity -> supportFragmentManager
        is ContextWrapper -> baseContext.getFragmentManager()
        else -> null
    }
}

internal fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(null, text))
}

internal val Context.streamThemeInflater: LayoutInflater
    get() = LayoutInflater.from(this.createStreamThemeWrapper())

internal fun Context.createStreamThemeWrapper(uiMode: UiMode = ChatUI.uiMode): Context {
    val typedValue = TypedValue()

    return when {
        this.theme.resolveAttribute(R.attr.streamUiValidTheme, typedValue, true) ->
            ContextThemeWrapper(this, R.style.StreamUiEmptyTheme)
        this.theme.resolveAttribute(R.attr.streamUiTheme, typedValue, true) ->
            ContextThemeWrapper(this, typedValue.resourceId)
        else -> ContextThemeWrapper(this, R.style.StreamUiTheme)
    }.apply {
        applyOverrideConfiguration(
            Configuration().apply { systemUiMode(uiMode)?.let { this.uiMode = it }}
        )
    }
}

private fun systemUiMode(uiMode: UiMode) = when (uiMode) {
    UiMode.LIGHT -> Configuration.UI_MODE_NIGHT_NO
    UiMode.DARK -> Configuration.UI_MODE_NIGHT_YES
    UiMode.SYSTEM -> null
}
