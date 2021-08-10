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
    val configuredContext = configureUiMode(uiMode)

    return when {
        configuredContext.theme.resolveAttribute(R.attr.streamUiValidTheme, typedValue, true) -> configuredContext
        configuredContext.theme.resolveAttribute(R.attr.streamUiTheme, typedValue, true) ->
            ContextThemeWrapper(configuredContext, typedValue.resourceId)
        else -> ContextThemeWrapper(configuredContext, R.style.StreamUiTheme)
    }
}

private fun Context.configureUiMode(uiMode: UiMode): Context = when (uiMode) {
    UiMode.LIGHT -> createConfigurationContext(resources.configuration.apply { this.uiMode = Configuration.UI_MODE_NIGHT_NO })
    UiMode.DARK -> createConfigurationContext(resources.configuration.apply { this.uiMode = Configuration.UI_MODE_NIGHT_YES })
    UiMode.SYSTEM -> this
}
